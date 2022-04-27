/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.user_site_access;

import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class UserSiteAccessDatabaseRepositoryTest extends DBIntegrationTest {
	@Autowired
	private UserSiteAccessDatabaseRepository userSiteAccessDatabaseRepository;

	@Autowired
	private UserSiteAccessEntityRepository userSiteAccessEntityRepository;
	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private CommunityRepository communityRepository;
	@Autowired
	private ProjectRepository projectRepository;

	private SiteId siteId;
	private ProjectId projectId;

	private SiteId siteId1;
	private ProjectId projectId1;

	@BeforeEach
	void setUp() {
		userSiteAccessEntityRepository.deleteAll();

		Site site = Site.builder()
			.name("name")
			.build();

		siteId = siteRepository.create(site, new SiteExternalId("id"));

		Community community = Community.builder()
			.name("name")
			.logo(FurmsImage.empty())
			.build();
		CommunityId communityId = communityRepository.create(community);

		Project project = Project.builder()
			.communityId(communityId)
			.name("name")
			.description("new_description")
			.logo(FurmsImage.empty())
			.acronym("acronym")
			.researchField("research filed")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();
		projectId = projectRepository.create(project);

		Site site1 = Site.builder()
			.name("name1")
			.build();

		siteId1 = siteRepository.create(site1, new SiteExternalId("id1"));

		Community community1 = Community.builder()
			.name("name1")
			.logo(FurmsImage.empty())
			.build();
		CommunityId communityId1 = communityRepository.create(community1);

		Project project1 = Project.builder()
			.communityId(communityId1)
			.name("name1")
			.description("new_description")
			.logo(FurmsImage.empty())
			.acronym("acronym")
			.researchField("research filed")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();
		projectId1 = projectRepository.create(project1);
	}

	@Test
	void shouldAdd(){
		FenixUserId userId = new FenixUserId("userId");
		userSiteAccessDatabaseRepository.add(siteId, projectId, userId);

		assertTrue(userSiteAccessEntityRepository.existsBySiteIdAndProjectIdAndUserId(siteId.id, projectId.id,
			userId.id));
	}

	@Test
	void shouldFindAllUserGroupedBySiteId(){
		FenixUserId userId = new FenixUserId("userId");
		FenixUserId userId1 = new FenixUserId("userId1");
		userSiteAccessEntityRepository.save(new UserSiteAccessEntity(siteId.id, projectId.id, userId.id));
		userSiteAccessEntityRepository.save(new UserSiteAccessEntity(siteId1.id, projectId.id, userId.id));
		userSiteAccessEntityRepository.save(new UserSiteAccessEntity(siteId1.id, projectId.id, userId1.id));
		userSiteAccessEntityRepository.save(new UserSiteAccessEntity(siteId1.id, projectId1.id, userId1.id));

		Map<SiteId, Set<FenixUserId>> allUserGroupedBySiteId =
			userSiteAccessDatabaseRepository.findAllUserGroupedBySiteId(projectId);

		assertEquals(Set.of(siteId, siteId1), allUserGroupedBySiteId.keySet());
		assertEquals(Set.of(userId), allUserGroupedBySiteId.get(siteId));
		assertEquals(Set.of(userId, userId1), allUserGroupedBySiteId.get(siteId1));
	}

	@Test
	void shouldFindAllUserProjectIds(){
		FenixUserId userId = new FenixUserId("userId");
		FenixUserId userId1 = new FenixUserId("userId1");
		userSiteAccessEntityRepository.save(new UserSiteAccessEntity(siteId.id, projectId.id, userId.id));
		userSiteAccessEntityRepository.save(new UserSiteAccessEntity(siteId1.id, projectId.id, userId1.id));
		userSiteAccessEntityRepository.save(new UserSiteAccessEntity(siteId.id, projectId1.id, userId.id));

		Set<ProjectId> allUserProjectIds = userSiteAccessDatabaseRepository.findAllUserProjectIds(siteId, userId);

		assertEquals(Set.of(projectId, projectId1), allUserProjectIds);
	}

	@Test
	void shouldRemoveByProjectIdAndUserId(){
		FenixUserId userId = new FenixUserId("userId");
		userSiteAccessEntityRepository.save(new UserSiteAccessEntity(siteId.id, projectId.id, userId.id));
		userSiteAccessEntityRepository.save(new UserSiteAccessEntity(siteId1.id, projectId.id, userId.id));

		userSiteAccessDatabaseRepository.remove(projectId, userId);

		assertFalse(userSiteAccessEntityRepository.existsBySiteIdAndProjectIdAndUserId(siteId.id, projectId.id, userId.id));
		assertFalse(userSiteAccessEntityRepository.existsBySiteIdAndProjectIdAndUserId(siteId1.id, projectId.id, userId.id));
	}

	@Test
	void shouldRemoveBySiteIdAndProjectIdAndUserId(){
		FenixUserId userId = new FenixUserId("userId");
		userSiteAccessEntityRepository.save(new UserSiteAccessEntity(siteId.id, projectId.id, userId.id));

		userSiteAccessDatabaseRepository.remove(siteId, projectId, userId);

		assertFalse(userSiteAccessEntityRepository.existsBySiteIdAndProjectIdAndUserId(siteId.id, projectId.id, userId.id));
	}

	@Test
	void shouldExistBySiteIdAndProjectIdAndUserId(){
		FenixUserId userId = new FenixUserId("userId");
		userSiteAccessEntityRepository.save(new UserSiteAccessEntity(siteId.id, projectId.id, userId.id));

		assertTrue(userSiteAccessDatabaseRepository.exists(siteId, projectId, userId));
	}

	@Test
	void shouldNotExistBySiteIdAndProjectIdAndUserId(){
		FenixUserId userId = new FenixUserId("userId");
		userSiteAccessEntityRepository.save(new UserSiteAccessEntity(siteId.id, projectId.id, userId.id));

		assertFalse(userSiteAccessDatabaseRepository.exists(siteId, projectId1, userId));
	}
}
