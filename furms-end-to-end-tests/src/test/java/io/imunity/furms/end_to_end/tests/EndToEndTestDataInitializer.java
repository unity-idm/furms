/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.end_to_end.tests;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityGroup;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.images.FurmsImage;
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
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static io.imunity.furms.domain.resource_types.ResourceMeasureType.DATA;
import static io.imunity.furms.domain.resource_types.ResourceMeasureType.TIME;
import static io.imunity.furms.domain.resource_types.ResourceMeasureUnit.H;
import static io.imunity.furms.domain.resource_types.ResourceMeasureUnit.PB;
import static java.util.function.Function.identity;

@Component
class EndToEndTestDataInitializer implements CommandLineRunner {
	private final CommunityRepository communityRepository;
	private final CommunityGroupsDAO communityGroupsDAO;

	private final SiteRepository siteRepository;
	private final SiteGroupDAO siteGroupDAO;
	private final UsersDAO usersDAO;
	private final ProjectRepository projectRepository;
	private final ProjectGroupsDAO projectGroupsDAO;
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

	EndToEndTestDataInitializer(CommunityRepository communityRepository, CommunityGroupsDAO communityGroupsDAO,
	                            SiteRepository siteRepository, SiteGroupDAO siteGroupDAO, UsersDAO usersDAO,
	                            ProjectRepository projectRepository, ProjectGroupsDAO projectGroupsDAO,
	                            InfraServiceRepository infraServiceRepository,
	                            ResourceTypeRepository resourceTypeRepository,
	                            ResourceCreditRepository resourceCreditRepository,
	                            CommunityAllocationRepository communityAllocationRepository,
	                            SiteAgentService siteAgentService,
	                            PolicyDocumentRepository policyDocumentRepository,
	                            ProjectAllocationRepository projectAllocationRepository,
	                            ProjectAllocationInstallationRepository projectAllocationInstallationRepository,
	                            ProjectOperationRepository projectOperationRepository,
	                            ResourceUsageRepository resourceUsageRepository) {
		this.communityRepository = communityRepository;
		this.communityGroupsDAO = communityGroupsDAO;
		this.siteRepository = siteRepository;
		this.siteGroupDAO = siteGroupDAO;
		this.usersDAO = usersDAO;
		this.projectRepository = projectRepository;
		this.projectGroupsDAO = projectGroupsDAO;
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
	public void run(String... args) {
		initCommunitiesAndProjects();
		initSitesWithContent();
	}

	private void initCommunitiesAndProjects() {
		PersistentId testAdminId = getPersistentId();
		initCommunities(testAdminId);
		initProjects(testAdminId);
	}

	private void initCommunities(PersistentId testAdminId) {
		Community community = Community.builder()
			.name("HBP")
			.description("Human Brain Project")
			.logo(FurmsImage.empty())
			.build();

		Community community2 = Community.builder()
			.name("PRACE")
			.description("Partnership for Advance Computing")
			.logo(FurmsImage.empty())
			.build();

		communityId = communityRepository.create(community);
		communityGroupsDAO.create(new CommunityGroup(communityId, community.getName()));
		community2Id = communityRepository.create(community2);
		communityGroupsDAO.create(new CommunityGroup(community2Id, community2.getName()));
		communityGroupsDAO.addAdmin(communityId, testAdminId);
	}

	private void initProjects(PersistentId testAdminId) {
		Project project = Project.builder()
			.name("Neuroinforamtics")
			.communityId(communityId)
			.description("Mouse Brain Project")
			.acronym("NI")
			.researchField("AI")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now().plusWeeks(20))
			.logo(FurmsImage.empty())
			.leaderId(testAdminId)
			.build();

		Project project2 = Project.builder()
			.name("Brain simulation")
			.communityId(communityId)
			.description("Human Brain Project")
			.acronym("BS")
			.researchField("AI")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now().plusWeeks(10))
			.logo(FurmsImage.empty())
			.build();

		projectId = projectRepository.create(project);
		projectGroupsDAO.create(new ProjectGroup(projectId, project.getName(), communityId));
		project2Id = projectRepository.create(project2);
		projectGroupsDAO.create(new ProjectGroup(project2Id, project2.getName(), communityId));

		projectGroupsDAO.addProjectUser(communityId, projectId, testAdminId, Role.PROJECT_ADMIN);

