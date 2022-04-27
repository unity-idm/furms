/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.server;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityGroup;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.policy_documents.PolicyContentType;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.PolicyWorkflow;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus;
import io.imunity.furms.domain.project_installation.ProjectInstallationJob;
import io.imunity.furms.domain.project_installation.ProjectInstallationStatus;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectGroup;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_credits.ResourceCreditId;
import io.imunity.furms.domain.resource_types.ResourceMeasureType;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.services.InfraServiceId;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.site.api.site_agent.SiteAgentService;
import io.imunity.furms.spi.communites.CommunityGroupsDAO;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.project_allocation_installation.ProjectAllocationInstallationRepository;
import io.imunity.furms.spi.project_installation.ProjectOperationRepository;
import io.imunity.furms.spi.projects.ProjectGroupsDAO;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import io.imunity.furms.spi.services.InfraServiceRepository;
import io.imunity.furms.spi.sites.SiteGroupDAO;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static java.util.function.Function.identity;

@Component
@Profile("demo-data-provisioning")
class DemoDataInitializer implements CommandLineRunner {
	private final CommunityRepository communityRepository;
	private final CommunityGroupsDAO communityGroupsDAO;

	private final SiteRepository siteRepository;
	private final SiteGroupDAO siteGroupDAO;
	private final UsersDAO usersDAO;
	private final ProjectRepository projectRepository;
	private final ProjectGroupsDAO projectGroupsDAO;
	private final UnityServerDetector unityDetector;
	private final InfraServiceRepository infraServiceRepository;
	private final ResourceTypeRepository resourceTypeRepository;
	private final ResourceCreditRepository resourceCreditRepository;
	private final CommunityAllocationRepository communityAllocationRepository;
	private final SiteAgentService siteAgentService;
	private final PolicyDocumentRepository policyDocumentRepository;
	private final ProjectAllocationRepository projectAllocationRepository;
	private final ProjectAllocationInstallationRepository projectAllocationInstallationRepository;
	private final ProjectOperationRepository projectOperationRepository;
	private final ResourceUsageRepository resourceUsageRepository;

	private CommunityId communityId;
	private CommunityId community2Id;

	private ProjectId projectId;
	private ProjectId project2Id;
	private ProjectId project3Id;
	private ProjectId project4Id;

	DemoDataInitializer(CommunityRepository communityRepository, CommunityGroupsDAO communityGroupsDAO,
	                    SiteRepository siteRepository, SiteGroupDAO siteGroupDAO, UsersDAO usersDAO,
	                    ProjectRepository projectRepository, ProjectGroupsDAO projectGroupsDAO,
	                    UnityServerDetector unityDetector, InfraServiceRepository infraServiceRepository,
	                    ResourceTypeRepository resourceTypeRepository, ResourceCreditRepository resourceCreditRepository,
	                    CommunityAllocationRepository communityAllocationRepository, SiteAgentService siteAgentService,
	                    PolicyDocumentRepository policyDocumentRepository, ProjectAllocationRepository projectAllocationRepository,
	                    ProjectAllocationInstallationRepository projectAllocationInstallationRepository,
	                    ProjectOperationRepository projectOperationRepository, ResourceUsageRepository resourceUsageRepository) {
		this.communityRepository = communityRepository;
		this.communityGroupsDAO = communityGroupsDAO;
		this.siteRepository = siteRepository;
		this.siteGroupDAO = siteGroupDAO;
		this.usersDAO = usersDAO;
		this.projectRepository = projectRepository;
		this.projectGroupsDAO = projectGroupsDAO;
		this.unityDetector = unityDetector;
		this.infraServiceRepository = infraServiceRepository;
		this.resourceTypeRepository = resourceTypeRepository;
		this.resourceCreditRepository = resourceCreditRepository;
		this.communityAllocationRepository = communityAllocationRepository;
		this.siteAgentService = siteAgentService;
		this.policyDocumentRepository = policyDocumentRepository;
		this.projectAllocationRepository = projectAllocationRepository;
		this.projectAllocationInstallationRepository = projectAllocationInstallationRepository;
		this.projectOperationRepository = projectOperationRepository;
		this.resourceUsageRepository = resourceUsageRepository;
	}

