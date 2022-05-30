/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.rest.site;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_credits.ResourceCreditId;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.domain.services.InfraServiceId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.integration.tests.IntegrationTestBase;
import io.imunity.furms.integration.tests.tools.users.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultCommunity;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultCommunityAllocation;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultPolicy;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultProject;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultProjectAllocation;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultProjectAllocationChunk;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultResourceCredit;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultResourceType;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultResourceUsage;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultUserResourceUsage;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultService;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultSite;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.basicUser;
import static java.math.BigDecimal.ONE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SiteAllocationsAndResourceConsumptionIntegrationTest extends IntegrationTestBase {

	private Site site;
	private Site darkSite;

	private TestUser projectAdmin;

	@BeforeEach
	void setUp() {
		Site.SiteBuilder siteBuilder = defaultSite().name("site1");
		site = siteBuilder
				.id(siteRepository.create(siteBuilder.build(), siteBuilder.build().getExternalId()))
				.build();
		Site.SiteBuilder darkSiteBuilder = defaultSite()
				.name("Dark Site");
		darkSite = darkSiteBuilder
				.id(siteRepository.create(darkSiteBuilder.build(), new SiteExternalId("dsid")))
				.build();

		projectAdmin = basicUser();
		projectAdmin.addSiteAdmin(site.getId());
	}

	@Test
	void shouldFindAllWhileGettingFurmsAllocationsForSpecificSite() throws Exception {
		//given
		final ResourceCreditId resourceCredit = createResourceCredit(site.getId(), "RC Test 1", BigDecimal.valueOf(100));
		final ResourceTypeId resourceType = resourceCreditRepository.findById(resourceCredit).get().resourceTypeId;
		final CommunityId communityId = createCommunity();
		final CommunityAllocationId communityAllocation = createCommunityAllocation(communityId, resourceCredit);
		final ProjectId projectId1 = createProject(communityId);
		final ProjectId projectId2 = createProject(communityId);
		final ProjectId projectId3 = createProject(communityId);
		final ProjectAllocationId allocation1 = createProjectAllocation(communityAllocation, projectId1, ONE);
		final ProjectAllocationId allocation2 = createProjectAllocation(communityAllocation, projectId2, ONE);
		final ProjectAllocationId allocation3 = createProjectAllocation(communityAllocation, projectId3, ONE);
		final Set<String> expectedAllocations = Set.of(allocation1.id.toString(), allocation2.id.toString(),
			allocation3.id.toString());

		final ResourceCreditId darkResourceCredit = createResourceCredit(darkSite.getId(), "RC Test 2", BigDecimal.valueOf(100));
		final CommunityId darkCommunityId = communityRepository.create(defaultCommunity()
				.name(UUID.randomUUID().toString())
				.build());
		final CommunityAllocationId darkCommunityAllocation = createCommunityAllocation(darkCommunityId, darkResourceCredit);
		final ProjectId darkProjectId = createProject(darkCommunityId);
		createProjectAllocation(darkCommunityAllocation, darkProjectId, ONE);

		projectAdmin.addProjectAdmin(communityId, projectId1);
		projectAdmin.addProjectAdmin(communityId, projectId2);
		projectAdmin.addProjectAdmin(communityId, projectId3);
		setupUser(projectAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/furmsAllocations", site.getId().id)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(3)))
				.andExpect(jsonPath("$.[0].id", in(expectedAllocations)))
				.andExpect(jsonPath("$.[0].communityAllocationId", equalTo(communityAllocation.id.toString())))
				.andExpect(jsonPath("$.[0].resourceTypeId", equalTo(resourceType.id.toString())))
				.andExpect(jsonPath("$.[0].amount", equalTo(ONE.intValue())))
				.andExpect(jsonPath("$.[1].id", in(expectedAllocations)))
				.andExpect(jsonPath("$.[1].communityAllocationId", equalTo(communityAllocation.id.toString())))
				.andExpect(jsonPath("$.[1].resourceTypeId", equalTo(resourceType.id.toString())))
				.andExpect(jsonPath("$.[1].amount", equalTo(ONE.intValue())))
				.andExpect(jsonPath("$.[2].id", in(expectedAllocations)))
				.andExpect(jsonPath("$.[2].communityAllocationId", equalTo(communityAllocation.id.toString())))
				.andExpect(jsonPath("$.[2].resourceTypeId", equalTo(resourceType.id.toString())))
				.andExpect(jsonPath("$.[2].amount", equalTo(ONE.intValue())));
	}

	@Test
	void shouldReturnForbiddenForUserThatHasNotRightsWhileGettingFurmsAllocations() throws Exception {
		//given
		final CommunityId communityId = communityRepository.create(defaultCommunity()
				.name(UUID.randomUUID().toString())
				.build());
		final ProjectId projectId = createProject(communityId);
		final TestUser noSiteAdmin = basicUser();
		noSiteAdmin.addProjectAdmin(communityId, projectId);
		setupUser(noSiteAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/furmsAllocations", site.getId().id)
				.with(noSiteAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldFindAllFurmsAllocationsForSpecificSiteAndProject() throws Exception {
		//given
		final ResourceCreditId resourceCredit = createResourceCredit(site.getId(), "RC Test 1", BigDecimal.valueOf(100));
		final ResourceTypeId resourceType = resourceCreditRepository.findById(resourceCredit).get().resourceTypeId;
		final CommunityId communityId = createCommunity();
		final CommunityAllocationId communityAllocation = createCommunityAllocation(communityId, resourceCredit);
		final ProjectId projectId1 = createProject(communityId);
		final ProjectId projectId2 = createProject(communityId);
		final ProjectAllocationId allocation1 = createProjectAllocation(communityAllocation, projectId1, ONE);
		final ProjectAllocationId allocation2 = createProjectAllocation(communityAllocation, projectId1, ONE);
		createProjectAllocation(communityAllocation, projectId2, ONE);

		final Set<String> expectedAllocations = Set.of(allocation1.id.toString(), allocation2.id.toString());

		projectAdmin.addProjectAdmin(communityId, projectId1);
		projectAdmin.addProjectAdmin(communityId, projectId2);
		setupUser(projectAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/furmsAllocations/{projectId}", site.getId().id, projectId1.id)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].id", in(expectedAllocations)))
				.andExpect(jsonPath("$.[0].communityAllocationId", equalTo(communityAllocation.id.toString())))
				.andExpect(jsonPath("$.[0].resourceTypeId", equalTo(resourceType.id.toString())))
				.andExpect(jsonPath("$.[0].amount", equalTo(ONE.intValue())))
				.andExpect(jsonPath("$.[1].id", in(expectedAllocations)))
				.andExpect(jsonPath("$.[1].communityAllocationId", equalTo(communityAllocation.id.toString())))
				.andExpect(jsonPath("$.[1].resourceTypeId", equalTo(resourceType.id.toString())))
				.andExpect(jsonPath("$.[1].amount", equalTo(ONE.intValue())));
	}

	@Test
	void shouldReturnForbiddenForUserThatHasNotRightsDuringGettingProjectAllocationsForProject() throws Exception {
		//given
		final CommunityId communityId = createCommunity();
		final ProjectId projectId = createProject(communityId);
		final TestUser noSiteAdmin = basicUser();
		noSiteAdmin.addProjectAdmin(communityId, projectId);
		setupUser(noSiteAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/furmsAllocations/{projectId}", site.getId().id, projectId.id)
				.with(noSiteAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldNotFindFurmsAllocationsAndReturnEmptyArrayWhenProjectDoesNotExists() throws Exception {
		//given
		final CommunityId communityId = createCommunity();
		final ProjectId projectId = createProject(communityId);

		projectAdmin.addProjectAdmin(communityId, projectId);
		setupUser(projectAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/furmsAllocations/{projectId}", site.getId().id,
				UUID.randomUUID().toString())
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void shouldReturnNotFoundIfSiteDoesNotExistDuringGettingAllInstalledProjects() throws Exception {
		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/furmsAllocations", UUID.randomUUID().toString()))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldReturnForbiddenIfSiteDoesNotBelongToUserDuringGettingAllInstalledProjects() throws Exception {
		final TestUser testUser = basicUser();
		setupUser(testUser);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/furmsAllocations", site.getId().id)
				.with(testUser.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldFindAllSiteAllocationForSpecificSite() throws Exception {
		//given
		final ResourceCreditId resourceCredit = createResourceCredit(site.getId(), "RC Test 1", BigDecimal.valueOf(100));
		final CommunityId communityId = createCommunity();
		final CommunityAllocationId communityAllocation = createCommunityAllocation(communityId, resourceCredit);
		final ProjectId projectId1 = createProject(communityId);
		final ProjectId projectId2 = createProject(communityId);
		final ProjectAllocationId allocation1 = createProjectAllocation(communityAllocation, projectId1, ONE);
		final ProjectAllocationId allocation2 = createProjectAllocation(communityAllocation, projectId2, ONE);
		createProjectAllocationChunk(allocation1, BigDecimal.valueOf(0.1));
		createProjectAllocationChunk(allocation2, BigDecimal.valueOf(0.1));
		createProjectAllocationChunk(allocation1, BigDecimal.valueOf(0.1));
		createProjectAllocationChunk(allocation2, BigDecimal.valueOf(0.1));

		final Set<String> expectedAllocations = Set.of(allocation1.id.toString(), allocation2.id.toString());

		final ResourceCreditId darkResourceCredit = createResourceCredit(darkSite.getId(), "RC Test 2", BigDecimal.valueOf(100));
		final CommunityId darkCommunity = createCommunity();
		final CommunityAllocationId darkCommunityAllocation = createCommunityAllocation(darkCommunity, darkResourceCredit);
		final ProjectId darkProjectId = createProject(darkCommunity);
		final ProjectAllocationId darkAllocation = createProjectAllocation(darkCommunityAllocation, darkProjectId, ONE);
		createProjectAllocationChunk(darkAllocation, BigDecimal.valueOf(0.1));
		createProjectAllocationChunk(darkAllocation, BigDecimal.valueOf(0.1));

		projectAdmin.addProjectAdmin(communityId, projectId1);
		projectAdmin.addProjectAdmin(communityId, projectId2);
		projectAdmin.addProjectAdmin(darkCommunity, darkProjectId);
		setupUser(projectAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/siteAllocations", site.getId().id)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(4)))
				.andExpect(jsonPath("$.[0].allocationId", in(expectedAllocations)))
				.andExpect(jsonPath("$.[0].siteId", equalTo(site.getId().id.toString())))
				.andExpect(jsonPath("$.[0].amount", equalTo(0.1)))
				.andExpect(jsonPath("$.[0].validity", notNullValue()));
	}

	@Test
	void shouldFindAllSiteAllocationForSpecificSiteAndProject() throws Exception {
		//given
		final ResourceCreditId resourceCredit = createResourceCredit(site.getId(), "RC Test 1", BigDecimal.valueOf(100));
		final CommunityId communityId = createCommunity();
		final CommunityAllocationId communityAllocation = createCommunityAllocation(communityId, resourceCredit);
		final ProjectId projectId1 = createProject(communityId);
		final ProjectId projectId2 = createProject(communityId);
		final ProjectAllocationId allocation1 = createProjectAllocation(communityAllocation, projectId1, ONE);
		final ProjectAllocationId allocation2 = createProjectAllocation(communityAllocation, projectId2, ONE);
		createProjectAllocationChunk(allocation1, BigDecimal.valueOf(0.1));
		createProjectAllocationChunk(allocation2, BigDecimal.valueOf(0.1));
		createProjectAllocationChunk(allocation1, BigDecimal.valueOf(0.1));
		createProjectAllocationChunk(allocation2, BigDecimal.valueOf(0.1));

		projectAdmin.addProjectAdmin(communityId, projectId1);
		projectAdmin.addProjectAdmin(communityId, projectId2);
		setupUser(projectAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/siteAllocations/{projectId}", site.getId().id, projectId1.id)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].allocationId", equalTo(allocation1.id.toString())))
				.andExpect(jsonPath("$.[0].siteId", equalTo(site.getId().id.toString())))
				.andExpect(jsonPath("$.[0].amount", equalTo(0.1)))
				.andExpect(jsonPath("$.[0].validity", notNullValue()));
	}

	@Test
	void shouldFindCumulativeProjectResourceConsumptionForSpecificSiteAndProject() throws Exception {
		//given
		final ResourceCreditId resourceCredit = createResourceCredit(site.getId(), "RC Test 1", BigDecimal.valueOf(100));
		final ResourceTypeId resourceType = resourceCreditRepository.findById(resourceCredit).get().resourceTypeId;
		final CommunityId communityId = createCommunity();
		final CommunityAllocationId communityAllocation = createCommunityAllocation(communityId, resourceCredit);
		final ProjectId projectId1 = createProject(communityId);
		final ProjectId projectId2 = createProject(communityId);
		final ProjectAllocationId allocation1 = createProjectAllocation(communityAllocation, projectId1, ONE);
		final ProjectAllocationId allocation2 = createProjectAllocation(communityAllocation, projectId1, ONE);
		createProjectAllocation(communityAllocation, projectId2, ONE);

		createResourceUsage(projectId1, allocation1, BigDecimal.valueOf(0.2));
		createResourceUsage(projectId1, allocation2, BigDecimal.valueOf(0.3));

		final Set<String> expectedAllocations = Set.of(allocation1.id.toString(), allocation2.id.toString());

		projectAdmin.addProjectAdmin(communityId, projectId1);
		projectAdmin.addProjectAdmin(communityId, projectId2);
		setupUser(projectAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/cumulativeResourcesConsumption/{projectId}", site.getId().id,
				projectId1.id)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].projectAllocationId", in(expectedAllocations)))
				.andExpect(jsonPath("$.[0].siteId", equalTo(site.getId().id.toString())))
				.andExpect(jsonPath("$.[0].resourceTypeId", equalTo(resourceType.id.toString())))
				.andExpect(jsonPath("$.[0].consumedAmount", in(Set.of(0.2, 0.3))))
				.andExpect(jsonPath("$.[1].projectAllocationId", in(expectedAllocations)))
				.andExpect(jsonPath("$.[1].siteId", equalTo(site.getId().id.toString())))
				.andExpect(jsonPath("$.[1].resourceTypeId", equalTo(resourceType.id.toString())))
				.andExpect(jsonPath("$.[1].consumedAmount", in(Set.of(0.2, 0.3))));
	}

	@Test
	void shouldFindProjectResourceRecordsForSpecificSiteAndProject() throws Exception {
		//given
		final ResourceCreditId resourceCredit = createResourceCredit(site.getId(), "RC Test 1", BigDecimal.valueOf(100));
		final ResourceTypeId resourceType = resourceCreditRepository.findById(resourceCredit).get().resourceTypeId;
		final CommunityId communityId = createCommunity();
		final CommunityAllocationId communityAllocation = createCommunityAllocation(communityId, resourceCredit);
		final ProjectId projectId1 = createProject(communityId);
		final ProjectId projectId2 = createProject(communityId);
		final ProjectAllocationId allocation1 = createProjectAllocation(communityAllocation, projectId1, ONE);
		final ProjectAllocationId allocation2 = createProjectAllocation(communityAllocation, projectId1, ONE);
		createProjectAllocation(communityAllocation, projectId2, ONE);

		createUserResourceUsage(projectId1, allocation1, BigDecimal.valueOf(0.2), projectAdmin);
		createUserResourceUsage(projectId1, allocation2, BigDecimal.valueOf(0.3), projectAdmin);

		final Set<String> expectedAllocations = Set.of(allocation1.id.toString(), allocation2.id.toString());

		projectAdmin.addProjectAdmin(communityId, projectId1);
		projectAdmin.addProjectAdmin(communityId, projectId2);
		setupUser(projectAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/usageRecords/{projectId}", site.getId().id, projectId1.id)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].projectAllocationId", in(expectedAllocations)))
				.andExpect(jsonPath("$.[0].siteId", equalTo(site.getId().id.toString())))
				.andExpect(jsonPath("$.[0].resourceTypeId", equalTo(resourceType.id.toString())))
				.andExpect(jsonPath("$.[0].consumedAmount", in(Set.of(0.2, 0.3))))
				.andExpect(jsonPath("$.[0].userFenixId", equalTo(projectAdmin.getFenixId())));
	}

	private ProjectId createProject(CommunityId communityId) {
		return projectRepository.create(defaultProject()
				.communityId(communityId)
				.name(UUID.randomUUID().toString())
				.leaderId(new PersistentId(ADMIN_USER.getUserId()))
				.build());
	}

	private ProjectAllocationId createProjectAllocation(CommunityAllocationId communityAllocation, ProjectId projectId,
	                                       BigDecimal amount) {
		return projectAllocationRepository.create(defaultProjectAllocation()
				.communityAllocationId(communityAllocation)
				.projectId(projectId)
				.name(UUID.randomUUID().toString())
				.amount(amount)
				.build());
	}

	private void createResourceUsage(ProjectId projectId, ProjectAllocationId projectAllocation, BigDecimal amount) {
		final ProjectAllocationResolved projectAllocationResolved =
				projectAllocationRepository.findByIdWithRelatedObjects(projectAllocation).get();
		resourceUsageRepository.create(defaultResourceUsage()
						.projectId(projectId)
						.projectAllocationId(projectAllocation)
						.cumulativeConsumption(amount)
						.build(),
				projectAllocationResolved);
	}

	private void createUserResourceUsage(ProjectId projectId, ProjectAllocationId projectAllocation, BigDecimal amount,
	                                     TestUser testUser) {
		resourceUsageRepository.create(defaultUserResourceUsage()
				.projectId(projectId)
				.projectAllocationId(projectAllocation)
				.cumulativeConsumption(amount)
				.fenixUserId(new FenixUserId(testUser.getFenixId()))
				.build());
	}

	private CommunityAllocationId createCommunityAllocation(CommunityId communityId, ResourceCreditId resourceCredit) {
		return communityAllocationRepository.create(defaultCommunityAllocation()
				.communityId(communityId)
				.resourceCreditId(resourceCredit)
				.name(UUID.randomUUID().toString())
				.amount(BigDecimal.TEN)
				.build());
	}

	private CommunityId createCommunity() {
		return communityRepository.create(defaultCommunity()
				.name(UUID.randomUUID().toString())
				.build());
	}

	private ResourceCreditId createResourceCredit(SiteId siteId, String name, BigDecimal amount) {
		final PolicyId policyId = policyDocumentRepository.create(defaultPolicy()
				.siteId(siteId)
				.build());
		final InfraServiceId serviceId = infraServiceRepository.create(defaultService()
				.siteId(siteId)
				.name(UUID.randomUUID().toString())
				.policyId(policyId)
				.build());
		final ResourceTypeId resourceType = resourceTypeRepository.create(defaultResourceType()
				.siteId(siteId)
				.serviceId(serviceId)
				.name(UUID.randomUUID().toString())
				.build());
		return resourceCreditRepository.create(defaultResourceCredit()
				.siteId(siteId)
				.resourceTypeId(resourceType)
				.name(name)
				.amount(amount)
				.build());
	}

	private void createProjectAllocationChunk(ProjectAllocationId allocation, BigDecimal amount) {
		projectAllocationInstallationRepository.create(defaultProjectAllocationChunk()
			.projectAllocationId(allocation)
			.amount(amount)
			.build());
	}
}