		Project project3 = Project.builder()
			.name("Neuroinforamtics2")
			.communityId(community2Id)
			.description("Mouse Brain Project")
			.acronym("NI")
			.researchField("AI")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now().plusWeeks(30))
			.logo(FurmsImage.empty())
			.build();

		Project project4 = Project.builder()
			.name("Brain simulation2")
			.communityId(community2Id)
			.description("Human Brain Project")
			.acronym("BS")
			.researchField("AI")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now().plusWeeks(13))
			.logo(FurmsImage.empty())
			.build();

		project3Id = projectRepository.create(project3);
		projectGroupsDAO.create(new ProjectGroup(project3Id, project3.getName(), community2Id));
		project4Id = projectRepository.create(project4);
		projectGroupsDAO.create(new ProjectGroup(project4Id, project4.getName(), community2Id));
	}

	private PersistentId getPersistentId() {
		return usersDAO.getAllUsers().stream()
			.filter(user -> user.email.equals("admin@not-existing-1qaz.example.com"))
			.map(user -> user.id)
			.findAny()
			.flatMap(identity())
			.orElse(null);
	}

	private void initSitesWithContent() {
		PersistentId testAdminId = getPersistentId();

		SiteId cinecaId = createSite("CINECA", "cin-x");
		SiteId fzjId = createSite("FZJ", "fzj-x");

		siteGroupDAO.addSiteUser(cinecaId, testAdminId, Role.SITE_ADMIN);

		PolicyId policyId = createPolicy("Cineca site policy", PolicyWorkflow.WEB_BASED, cinecaId);
		PolicyId policyId1 = createPolicy("Service policy", PolicyWorkflow.PAPER_BASED, cinecaId);

		addPolicyToSite(cinecaId, policyId);

		InfraServiceId serviceCinecaId = createInfraService(cinecaId, policyId1, "Virtual Machines", "Service for deploying virtual machines");
		InfraServiceId serviceFzjId1 = createInfraService(fzjId, PolicyId.empty(), "Archive Fzj", "Archive Fzj");

		ResourceTypeId resourceTypeCinecaId = createResourceType(cinecaId, serviceCinecaId, "Cineca Vector CPU Time", TIME, H);
		ResourceTypeId resourceTypeFzjId1 = createResourceType(fzjId, serviceFzjId1, "Fzj Storage", DATA, PB);

		ResourceCreditId resourceCreditCinecaId1 = createResourceCredit(cinecaId, resourceTypeCinecaId, "First Cineca " +
			"pool", 100, 2, 11, 11, 11, 11, 16, 16, 2025, 4);
		ResourceCreditId resourceCreditFzjId1 = createResourceCredit(fzjId, resourceTypeFzjId1, "Second Fzj pool", 754
			, 4, 23, 22, 12, 32, 3, 5, 2024, 8);

		CommunityAllocationId communityAllocationId = createCommunityAllocation(communityId, resourceCreditCinecaId1,
			"HBP Cineca Allocation", 50);
		createCommunityAllocation(communityId, resourceCreditCinecaId1, "HBP Cineca Allocation 2", 50);
		createCommunityAllocation(community2Id, resourceCreditFzjId1, "Second Allocation", 500);

		ProjectAllocationId projectAllocationId = createProjectAllocation("Neuroinforamtics Cineca Allocation",
			communityAllocationId, cinecaId);
		createProjectAllocationChunks(projectAllocationId);
		createResourceUsages(projectAllocationId, projectAllocationRepository.findByIdWithRelatedObjects(projectAllocationId).get());

		ProjectAllocationId projectAllocationId1 = createProjectAllocation("Neuroinforamtics Cineca Allocation 2",
			communityAllocationId, cinecaId);
		createProjectAllocationChunks(projectAllocationId1);
		createResourceUsages(projectAllocationId1, projectAllocationRepository.findByIdWithRelatedObjects(projectAllocationId1).get());

		crateProjectInstallationJobs(cinecaId);
	}

	private ProjectAllocationId createProjectAllocation(String Neuroinforamtics_Cineca_Allocation,
	                                                    CommunityAllocationId communityAllocationId, SiteId cinecaId) {
		ProjectAllocation projectAllocation = ProjectAllocation.builder()
			.projectId(projectId)
			.name(Neuroinforamtics_Cineca_Allocation)
			.amount(new BigDecimal(20))
			.communityAllocationId(communityAllocationId)
			.build();

		ProjectAllocationId projectAllocationId = projectAllocationRepository.create(projectAllocation);

		ProjectAllocationInstallation projectAllocationInstallation = ProjectAllocationInstallation.builder()
			.projectAllocationId(projectAllocationId)
			.siteId(cinecaId)
			.status(ProjectAllocationInstallationStatus.INSTALLATION_ACKNOWLEDGED)
			.correlationId(new CorrelationId(UUID.randomUUID().toString()))
			.build();

		projectAllocationInstallationRepository.create(projectAllocationInstallation);
		return projectAllocationId;
	}

	private CommunityAllocationId createCommunityAllocation(CommunityId communityId,
	                                                        ResourceCreditId resourceCreditCinecaId1,
	                                                        String HBP_Cineca_Allocation, int val) {
		CommunityAllocation communityAllocation2 = CommunityAllocation.builder()
			.communityId(communityId)
			.resourceCreditId(resourceCreditCinecaId1)
			.name(HBP_Cineca_Allocation)
			.amount(new BigDecimal(val))
			.build();
		return communityAllocationRepository.create(communityAllocation2);
	}

	private ResourceCreditId createResourceCredit(SiteId cinecaId, ResourceTypeId resourceTypeCinecaId,
	                                              String First_Cineca_pool, int val, int month, int dayOfMonth,
	                                              int dayOfMonth1, int dayOfMonth2, int dayOfMonth3, int hour,
	                                              int hour1, int year, int month1) {
		ResourceCredit resourceCreditCineca = ResourceCredit.builder()
			.siteId(cinecaId)
			.resourceTypeId(resourceTypeCinecaId)
			.name(First_Cineca_pool)
			.amount(new BigDecimal(val))
			.utcStartTime(LocalDateTime.of(2021, month, dayOfMonth, hour, dayOfMonth1))
			.utcEndTime(LocalDateTime.of(year, month1, dayOfMonth2, hour1, dayOfMonth3))
			.build();
		ResourceCreditId resourceCreditCinecaId1 = resourceCreditRepository.create(resourceCreditCineca);
		return resourceCreditCinecaId1;
	}

	private void createProjectAllocationChunks(ProjectAllocationId projectAllocationId1) {
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
	}

	private void crateProjectInstallationJobs(SiteId cinecaId) {
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

	private void createResourceUsages(ProjectAllocationId projectAllocationId,
	                                  ProjectAllocationResolved projectAllocationResolved) {
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
	}

	private ResourceTypeId createResourceType(SiteId cinecaId, InfraServiceId serviceCinecaId, String name,
	                                          ResourceMeasureType type, ResourceMeasureUnit unit) {
		ResourceType resourceTypeCineca = ResourceType.builder()
			.siteId(cinecaId)
			.serviceId(serviceCinecaId)
			.name(name)
			.type(type)
			.unit(unit)
			.build();
		return resourceTypeRepository.create(resourceTypeCineca);
	}

	private InfraServiceId createInfraService(SiteId cinecaId, PolicyId policyId1, String name, String description) {
		InfraService infraServiceCineca = InfraService.builder()
			.name(name)
			.siteId(cinecaId)
			.description(description)
			.policyId(policyId1)
			.build();
		return infraServiceRepository.create(infraServiceCineca);
	}

	private SiteId createSite(String CINECA, String id) {
		Site cineca = Site.builder()
			.name(CINECA)
			.build();
		SiteExternalId ciencaExternalId = new SiteExternalId(id);
		SiteId cinecaId = siteRepository.create(cineca, ciencaExternalId);
		siteAgentService.initializeSiteConnection(ciencaExternalId);
		siteGroupDAO.create(Site.builder().id(cinecaId).name(cineca.getName()).build());
		return cinecaId;
	}

	private void addPolicyToSite(SiteId cinecaId, PolicyId policyId) {
		Site updateCineca = Site.builder()
			.id(cinecaId)
			.policyId(policyId)
			.name("CINECA")
			.build();
		siteRepository.update(updateCineca);
	}

	private PolicyId createPolicy(String Cineca_site_policy, PolicyWorkflow webBased, SiteId cinecaId) {
		PolicyDocument policyDocument = PolicyDocument.builder()
			.name(Cineca_site_policy)
			.workflow(webBased)
			.contentType(PolicyContentType.EMBEDDED)
			.wysiwygText("<div><p>TEXT</p></div>")
			.revision(1)
			.siteId(cinecaId)
			.build();
		return policyDocumentRepository.create(policyDocument);
	}
}
