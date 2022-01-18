/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.performance.tests.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.api.policy_documents.PolicyDocumentService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.resource_types.ResourceTypeService;
import io.imunity.furms.api.services.InfraServiceService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.api.user.api.key.UserApiKeyService;
import io.imunity.furms.unity.client.UnityClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.imunity.furms.performance.tests.SecurityUserUtils.createSecurityUser;
import static java.lang.String.format;

@SpringBootTest
@ActiveProfiles("performance_tests")
public class DataLoader {

	@Autowired private PolicyDocumentService policyDocumentService;
	@Autowired private ResourceTypeService resourceTypeService;
	@Autowired private InfraServiceService infraServiceService;
	@Autowired private SiteService siteService;
	@Autowired private CommunityService communityService;
	@Autowired private ProjectService projectService;
	@Autowired private UnityClient unityClient;
	@Autowired private UserApiKeyService userApiKeyService;

	private SiteDataLoader siteDataLoader;
	private CommunityDataLoader communityDataLoader;
	private UserDataLoader userDataLoader;
	private ProjectMembershipsDataLoader projectMembershipsDataLoader;

	private final static long SITES_COUNT = 30;
	private final static long BIG_COMMUNITIES_COUNT = 1;
	private final static long BIG_COMMUNITIES_PROJECTS_COUNT = 2400;
	private final static long COMMUNITIES_COUNT = 60;
	private final static long COMMUNITIES_PROJECTS_COUNT = 10;
	private final static long USERS_COUNT = 30000;

	@BeforeEach
	void setUp() {
		siteDataLoader = new SiteDataLoader(policyDocumentService, resourceTypeService, infraServiceService, siteService);
		communityDataLoader = new CommunityDataLoader(communityService, projectService);
		userDataLoader = new UserDataLoader(unityClient, userApiKeyService, communityService, projectService);
		projectMembershipsDataLoader = new ProjectMembershipsDataLoader(projectService);
	}

	/**
	 * Before generating performance test data required is
	 * generating users outside of this test.
	 *
	 * There is available
	 */
	@Test
	public void dataGenerator_WithoutUsersCreation() throws JsonProcessingException {
		loadData(() -> userDataLoader.findAllUsers());
	}

	/**
	 * This method is simpler but It takes a lot of time.
	 *
	 * You can use automatic script to load all users.
	 * Script is available in test/resources/massUsersInitializer.groovy
	 *
	 * To use this script put his to your UNITY SERVER configuration and
	 * add properties:
	 *
	 * unityServer.core.script.${order}.file=classpath:path_to_script/massUsersInitializer.groovy
	 * unityServer.core.script.${order}.trigger=pre-init
	 *
	 * After run UNITY SERVER without earlier data users should be loaded in few seconds.
	 *
	 * To define how much users have to be created use parameters:
	 *      ENTITIES * IN_BATCH = number of created users
	 */
	@Test
	public void dataGenerator_WithUsersCreation() throws JsonProcessingException {
		loadData(() -> userDataLoader.loadUsers(USERS_COUNT));
	}

	private void loadData(Supplier<Set<Data.User>> userLoaderFunction) throws JsonProcessingException {
		createSecurityUser(Map.of());

		final LocalDateTime startLoading = LocalDateTime.now();

		final Set<Data.User> users = userLoaderFunction.get();

		final Set<Data.Site> sites = siteDataLoader.loadSites(SITES_COUNT);
		final Data.Community bigCommunity = communityDataLoader.loadCommunities(
				BIG_COMMUNITIES_COUNT,
				BIG_COMMUNITIES_PROJECTS_COUNT,
				users).stream().findFirst().get();
		final Set<Data.Community> communities = communityDataLoader.loadCommunities(
				COMMUNITIES_COUNT,
				COMMUNITIES_PROJECTS_COUNT,
				users);

		final Map<Data.User, Set<Data.Project>> projectMemberships = projectMembershipsDataLoader.loadProjectMemberships(
				users, combineProjects(bigCommunity, communities));

		final Data.User fenixAdmin = userDataLoader.createFenixAdmin();
		final Set<Data.Community> allCommunities = new HashSet<>();
		allCommunities.add(bigCommunity);
		allCommunities.addAll(communities);

		final Data.User communitiesAdmin = userDataLoader.createCommunitiesAdmin(allCommunities);

		final Data.User projectsAdmin = userDataLoader.createProjectsAdmin(projectMemberships);

		final LocalDateTime endLoading = LocalDateTime.now();

		prettyPrintResults(startLoading, endLoading,
				new Data.Results(sites, allCommunities, fenixAdmin, communitiesAdmin, projectsAdmin));
	}

	private List<Data.Project> combineProjects(Data.Community bigCommunity, Set<Data.Community> communities) {
		final List<Data.Project> projects = Stream.concat(
				bigCommunity.projectIds.stream()
						.map(projectId -> new Data.Project(projectId, bigCommunity.communityId)),
				communities.stream()
						.map(community -> community.projectIds.stream()
								.map(projectId -> new Data.Project(projectId, community.communityId))
								.collect(Collectors.toList()))
						.flatMap(Collection::stream))
				.collect(Collectors.toList());
		Collections.shuffle(projects);
		return projects;
	}

	private void prettyPrintResults(LocalDateTime startLoading,
	                                LocalDateTime endLoading,
	                                Data.Results results) throws JsonProcessingException {
		System.out.println(format("Data loaded in: %s [s]", Duration.between(startLoading, endLoading)));
		System.out.println(new ObjectMapper().writeValueAsString(results));
	}

}
