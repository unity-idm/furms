/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.rest.site;

import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
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
				.name("Dark Site")
				.externalId(new SiteExternalId("dsid"));
		darkSite = darkSiteBuilder
				.id(siteRepository.create(darkSiteBuilder.build(), darkSiteBuilder.build().getExternalId()))
				.build();

		projectAdmin = basicUser();
		projectAdmin.addSiteAdmin(site.getId());
	}

	@Test
	void shouldFindAllWhileGettingFurmsAllocationsForSpecificSite() throws Exception {
		//given
		final String resourceCredit = createResourceCredit(site.getId(), "RC Test 1", BigDecimal.valueOf(100));
		final String resourceType = resourceCreditRepository.findById(resourceCredit).get().resourceTypeId;
		final String communityId = createCommunity();
		final String communityAllocation = createCommunityAllocation(communityId, resourceCredit);
		final String projectId1 = createProject(communityId);
		final String projectId2 = createProject(communityId);
		final String projectId3 = createProject(communityId);
		final String allocation1 = createProjectAllocation(communityAllocation, projectId1, ONE);
		final String allocation2 = createProjectAllocation(communityAllocation, projectId2, ONE);
		final String allocation3 = createProjectAllocation(communityAllocation, projectId3, ONE);
		final Set<String> expectedAllocations = Set.of(allocation1, allocation2, allocation3);

		final String darkResourceCredit = createResourceCredit(darkSite.getId(), "RC Test 2", BigDecimal.valueOf(100));
		final String darkCommunityId = communityRepository.create(defaultCommunity()
				.name(UUID.randomUUID().toString())
				.build());
		final String darkCommunityAllocation = createCommunityAllocation(darkCommunityId, darkResourceCredit);
		final String darkProjectId = createProject(darkCommunityId);
		createProjectAllocation(darkCommunityAllocation, darkProjectId, ONE);

		projectAdmin.addProjectAdmin(communityId, projectId1);
		projectAdmin.addProjectAdmin(communityId, projectId2);
		projectAdmin.addProjectAdmin(communityId, projectId3);
		setupUser(projectAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/furmsAllocations", site.getId())
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(3)))
				.andExpect(jsonPath("$.[0].id", in(expectedAllocations)))
				.andExpect(jsonPath("$.[0].communityAllocationId", equalTo(communityAllocation)))
				.andExpect(jsonPath("$.[0].resourceTypeId", equalTo(resourceType)))
				.andExpect(jsonPath("$.[0].amount", equalTo(ONE.intValue())))
				.andExpect(jsonPath("$.[1].id", in(expectedAllocations)))
				.andExpect(jsonPath("$.[1].communityAllocationId", equalTo(communityAllocation)))
				.andExpect(jsonPath("$.[1].resourceTypeId", equalTo(resourceType)))
				.andExpect(jsonPath("$.[1].amount", equalTo(ONE.intValue())))
				.andExpect(jsonPath("$.[2].id", in(expectedAllocations)))
				.andExpect(jsonPath("$.[2].communityAllocationId", equalTo(communityAllocation)))
				.andExpect(jsonPath("$.[2].resourceTypeId", equalTo(resourceType)))
				.andExpect(jsonPath("$.[2].amount", equalTo(ONE.intValue())));
	}

	@Test
	void shouldReturnForbiddenForUserThatHasNotRightsWhileGettingFurmsAllocations() throws Exception {
		//given
		final String communityId = communityRepository.create(defaultCommunity()
				.name(UUID.randomUUID().toString())
				.build());
		final String projectId = createProject(communityId);
		final TestUser noSiteAdmin = basicUser();
		noSiteAdmin.addProjectAdmin(communityId, projectId);
		setupUser(noSiteAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/furmsAllocations", site.getId())
				.with(noSiteAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldFindAllFurmsAllocationsForSpecificSiteAndProject() throws Exception {
		//given
		final String resourceCredit = createResourceCredit(site.getId(), "RC Test 1", BigDecimal.valueOf(100));
		final String resourceType = resourceCreditRepository.findById(resourceCredit).get().resourceTypeId;
		final String communityId = createCommunity();
		final String communityAllocation = createCommunityAllocation(communityId, resourceCredit);
		final String projectId1 = createProject(communityId);
		final String projectId2 = createProject(communityId);
		final String allocation1 = createProjectAllocation(communityAllocation, projectId1, ONE);
		final String allocation2 = createProjectAllocation(communityAllocation, projectId1, ONE);
		createProjectAllocation(communityAllocation, projectId2, ONE);

		final Set<String> expectedAllocations = Set.of(allocation1, allocation2);

		projectAdmin.addProjectAdmin(communityId, projectId1);
		projectAdmin.addProjectAdmin(communityId, projectId2);
		setupUser(projectAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/furmsAllocations/{projectId}", site.getId(), projectId1)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].id", in(expectedAllocations)))
				.andExpect(jsonPath("$.[0].communityAllocationId", equalTo(communityAllocation)))
				.andExpect(jsonPath("$.[0].resourceTypeId", equalTo(resourceType)))
				.andExpect(jsonPath("$.[0].amount", equalTo(ONE.intValue())))
				.andExpect(jsonPath("$.[1].id", in(expectedAllocations)))
				.andExpect(jsonPath("$.[1].communityAllocationId", equalTo(communityAllocation)))
				.andExpect(jsonPath("$.[1].resourceTypeId", equalTo(resourceType)))
				.andExpect(jsonPath("$.[1].amount", equalTo(ONE.intValue())));
	}

	@Test
	void shouldReturnForbiddenForUserThatHasNotRightsDuringGettingProjectAllocationsForProject() throws Exception {
		//given
		final String communityId = createCommunity();
		final String projectId = createProject(communityId);
		final TestUser noSiteAdmin = basicUser();
		noSiteAdmin.addProjectAdmin(communityId, projectId);
		setupUser(noSiteAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/furmsAllocations/{projectId}", site.getId(), projectId)
				.with(noSiteAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldNotFindFurmsAllocationsAndReturnEmptyArrayWhenProjectDoesNotExists() throws Exception {
		//given
		final String communityId = createCommunity();
		final String projectId = createProject(communityId);

		projectAdmin.addProjectAdmin(communityId, projectId);
		setupUser(projectAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/furmsAllocations/{projectId}", site.getId(), UUID.randomUUID().toString())
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
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/furmsAllocations", site.getId())
				.with(testUser.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldFindAllSiteAllocationForSpecificSite() throws Exception {
		//given
		final String resourceCredit = createResourceCredit(site.getId(), "RC Test 1", BigDecimal.valueOf(100));
		final String communityId = createCommunity();
		final String communityAllocation = createCommunityAllocation(communityId, resourceCredit);
		final String projectId1 = createProject(communityId);
		final String projectId2 = createProject(communityId);
		final String allocation1 = createProjectAllocation(communityAllocation, projectId1, ONE);
		final String allocation2 = createProjectAllocation(communityAllocation, projectId2, ONE);
		createProjectAllocationChunk(allocation1, BigDecimal.valueOf(0.1));
		createProjectAllocationChunk(allocation2, BigDecimal.valueOf(0.1));
		createProjectAllocationChunk(allocation1, BigDecimal.valueOf(0.1));
		createProjectAllocationChunk(allocation2, BigDecimal.valueOf(0.1));

		final Set<String> expectedAllocations = Set.of(allocation1, allocation2);

		final String darkResourceCredit = createResourceCredit(darkSite.getId(), "RC Test 2", BigDecimal.valueOf(100));
		final String darkCommunity = createCommunity();
		final String darkCommunityAllocation = createCommunityAllocation(darkCommunity, darkResourceCredit);
		final String darkProjectId = createProject(darkCommunity);
		final String darkAllocation = createProjectAllocation(darkCommunityAllocation, darkProjectId, ONE);
		createProjectAllocationChunk(darkAllocation, BigDecimal.valueOf(0.1));
		createProjectAllocationChunk(darkAllocation, BigDecimal.valueOf(0.1));

		projectAdmin.addProjectAdmin(communityId, projectId1);
		projectAdmin.addProjectAdmin(communityId, projectId2);
		projectAdmin.addProjectAdmin(darkCommunity, darkProjectId);
		setupUser(projectAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/siteAllocations", site.getId())
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(4)))
				.andExpect(jsonPath("$.[0].allocationId", in(expectedAllocations)))
				.andExpect(jsonPath("$.[0].siteId", equalTo(site.getId())))
				.andExpect(jsonPath("$.[0].amount", equalTo(0.1)))
				.andExpect(jsonPath("$.[0].validity", notNullValue()));
	}

	@Test
	void shouldFindAllSiteAllocationForSpecificSiteAndProject() throws Exception {
		//given
		final String resourceCredit = createResourceCredit(site.getId(), "RC Test 1", BigDecimal.valueOf(100));
		final String communityId = createCommunity();
		final String communityAllocation = createCommunityAllocation(communityId, resourceCredit);
		final String projectId1 = createProject(communityId);
		final String projectId2 = createProject(communityId);
		final String allocation1 = createProjectAllocation(communityAllocation, projectId1, ONE);
		final String allocation2 = createProjectAllocation(communityAllocation, projectId2, ONE);
		createProjectAllocationChunk(allocation1, BigDecimal.valueOf(0.1));
		createProjectAllocationChunk(allocation2, BigDecimal.valueOf(0.1));
		createProjectAllocationChunk(allocation1, BigDecimal.valueOf(0.1));
		createProjectAllocationChunk(allocation2, BigDecimal.valueOf(0.1));

		projectAdmin.addProjectAdmin(communityId, projectId1);
		projectAdmin.addProjectAdmin(communityId, projectId2);
		setupUser(projectAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/siteAllocations/{projectId}", site.getId(), projectId1)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].allocationId", equalTo(allocation1)))
				.andExpect(jsonPath("$.[0].siteId", equalTo(site.getId())))
				.andExpect(jsonPath("$.[0].amount", equalTo(0.1)))
				.andExpect(jsonPath("$.[0].validity", notNullValue()));
	}

	@Test
	void shouldFindCumulativeProjectResourceConsumptionForSpecificSiteAndProject() throws Exception {
		//given
		final String resourceCredit = createResourceCredit(site.getId(), "RC Test 1", BigDecimal.valueOf(100));
		final String resourceType = resourceCreditRepository.findById(resourceCredit).get().resourceTypeId;
		final String communityId = createCommunity();
		final String communityAllocation = createCommunityAllocation(communityId, resourceCredit);
		final String projectId1 = createProject(communityId);
		final String projectId2 = createProject(communityId);
		final String allocation1 = createProjectAllocation(communityAllocation, projectId1, ONE);
		final String allocation2 = createProjectAllocation(communityAllocation, projectId1, ONE);
		createProjectAllocation(communityAllocation, projectId2, ONE);

		createResourceUsage(projectId1, allocation1, BigDecimal.valueOf(0.2));
		createResourceUsage(projectId1, allocation2, BigDecimal.valueOf(0.3));

		final Set<String> expectedAllocations = Set.of(allocation1, allocation2);

		projectAdmin.addProjectAdmin(communityId, projectId1);
		projectAdmin.addProjectAdmin(communityId, projectId2);
		setupUser(projectAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/cumulativeResourcesConsumption/{projectId}", site.getId(), projectId1)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].projectAllocationId", in(expectedAllocations)))
				.andExpect(jsonPath("$.[0].siteId", equalTo(site.getId())))
				.andExpect(jsonPath("$.[0].resourceTypeId", equalTo(resourceType)))
				.andExpect(jsonPath("$.[0].consumedAmount", in(Set.of(0.2, 0.3))))
				.andExpect(jsonPath("$.[1].projectAllocationId", in(expectedAllocations)))
				.andExpect(jsonPath("$.[1].siteId", equalTo(site.getId())))
				.andExpect(jsonPath("$.[1].resourceTypeId", equalTo(resourceType)))
				.andExpect(jsonPath("$.[1].consumedAmount", in(Set.of(0.2, 0.3))));
	}

	@Test
	void shouldFindProjectResourceRecordsForSpecificSiteAndProject() throws Exception {
		//given
		final String resourceCredit = createResourceCredit(site.getId(), "RC Test 1", BigDecimal.valueOf(100));
		final String resourceType = resourceCreditRepository.findById(resourceCredit).get().resourceTypeId;
		final String communityId = createCommunity();
		final String communityAllocation = createCommunityAllocation(communityId, resourceCredit);
		final String projectId1 = createProject(communityId);
		final String projectId2 = createProject(communityId);
		final String allocation1 = createProjectAllocation(communityAllocation, projectId1, ONE);
		final String allocation2 = createProjectAllocation(communityAllocation, projectId1, ONE);
		createProjectAllocation(communityAllocation, projectId2, ONE);

		createUserResourceUsage(projectId1, allocation1, BigDecimal.valueOf(0.2), projectAdmin);
		createUserResourceUsage(projectId1, allocation2, BigDecimal.valueOf(0.3), projectAdmin);

		final Set<String> expectedAllocations = Set.of(allocation1, allocation2);

		projectAdmin.addProjectAdmin(communityId, projectId1);
		projectAdmin.addProjectAdmin(communityId, projectId2);
		setupUser(projectAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/usageRecords/{projectId}", site.getId(), projectId1)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].projectAllocationId", in(expectedAllocations)))
				.andExpect(jsonPath("$.[0].siteId", equalTo(site.getId())))
				.andExpect(jsonPath("$.[0].resourceTypeId", equalTo(resourceType)))
				.andExpect(jsonPath("$.[0].consumedAmount", in(Set.of(0.2, 0.3))))
				.andExpect(jsonPath("$.[0].userFenixId", equalTo(projectAdmin.getFenixId())));
	}

	private String createProject(String communityId) {
		return projectRepository.create(defaultProject()
				.communityId(communityId)
				.name(UUID.randomUUID().toString())
				.leaderId(new PersistentId(ADMIN_USER.getUserId()))
				.build());
	}

	private String createProjectAllocation(String communityAllocation, String projectId, BigDecimal amount) {
		return projectAllocationRepository.create(defaultProjectAllocation()
				.communityAllocationId(communityAllocation)
				.projectId(projectId)
				.name(UUID.randomUUID().toString())
				.amount(amount)
				.build());
	}

	private void createResourceUsage(String projectId, String projectAllocation, BigDecimal amount) {
		final ProjectAllocationResolved projectAllocationResolved =
				projectAllocationRepository.findByIdWithRelatedObjects(projectAllocation).get();
		resourceUsageRepository.create(defaultResourceUsage()
						.projectId(projectId)
						.projectAllocationId(projectAllocation)
						.cumulativeConsumption(amount)
						.build(),
				projectAllocationResolved);
	}

	private void createUserResourceUsage(String projectId, String projectAllocation, BigDecimal amount, TestUser testUser) {
		resourceUsageRepository.create(defaultUserResourceUsage()
				.projectId(projectId)
				.projectAllocationId(projectAllocation)
				.cumulativeConsumption(amount)
				.fenixUserId(new FenixUserId(testUser.getFenixId()))
				.build());
	}

	private String createCommunityAllocation(String communityId, String resourceCredit) {
		return communityAllocationRepository.create(defaultCommunityAllocation()
				.communityId(communityId)
				.resourceCreditId(resourceCredit)
				.name(UUID.randomUUID().toString())
				.amount(BigDecimal.TEN)
				.build());
	}

	private String createCommunity() {
		return communityRepository.create(defaultCommunity()
				.name(UUID.randomUUID().toString())
				.build());
	}

	private String createResourceCredit(String siteId, String name, BigDecimal amount) {
		final PolicyId policyId = policyDocumentRepository.create(defaultPolicy()
				.siteId(siteId)
				.build());
		final String serviceId = infraServiceRepository.create(defaultService()
				.siteId(siteId)
				.name(UUID.randomUUID().toString())
				.policyId(policyId)
				.build());
		final String resourceType = resourceTypeRepository.create(defaultResourceType()
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

	private void createProjectAllocationChunk(String allocation, BigDecimal amount) {
		projectAllocationInstallationRepository.create(defaultProjectAllocationChunk()
			.projectAllocationId(allocation)
			.amount(amount)
			.build());
	}
}
