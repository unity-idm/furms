/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.server;

import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityGroup;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectGroup;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.communites.CommunityGroupsDAO;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.projects.ProjectGroupsDAO;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.sites.SiteWebClient;
import io.imunity.furms.spi.users.UsersDAO;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDateTime;

import static java.util.function.Function.identity;

@Component
@Profile("demo-data-provisioning")
class DemoDataInitializer {
	private final CommunityRepository communityRepository;
	private final CommunityGroupsDAO communityGroupsDAO;

	private final SiteRepository siteRepository;
	private final SiteWebClient siteWebClient;
	private final UsersDAO usersDAO;
	private final ProjectRepository projectRepository;
	private final ProjectGroupsDAO projectGroupsDAO;
	private final UnityServerDetector unityDetector;

	public DemoDataInitializer(CommunityRepository communityRepository,
			CommunityGroupsDAO communityGroupsDAO,
			ProjectRepository projectRepository,
			ProjectGroupsDAO projectGroupsDAO,
			SiteRepository siteRepository,
			SiteWebClient siteWebClient,
			UsersDAO usersDAO,
			UnityServerDetector unityDetector) {
		this.communityRepository = communityRepository;
		this.communityGroupsDAO = communityGroupsDAO;
		this.projectRepository = projectRepository;
		this.projectGroupsDAO = projectGroupsDAO;
		this.siteRepository = siteRepository;
		this.siteWebClient = siteWebClient;
		this.usersDAO = usersDAO;
		this.unityDetector = unityDetector;
	}

	@PostConstruct
	public void init() throws IOException, InterruptedException {
		unityDetector.waitForUnityToStartUp();
		initCommunitiesAndProjects();
		initSites();
	}

	private void initCommunitiesAndProjects() throws IOException {
		
		PersistentId testAdminId = usersDAO.getAllUsers().stream()
			.filter(user -> user.email.equals("admin@domain.com"))
			.map(user -> user.id)
			.findAny()
			.flatMap(identity())
			.orElse(null);
		if(communityRepository.findAll().isEmpty()) {
			byte[] imgHBPFile = getClass().getClassLoader().getResourceAsStream("demo/HBP.png").readAllBytes();
			Community community = Community.builder()
				.name("HBP")
				.description("Human Brain Project")
				.logo(imgHBPFile, "png")
				.build();

			byte[] imgPRACEFile = getClass().getClassLoader().getResourceAsStream("demo/PRACE.png").readAllBytes();
			Community community2 = Community.builder()
				.name("PRACE")
				.description("Partnership for Advance Computing")
				.logo(imgPRACEFile, "png")
				.build();

			String communityId = communityRepository.create(community);
			communityGroupsDAO.create(new CommunityGroup(communityId, community.getName()));
			String community2Id = communityRepository.create(community2);
			communityGroupsDAO.create(new CommunityGroup(community2Id, community2.getName()));

			Project project = Project.builder()
				.name("Neuroinforamtics")
				.communityId(communityId)
				.description("Mouse Brain Project")
				.logo(imgHBPFile, "png")
				.acronym("NI")
				.researchField("AI")
				.startTime(LocalDateTime.now())
				.endTime(LocalDateTime.now().plusWeeks(20))
				.leaderId(testAdminId)
				.build();

			Project project2 = Project.builder()
				.name("Brain simulation")
				.communityId(communityId)
				.description("Human Brain Project")
				.logo(imgHBPFile, "png")
				.acronym("BS")
				.researchField("AI")
				.startTime(LocalDateTime.now())
				.endTime(LocalDateTime.now().plusWeeks(10))
				.leaderId(testAdminId)
				.build();

			String projectId = projectRepository.create(project);
			projectGroupsDAO.create(new ProjectGroup(projectId, project.getName(), communityId));
			String project2Id = projectRepository.create(project2);
			projectGroupsDAO.create(new ProjectGroup(project2Id, project2.getName(), communityId));

			projectGroupsDAO.addAdmin(communityId, projectId, testAdminId);
			projectGroupsDAO.addAdmin(communityId, project2Id, testAdminId);

			Project project3 = Project.builder()
				.name("Neuroinforamtics2")
				.communityId(community2Id)
				.description("Mouse Brain Project")
				.logo(imgHBPFile, "png")
				.acronym("NI")
				.researchField("AI")
				.startTime(LocalDateTime.now())
				.endTime(LocalDateTime.now().plusWeeks(30))
				.leaderId(testAdminId)
				.build();

			Project project4 = Project.builder()
				.name("Brain simulation2")
				.communityId(community2Id)
				.description("Human Brain Project")
				.logo(imgHBPFile, "png")
				.acronym("BS")
				.researchField("AI")
				.startTime(LocalDateTime.now())
				.endTime(LocalDateTime.now().plusWeeks(13))
				.leaderId(testAdminId)
				.build();

			String project3Id = projectRepository.create(project3);
			projectGroupsDAO.create(new ProjectGroup(project3Id, project3.getName(), community2Id));
			String project4Id = projectRepository.create(project4);
			projectGroupsDAO.create(new ProjectGroup(project4Id, project4.getName(), community2Id));

			projectGroupsDAO.addAdmin(community2Id, project3Id, testAdminId);
			projectGroupsDAO.addAdmin(community2Id, project4Id, testAdminId);
		}
	}

	private void initSites() {
		if (siteRepository.findAll().isEmpty()) {
			Site cineca = Site.builder()
					.name("CINECA")
					.build();
			Site fzj = Site.builder()
					.name("FZJ")
					.build();
			Site bsc = Site.builder()
					.name("BSC")
					.build();

			String cinecaId = siteRepository.create(cineca);
			String fzjId = siteRepository.create(fzj);
			String bscId = siteRepository.create(bsc);

			siteWebClient.create(Site.builder().id(cinecaId).name(cineca.getName()).build());
			siteWebClient.create(Site.builder().id(fzjId).name(fzj.getName()).build());
			siteWebClient.create(Site.builder().id(bscId).name(bsc.getName()).build());
		}
	}
}
