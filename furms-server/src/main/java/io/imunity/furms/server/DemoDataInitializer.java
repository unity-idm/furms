/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.server;

import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityGroup;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectGroup;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_types.ResourceMeasureType;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.site.api.site_agent.SiteAgentService;
import io.imunity.furms.spi.communites.CommunityGroupsDAO;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.projects.ProjectGroupsDAO;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.services.InfraServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.sites.SiteWebClient;
import io.imunity.furms.spi.users.UsersDAO;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static java.util.function.Function.identity;

@Component
@Profile("demo-data-provisioning")
class DemoDataInitializer implements CommandLineRunner {
	private final CommunityRepository communityRepository;
	private final CommunityGroupsDAO communityGroupsDAO;

	private final SiteRepository siteRepository;
	private final SiteWebClient siteWebClient;
	private final UsersDAO usersDAO;
	private final ProjectRepository projectRepository;
	private final ProjectGroupsDAO projectGroupsDAO;
	private final UnityServerDetector unityDetector;
	private final InfraServiceRepository infraServiceRepository;
	private final ResourceTypeRepository resourceTypeRepository;
	private final ResourceCreditRepository resourceCreditRepository;
	private final CommunityAllocationRepository communityAllocationRepository;
	private final SiteAgentService siteAgentService;

	private String communityId;
	private String community2Id;

	DemoDataInitializer(CommunityRepository communityRepository, CommunityGroupsDAO communityGroupsDAO,
	                    SiteRepository siteRepository, SiteWebClient siteWebClient, UsersDAO usersDAO,
	                    ProjectRepository projectRepository, ProjectGroupsDAO projectGroupsDAO,
	                    UnityServerDetector unityDetector, InfraServiceRepository infraServiceRepository,
	                    ResourceTypeRepository resourceTypeRepository, ResourceCreditRepository resourceCreditRepository,
	                    CommunityAllocationRepository communityAllocationRepository, SiteAgentService siteAgentService) {
		this.communityRepository = communityRepository;
		this.communityGroupsDAO = communityGroupsDAO;
		this.siteRepository = siteRepository;
		this.siteWebClient = siteWebClient;
		this.usersDAO = usersDAO;
		this.projectRepository = projectRepository;
		this.projectGroupsDAO = projectGroupsDAO;
		this.unityDetector = unityDetector;
		this.infraServiceRepository = infraServiceRepository;
		this.resourceTypeRepository = resourceTypeRepository;
		this.resourceCreditRepository = resourceCreditRepository;
		this.communityAllocationRepository = communityAllocationRepository;
		this.siteAgentService = siteAgentService;
	}