	@Override
	public void run(String... args) throws Exception {
		unityDetector.waitForUnityToStartUp();
		initCommunitiesAndProjects();
		initSites();
	}

	private void initCommunitiesAndProjects() throws IOException {

		PersistentId testAdminId = getPersistentId();
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
			communityGroupsDAO.addAdmin(communityId, testAdminId);

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

			projectId = projectRepository.create(project);
			projectGroupsDAO.create(new ProjectGroup(projectId, project.getName(), communityId));
			project2Id = projectRepository.create(project2);
			projectGroupsDAO.create(new ProjectGroup(project2Id, project2.getName(), communityId));

			projectGroupsDAO.addProjectUser(communityId, projectId, testAdminId, Role.PROJECT_ADMIN);
			projectGroupsDAO.addProjectUser(communityId, project2Id, testAdminId, Role.PROJECT_ADMIN);

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

			project3Id = projectRepository.create(project3);
			projectGroupsDAO.create(new ProjectGroup(project3Id, project3.getName(), community2Id));
			project4Id = projectRepository.create(project4);
			projectGroupsDAO.create(new ProjectGroup(project4Id, project4.getName(), community2Id));

			projectGroupsDAO.addProjectUser(community2Id, project3Id, testAdminId, Role.PROJECT_ADMIN);
			projectGroupsDAO.addProjectUser(community2Id, project4Id, testAdminId, Role.PROJECT_ADMIN);
		}
	}

	private PersistentId getPersistentId() {
		return usersDAO.getAllUsers().stream()
			.filter(user -> user.email.equals("admin@not-existing-1qaz.example.com"))
			.map(user -> user.id)
			.findAny()
			.flatMap(identity())
			.orElse(null);
	}

	private void initSites() {
		if (siteRepository.findAll().isEmpty()) {
			PersistentId testAdminId = getPersistentId();

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

			SiteId cinecaId = siteRepository.create(cineca, ciencaExternalId);
			SiteId fzjId = siteRepository.create(fzj, fzjExternalId);
			SiteId bscId = siteRepository.create(bsc, bscExternalId);

			siteAgentService.initializeSiteConnection(ciencaExternalId);
			siteAgentService.initializeSiteConnection(fzjExternalId);
			siteAgentService.initializeSiteConnection(bscExternalId);

			siteGroupDAO.create(Site.builder().id(cinecaId).name(cineca.getName()).build());
			siteGroupDAO.create(Site.builder().id(fzjId).name(fzj.getName()).build());
			siteGroupDAO.create(Site.builder().id(bscId).name(bsc.getName()).build());

			siteGroupDAO.addSiteUser(cinecaId, testAdminId, Role.SITE_ADMIN);

			PolicyDocument policyDocument = PolicyDocument.builder()
				.name("Cineca site policy")
				.workflow(PolicyWorkflow.WEB_BASED)
				.contentType(PolicyContentType.EMBEDDED)
				.wysiwygText("<div><p>TEXT</p></div>")
				.revision(1)
				.siteId(cinecaId)
				.build();

			PolicyDocument policyDocument1 = PolicyDocument.builder()
				.name("Service policy")
				.workflow(PolicyWorkflow.PAPER_BASED)
				.contentType(PolicyContentType.EMBEDDED)
				.wysiwygText("<div><p>TEXT</p></div>")
				.revision(1)
				.siteId(cinecaId)
				.build();

			PolicyId policyId = policyDocumentRepository.create(policyDocument);
			PolicyId policyId1 = policyDocumentRepository.create(policyDocument1);

			Site updateCineca = Site.builder()
				.id(cinecaId)
				.policyId(policyId)
				.name("CINECA")
				.build();
			siteRepository.update(updateCineca);

			InfraService infraServiceCineca = InfraService.builder()
				.name("Virtual Machines")
				.siteId(cinecaId)
				.description("Service for deploying virtual machines")
				.policyId(policyId1)
				.build();
			InfraService infraServiceCineca1 = InfraService.builder()
				.name("Archive Cineca")
				.siteId(cinecaId)
				.description("Archive Cineca")
				.policyId(policyId1)
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

			InfraServiceId serviceCinecaId = infraServiceRepository.create(infraServiceCineca);
			InfraServiceId serviceCinecaId1 = infraServiceRepository.create(infraServiceCineca1);
			InfraServiceId serviceFzjId = infraServiceRepository.create(infraServiceFzj);
			InfraServiceId serviceFzjId1 = infraServiceRepository.create(infraServiceFzj1);
			InfraServiceId serviceBscId = infraServiceRepository.create(infraServiceBsc);
			InfraServiceId serviceBscId1 = infraServiceRepository.create(infraServiceBsc1);

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

			ResourceTypeId resourceTypeCinecaId = resourceTypeRepository.create(resourceTypeCineca);
			ResourceTypeId resourceTypeCinecaId1 = resourceTypeRepository.create(resourceTypeCineca1);
			ResourceTypeId resourceTypeFzjId = resourceTypeRepository.create(resourceTypeFzj);
			ResourceTypeId resourceTypeFzjId1 = resourceTypeRepository.create(resourceTypeFzj1);
			ResourceTypeId resourceTypeBscId = resourceTypeRepository.create(resourceTypeBsc);
			ResourceTypeId resourceTypeBscId1 = resourceTypeRepository.create(resourceTypeBsc1);

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

			ResourceCreditId resourceCreditCinecaId1 = resourceCreditRepository.create(resourceCreditCineca);
			resourceCreditRepository.create(resourceCreditCineca1);
			resourceCreditRepository.create(resourceCreditFzj);
			ResourceCreditId resourceCreditFzjId1 = resourceCreditRepository.create(resourceCreditFzj1);
			resourceCreditRepository.create(resourceCreditBsc);
			ResourceCreditId resourceCreditBscId1 = resourceCreditRepository.create(resourceCreditBsc1);

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
			CommunityAllocation communityAllocation2 = CommunityAllocation.builder()
				.communityId(communityId)
				.resourceCreditId(resourceCreditCinecaId1)
				.name("HBP Cineca Allocation")
				.amount(new BigDecimal(50))
				.build();
			CommunityAllocation communityAllocation3 = CommunityAllocation.builder()
				.communityId(communityId)
				.resourceCreditId(resourceCreditCinecaId1)
				.name("HBP Cineca Allocation 2")
				.amount(new BigDecimal(50))
				.build();

			communityAllocationRepository.create(communityAllocation);
			communityAllocationRepository.create(communityAllocation1);
			CommunityAllocationId communityAllocationId = communityAllocationRepository.create(communityAllocation2);
			CommunityAllocationId communityAllocationId1 = communityAllocationRepository.create(communityAllocation3);

			ProjectAllocation projectAllocation = ProjectAllocation.builder()
				.projectId(projectId)
				.name("Neuroinforamtics Cineca Allocation")
				.amount(new BigDecimal(20))
				.communityAllocationId(communityAllocationId)
				.build();

			ProjectAllocationId projectAllocationId = projectAllocationRepository.create(projectAllocation);

			ProjectAllocationInstallation projectAllocationInstallation = ProjectAllocationInstallation.builder()
				.projectAllocationId(projectAllocationId)
				.siteId(cinecaId)
				.status(ProjectAllocationInstallationStatus.ACKNOWLEDGED)
				.correlationId(new CorrelationId(UUID.randomUUID().toString()))
				.build();

			projectAllocationInstallationRepository.create(projectAllocationInstallation);

			ProjectAllocationChunk projectAllocationChunk = ProjectAllocationChunk.builder()
				.amount(new BigDecimal(5))
				.projectAllocationId(projectAllocationId)
				.chunkId("1")
				.validFrom(LocalDateTime.now().minusMonths(2))
				.validTo(LocalDateTime.now().plusDays(20))
				.receivedTime(LocalDateTime.now().minusMonths(2))
				.build();
			projectAllocationInstallationRepository.create(projectAllocationChunk);

			ProjectAllocationChunk projectAllocationChunk1 = ProjectAllocationChunk.builder()
				.amount(new BigDecimal(3))
				.projectAllocationId(projectAllocationId)
				.chunkId("2")
				.validFrom(LocalDateTime.now().minusMonths(1))
				.validTo(LocalDateTime.now().plusDays(25))
				.receivedTime(LocalDateTime.now().minusMonths(1))
				.build();
			projectAllocationInstallationRepository.create(projectAllocationChunk1);

			ProjectAllocationChunk projectAllocationChunk2 = ProjectAllocationChunk.builder()
				.amount(new BigDecimal(4))
				.projectAllocationId(projectAllocationId)
				.chunkId("3")
				.validFrom(LocalDateTime.now().minusDays(15))
				.validTo(LocalDateTime.now().plusDays(27))
				.receivedTime(LocalDateTime.now().minusDays(15))
				.build();
			projectAllocationInstallationRepository.create(projectAllocationChunk2);

			ProjectAllocationChunk projectAllocationChunk3 = ProjectAllocationChunk.builder()
				.amount(new BigDecimal(3))
				.projectAllocationId(projectAllocationId)
				.chunkId("3")
				.validFrom(LocalDateTime.now().minusDays(2))
				.validTo(LocalDateTime.now().plusDays(35))
				.receivedTime(LocalDateTime.now().minusDays(2))
				.build();
			projectAllocationInstallationRepository.create(projectAllocationChunk3);

			ProjectAllocationResolved projectAllocationResolved = projectAllocationRepository.findByIdWithRelatedObjects(projectAllocationId).get();
			resourceUsageRepository.create(ResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId)
					.cumulativeConsumption(new BigDecimal(1))
					.probedAt(LocalDateTime.now().minusDays(59))
					.build(),
				projectAllocationResolved
			);
			resourceUsageRepository.create(ResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId)
					.cumulativeConsumption(new BigDecimal(2))
					.probedAt(LocalDateTime.now().minusDays(50))
					.build(),
				projectAllocationResolved
			);
			resourceUsageRepository.create(ResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId)
					.cumulativeConsumption(new BigDecimal(5))
					.probedAt(LocalDateTime.now().minusDays(40))
					.build(),
				projectAllocationResolved
			);
			resourceUsageRepository.create(ResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId)
					.cumulativeConsumption(new BigDecimal(8))
					.probedAt(LocalDateTime.now().minusDays(25))
					.build(),
				projectAllocationResolved
			);
			resourceUsageRepository.create(ResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId)
					.cumulativeConsumption(new BigDecimal(10))
					.probedAt(LocalDateTime.now().minusDays(15))
					.build(),
				projectAllocationResolved
			);
			resourceUsageRepository.create(ResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId)
					.cumulativeConsumption(new BigDecimal(13))
					.probedAt(LocalDateTime.now().minusDays(5))
					.build(),
				projectAllocationResolved
			);
			resourceUsageRepository.create(ResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId)
					.cumulativeConsumption(new BigDecimal(16))
					.probedAt(LocalDateTime.now().minusDays(1))
					.build(),
				projectAllocationResolved
			);

			resourceUsageRepository.create(
				UserResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId)
					.cumulativeConsumption(new BigDecimal(1))
					.fenixUserId(new FenixUserId("usr1@email.com"))
					.consumedUntil(LocalDateTime.now().minusDays(59))
					.build()
			);
			resourceUsageRepository.create(
				UserResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId)
					.cumulativeConsumption(new BigDecimal(1))
					.fenixUserId(new FenixUserId("usr2@email.com"))
					.consumedUntil(LocalDateTime.now().minusDays(54))
					.build()
			);
			resourceUsageRepository.create(
				UserResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId)
					.cumulativeConsumption(new BigDecimal(2))
					.fenixUserId(new FenixUserId("usr2@email.com"))
					.consumedUntil(LocalDateTime.now().minusDays(49))
					.build()
			);
			resourceUsageRepository.create(
				UserResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId)
					.cumulativeConsumption(new BigDecimal(2))
					.fenixUserId(new FenixUserId("usr1@email.com"))
					.consumedUntil(LocalDateTime.now().minusDays(45))
					.build()
			);
			resourceUsageRepository.create(
				UserResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId)
					.cumulativeConsumption(new BigDecimal(1))
					.fenixUserId(new FenixUserId("usr3@email.com"))
					.consumedUntil(LocalDateTime.now().minusDays(42))
					.build()
			);
			resourceUsageRepository.create(
				UserResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId)
					.cumulativeConsumption(new BigDecimal(4))
					.fenixUserId(new FenixUserId("usr3@email.com"))
					.consumedUntil(LocalDateTime.now().minusDays(28))
					.build()
			);
			resourceUsageRepository.create(
				UserResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId)
					.cumulativeConsumption(new BigDecimal(3))
					.fenixUserId(new FenixUserId("usr1@email.com"))
					.consumedUntil(LocalDateTime.now().minusDays(17))
					.build()
			);
			resourceUsageRepository.create(
				UserResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId)
					.cumulativeConsumption(new BigDecimal(3))
					.fenixUserId(new FenixUserId("usr2@email.com"))
					.consumedUntil(LocalDateTime.now().minusDays(16))
					.build()
			);
			resourceUsageRepository.create(
				UserResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId)
					.cumulativeConsumption(new BigDecimal(5))
					.fenixUserId(new FenixUserId("usr1@email.com"))
					.consumedUntil(LocalDateTime.now().minusDays(10))
					.build()
			);
			resourceUsageRepository.create(
				UserResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId)
					.cumulativeConsumption(new BigDecimal(5))
					.fenixUserId(new FenixUserId("usr3@email.com"))
					.consumedUntil(LocalDateTime.now().minusDays(7))
					.build()
			);
			resourceUsageRepository.create(
				UserResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId)
					.cumulativeConsumption(new BigDecimal(4))
					.fenixUserId(new FenixUserId("usr2@email.com"))
					.consumedUntil(LocalDateTime.now().minusDays(4))
					.build()
			);
			resourceUsageRepository.create(
				UserResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId)
					.cumulativeConsumption(new BigDecimal(5))
					.fenixUserId(new FenixUserId("usr2@email.com"))
					.consumedUntil(LocalDateTime.now().minusDays(2))
					.build()
			);

			ProjectAllocation projectAllocation1 = ProjectAllocation.builder()
				.projectId(projectId)
				.name("Neuroinforamtics Cineca Allocation 2")
				.amount(new BigDecimal(20))
				.communityAllocationId(communityAllocationId)
				.build();

			ProjectAllocationId projectAllocationId1 = projectAllocationRepository.create(projectAllocation1);

			ProjectAllocationInstallation projectAllocationInstallation1 = ProjectAllocationInstallation.builder()
				.projectAllocationId(projectAllocationId1)
				.siteId(cinecaId)
				.status(ProjectAllocationInstallationStatus.ACKNOWLEDGED)
				.correlationId(new CorrelationId(UUID.randomUUID().toString()))
				.build();

			projectAllocationInstallationRepository.create(projectAllocationInstallation1);

			ProjectAllocationChunk projectAllocationChunk10 = ProjectAllocationChunk.builder()
				.amount(new BigDecimal(5))
				.projectAllocationId(projectAllocationId1)
				.chunkId("1")
				.validFrom(LocalDateTime.now().minusMonths(2))
				.validTo(LocalDateTime.now().plusDays(20))
				.receivedTime(LocalDateTime.now().minusMonths(2))
				.build();
			projectAllocationInstallationRepository.create(projectAllocationChunk10);

			ProjectAllocationChunk projectAllocationChunk11 = ProjectAllocationChunk.builder()
				.amount(new BigDecimal(3))
				.projectAllocationId(projectAllocationId1)
				.chunkId("2")
				.validFrom(LocalDateTime.now().minusMonths(1))
				.validTo(LocalDateTime.now().plusDays(25))
				.receivedTime(LocalDateTime.now().minusMonths(1))
				.build();
			projectAllocationInstallationRepository.create(projectAllocationChunk11);

			ProjectAllocationChunk projectAllocationChunk12 = ProjectAllocationChunk.builder()
				.amount(new BigDecimal(4))
				.projectAllocationId(projectAllocationId1)
				.chunkId("3")
				.validFrom(LocalDateTime.now().minusDays(15))
				.validTo(LocalDateTime.now().plusDays(27))
				.receivedTime(LocalDateTime.now().minusDays(15))
				.build();
			projectAllocationInstallationRepository.create(projectAllocationChunk12);

			ProjectAllocationChunk projectAllocationChunk13 = ProjectAllocationChunk.builder()
				.amount(new BigDecimal(3))
				.projectAllocationId(projectAllocationId1)
				.chunkId("3")
				.validFrom(LocalDateTime.now().minusDays(2))
				.validTo(LocalDateTime.now().plusDays(35))
				.receivedTime(LocalDateTime.now().minusDays(2))
				.build();
			projectAllocationInstallationRepository.create(projectAllocationChunk13);

			ProjectAllocationResolved projectAllocationResolved1 = projectAllocationRepository.findByIdWithRelatedObjects(projectAllocationId1).get();
			resourceUsageRepository.create(ResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId1)
					.cumulativeConsumption(new BigDecimal(1))
					.probedAt(LocalDateTime.now().minusDays(59))
					.build(),
				projectAllocationResolved1
			);
			resourceUsageRepository.create(ResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId1)
					.cumulativeConsumption(new BigDecimal(2))
					.probedAt(LocalDateTime.now().minusDays(50))
					.build(),
				projectAllocationResolved1
			);
			resourceUsageRepository.create(ResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId1)
					.cumulativeConsumption(new BigDecimal(5))
					.probedAt(LocalDateTime.now().minusDays(40))
					.build(),
				projectAllocationResolved1
			);
			resourceUsageRepository.create(ResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId1)
					.cumulativeConsumption(new BigDecimal(8))
					.probedAt(LocalDateTime.now().minusDays(25))
					.build(),
				projectAllocationResolved1
			);
			resourceUsageRepository.create(ResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId1)
					.cumulativeConsumption(new BigDecimal(10))
					.probedAt(LocalDateTime.now().minusDays(15))
					.build(),
				projectAllocationResolved1
			);
			resourceUsageRepository.create(ResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId1)
					.cumulativeConsumption(new BigDecimal(13))
					.probedAt(LocalDateTime.now().minusDays(5))
					.build(),
				projectAllocationResolved1
			);
			resourceUsageRepository.create(ResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId1)
					.cumulativeConsumption(new BigDecimal(16))
					.probedAt(LocalDateTime.now().minusDays(1))
					.build(),
				projectAllocationResolved1
			);

			resourceUsageRepository.create(
				UserResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId1)
					.cumulativeConsumption(new BigDecimal(1))
					.fenixUserId(new FenixUserId("usr1@email.com"))
					.consumedUntil(LocalDateTime.now().minusDays(59))
					.build()
			);
			resourceUsageRepository.create(
				UserResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId1)
					.cumulativeConsumption(new BigDecimal(1))
					.fenixUserId(new FenixUserId("usr2@email.com"))
					.consumedUntil(LocalDateTime.now().minusDays(54))
					.build()
			);
			resourceUsageRepository.create(
				UserResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId1)
					.cumulativeConsumption(new BigDecimal(2))
					.fenixUserId(new FenixUserId("usr2@email.com"))
					.consumedUntil(LocalDateTime.now().minusDays(49))
					.build()
			);
			resourceUsageRepository.create(
				UserResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId1)
					.cumulativeConsumption(new BigDecimal(2))
					.fenixUserId(new FenixUserId("usr1@email.com"))
					.consumedUntil(LocalDateTime.now().minusDays(45))
					.build()
			);
			resourceUsageRepository.create(
				UserResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId1)
					.cumulativeConsumption(new BigDecimal(1))
					.fenixUserId(new FenixUserId("usr3@email.com"))
					.consumedUntil(LocalDateTime.now().minusDays(42))
					.build()
			);
			resourceUsageRepository.create(
				UserResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId1)
					.cumulativeConsumption(new BigDecimal(4))
					.fenixUserId(new FenixUserId("usr3@email.com"))
					.consumedUntil(LocalDateTime.now().minusDays(28))
					.build()
			);
			resourceUsageRepository.create(
				UserResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId1)
					.cumulativeConsumption(new BigDecimal(3))
					.fenixUserId(new FenixUserId("usr1@email.com"))
					.consumedUntil(LocalDateTime.now().minusDays(17))
					.build()
			);
			resourceUsageRepository.create(
				UserResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId1)
					.cumulativeConsumption(new BigDecimal(3))
					.fenixUserId(new FenixUserId("usr2@email.com"))
					.consumedUntil(LocalDateTime.now().minusDays(16))
					.build()
			);
			resourceUsageRepository.create(
				UserResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId1)
					.cumulativeConsumption(new BigDecimal(5))
					.fenixUserId(new FenixUserId("usr1@email.com"))
					.consumedUntil(LocalDateTime.now().minusDays(10))
					.build()
			);
			resourceUsageRepository.create(
				UserResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId1)
					.cumulativeConsumption(new BigDecimal(5))
					.fenixUserId(new FenixUserId("usr3@email.com"))
					.consumedUntil(LocalDateTime.now().minusDays(7))
					.build()
			);
			resourceUsageRepository.create(
				UserResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId1)
					.cumulativeConsumption(new BigDecimal(4))
					.fenixUserId(new FenixUserId("usr2@email.com"))
					.consumedUntil(LocalDateTime.now().minusDays(4))
					.build()
			);
			resourceUsageRepository.create(
				UserResourceUsage.builder()
					.projectId(projectId)
					.projectAllocationId(projectAllocationId1)
					.cumulativeConsumption(new BigDecimal(5))
					.fenixUserId(new FenixUserId("usr2@email.com"))
					.consumedUntil(LocalDateTime.now().minusDays(2))
					.build()
			);

			ProjectInstallationJob projectInstallationJob = ProjectInstallationJob.builder()
				.gid("gid")
				.siteId(cinecaId)
				.projectId(projectId)
				.correlationId(CorrelationId.randomID())
				.status(ProjectInstallationStatus.INSTALLED)
				.build();
			projectOperationRepository.createOrUpdate(projectInstallationJob);

			ProjectInstallationJob projectInstallationJob2 = ProjectInstallationJob.builder()
				.gid("gid")
				.siteId(cinecaId)
				.projectId(project2Id)
				.correlationId(CorrelationId.randomID())
				.status(ProjectInstallationStatus.INSTALLED)
				.build();
			projectOperationRepository.createOrUpdate(projectInstallationJob2);

			ProjectInstallationJob projectInstallationJob3 = ProjectInstallationJob.builder()
				.gid("gid")
				.siteId(cinecaId)
				.projectId(project3Id)
				.correlationId(CorrelationId.randomID())
				.status(ProjectInstallationStatus.INSTALLED)
				.build();
			projectOperationRepository.createOrUpdate(projectInstallationJob3);

			ProjectInstallationJob projectInstallationJob4 = ProjectInstallationJob.builder()
				.gid("gid")
				.siteId(cinecaId)
				.projectId(project4Id)
				.correlationId(CorrelationId.randomID())
				.status(ProjectInstallationStatus.INSTALLED)
				.build();
			projectOperationRepository.createOrUpdate(projectInstallationJob4);
		}
	}
}