	@Override
	public void run(String... args) throws Exception {
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

			communityId = communityRepository.create(community);
			communityGroupsDAO.create(new CommunityGroup(communityId, community.getName()));
			community2Id = communityRepository.create(community2);
			communityGroupsDAO.create(new CommunityGroup(community2Id, community2.getName()));

			Project project = Project.builder()
				.name("Neuroinforamtics")
				.communityId(communityId)
				.description("Mouse Brain Project")
				.logo(imgHBPFile, "png")
				.acronym("NI")
				.researchField("AI")
				.utcStartTime(LocalDateTime.now())
				.utcEndTime(LocalDateTime.now().plusWeeks(20))
				.leaderId(testAdminId)
				.build();

			Project project2 = Project.builder()
				.name("Brain simulation")
				.communityId(communityId)
				.description("Human Brain Project")
				.logo(imgHBPFile, "png")
				.acronym("BS")
				.researchField("AI")
				.utcStartTime(LocalDateTime.now())
				.utcEndTime(LocalDateTime.now().plusWeeks(10))
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
				.utcStartTime(LocalDateTime.now())
				.utcEndTime(LocalDateTime.now().plusWeeks(30))
				.leaderId(testAdminId)
				.build();

			Project project4 = Project.builder()
				.name("Brain simulation2")
				.communityId(community2Id)
				.description("Human Brain Project")
				.logo(imgHBPFile, "png")
				.acronym("BS")
				.researchField("AI")
				.utcStartTime(LocalDateTime.now())
				.utcEndTime(LocalDateTime.now().plusWeeks(13))
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

			SiteExternalId ciencaExternalId = new SiteExternalId("cin-x");
			SiteExternalId fzjExternalId = new SiteExternalId("fzj-x");
			SiteExternalId bscExternalId = new SiteExternalId("bsc-x");

			String cinecaId = siteRepository.create(cineca, ciencaExternalId);
			String fzjId = siteRepository.create(fzj, fzjExternalId);
			String bscId = siteRepository.create(bsc, bscExternalId);

			siteAgentService.initializeSiteConnection(ciencaExternalId);
			siteAgentService.initializeSiteConnection(fzjExternalId);
			siteAgentService.initializeSiteConnection(bscExternalId);

			siteWebClient.create(Site.builder().id(cinecaId).name(cineca.getName()).build());
			siteWebClient.create(Site.builder().id(fzjId).name(fzj.getName()).build());
			siteWebClient.create(Site.builder().id(bscId).name(bsc.getName()).build());

			InfraService infraServiceCineca = InfraService.builder()
				.name("Virtual Machines")
				.siteId(cinecaId)
				.description("Service for deploying virtual machines")
				.build();
			InfraService infraServiceCineca1 = InfraService.builder()
				.name("Archive Cineca")
				.siteId(cinecaId)
				.description("Archive Cineca")
				.build();
			InfraService infraServiceFzj = InfraService.builder()
				.name("Cluster Fzj")
				.siteId(fzjId)
				.description("Cluster Fzj")
				.build();
			InfraService infraServiceFzj1 = InfraService.builder()
				.name("Archive Fzj")
				.siteId(fzjId)
				.description("Archive Fzj")
				.build();
			InfraService infraServiceBsc = InfraService.builder()
				.name("Cluster Bsc")
				.siteId(bscId)
				.description("Cluster Bsc")
				.build();
			InfraService infraServiceBsc1 = InfraService.builder()
				.name("Archive Bsc")
				.siteId(bscId)
				.description("Archive Bsc")
				.build();

			String serviceCinecaId = infraServiceRepository.create(infraServiceCineca);
			String serviceCinecaId1 = infraServiceRepository.create(infraServiceCineca1);
			String serviceFzjId = infraServiceRepository.create(infraServiceFzj);
			String serviceFzjId1 = infraServiceRepository.create(infraServiceFzj1);
			String serviceBscId = infraServiceRepository.create(infraServiceBsc);
			String serviceBscId1 = infraServiceRepository.create(infraServiceBsc1);

			ResourceType resourceTypeCineca = ResourceType.builder()
				.siteId(cinecaId)
				.serviceId(serviceCinecaId)
				.name("Cineca Vector CPU Time")
				.type(ResourceMeasureType.TIME)
				.unit(ResourceMeasureUnit.H)
				.build();
			ResourceType resourceTypeCineca1 = ResourceType.builder()
				.siteId(cinecaId)
				.serviceId(serviceCinecaId1)
				.name("Cineca Disk Space")
				.type(ResourceMeasureType.DATA)
				.unit(ResourceMeasureUnit.GB)
				.build();
			ResourceType resourceTypeFzj = ResourceType.builder()
				.siteId(fzjId)
				.serviceId(serviceFzjId)
				.name("Fzj Vector GPU Time")
				.type(ResourceMeasureType.TIME)
				.unit(ResourceMeasureUnit.MIN)
				.build();
			ResourceType resourceTypeFzj1 = ResourceType.builder()
				.siteId(fzjId)
				.serviceId(serviceFzjId1)
				.name("Fzj Storage")
				.type(ResourceMeasureType.DATA)
				.unit(ResourceMeasureUnit.PB)
				.build();
			ResourceType resourceTypeBsc = ResourceType.builder()
				.siteId(bscId)
				.serviceId(serviceBscId)
				.name("BSC Vector CPU Time")
				.type(ResourceMeasureType.TIME)
				.unit(ResourceMeasureUnit.DAY)
				.build();
			ResourceType resourceTypeBsc1 = ResourceType.builder()
				.siteId(bscId)
				.serviceId(serviceBscId1)
				.name("BSC Disk")
				.type(ResourceMeasureType.DATA)
				.unit(ResourceMeasureUnit.KB)
				.build();

			String resourceTypeCinecaId = resourceTypeRepository.create(resourceTypeCineca);
			String resourceTypeCinecaId1 = resourceTypeRepository.create(resourceTypeCineca1);
			String resourceTypeFzjId = resourceTypeRepository.create(resourceTypeFzj);
			String resourceTypeFzjId1 = resourceTypeRepository.create(resourceTypeFzj1);
			String resourceTypeBscId = resourceTypeRepository.create(resourceTypeBsc);
			String resourceTypeBscId1 = resourceTypeRepository.create(resourceTypeBsc1);

			ResourceCredit resourceCreditCineca = ResourceCredit.builder()
				.siteId(cinecaId)
				.resourceTypeId(resourceTypeCinecaId)
				.name("First Cineca pool")
				.amount(new BigDecimal(100))
				.utcStartTime(LocalDateTime.of(2021, 2, 11, 16, 11))
				.utcEndTime(LocalDateTime.of(2025, 4, 11, 16, 11))
				.build();
			ResourceCredit resourceCreditCineca1 = ResourceCredit.builder()
				.siteId(cinecaId)
				.resourceTypeId(resourceTypeCinecaId1)
				.name("Second Cineca pool")
				.amount(new BigDecimal(546))
				.utcStartTime(LocalDateTime.of(2021, 3, 9, 1, 22))
				.utcEndTime(LocalDateTime.of(2024, 5, 3, 6, 32))
				.build();
			ResourceCredit resourceCreditFzj = ResourceCredit.builder()
				.siteId(fzjId)
				.resourceTypeId(resourceTypeFzjId)
				.name("First Fzj pool")
				.amount(new BigDecimal(643))
				.utcStartTime(LocalDateTime.of(2021, 2, 7, 4, 22))
				.utcEndTime(LocalDateTime.of(2021, 4, 9, 7, 32))
				.build();
			ResourceCredit resourceCreditFzj1 = ResourceCredit.builder()
				.siteId(fzjId)
				.resourceTypeId(resourceTypeFzjId1)
				.name("Second Fzj pool")
				.amount(new BigDecimal(754))
				.utcStartTime(LocalDateTime.of(2021, 4, 23, 3, 22))
				.utcEndTime(LocalDateTime.of(2024, 8, 12, 5, 32))
				.build();
			ResourceCredit resourceCreditBsc = ResourceCredit.builder()
				.siteId(bscId)
				.resourceTypeId(resourceTypeBscId)
				.name("First Bsc pool")
				.amount(new BigDecimal(112))
				.utcStartTime(LocalDateTime.of(2021, 3, 11, 6, 22))
				.utcEndTime(LocalDateTime.of(2024, 6, 22, 7, 32))
				.build();
			ResourceCredit resourceCreditBsc1 = ResourceCredit.builder()
				.siteId(bscId)
				.resourceTypeId(resourceTypeBscId1)
				.name("Second Bsc pool")
				.amount(new BigDecimal(9875))
				.utcStartTime(LocalDateTime.of(2021, 10, 22, 11, 22))
				.utcEndTime(LocalDateTime.of(2024, 12, 8, 17, 32))
				.build();

			resourceCreditRepository.create(resourceCreditCineca);
			resourceCreditRepository.create(resourceCreditCineca1);
			resourceCreditRepository.create(resourceCreditFzj);
			String resourceCreditFzjId1 = resourceCreditRepository.create(resourceCreditFzj1);
			resourceCreditRepository.create(resourceCreditBsc);
			String resourceCreditBscId1 = resourceCreditRepository.create(resourceCreditBsc1);

			CommunityAllocation communityAllocation = CommunityAllocation.builder()
				.communityId(communityId)
				.resourceCreditId(resourceCreditBscId1)
				.name("First Allocation")
				.amount(new BigDecimal(1000))
				.build();
			CommunityAllocation communityAllocation1 = CommunityAllocation.builder()
				.communityId(community2Id)
				.resourceCreditId(resourceCreditFzjId1)
				.name("Second Allocation")
				.amount(new BigDecimal(500))
				.build();

			communityAllocationRepository.create(communityAllocation);
			communityAllocationRepository.create(communityAllocation1);
		}
	}
}
