/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.rest.project;

import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.project_installation.Error;
import io.imunity.furms.domain.project_installation.ProjectInstallationResult;
import io.imunity.furms.domain.project_installation.ProjectInstallationStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.integration.tests.IntegrationTestBase;
import io.imunity.furms.integration.tests.tools.users.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.INSTALLED;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultCommunity;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultCommunityAllocation;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultPolicy;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultProject;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultProjectAllocation;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultProjectInstallationJob;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultResourceCredit;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultResourceType;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultService;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultSite;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultUserAddition;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.basicUser;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProjectsAllocationsIntegrationTest extends IntegrationTestBase {

	private Site site;
	private TestUser projectAdmin;

	@BeforeEach
	void setUp() {
		Site.SiteBuilder siteBuilder = defaultSite();
		site = siteBuilder
				.name("site1")
				.id(siteRepository.create(siteBuilder.build(), siteBuilder.build().getExternalId()))
				.build();
		projectAdmin = basicUser();
	}

	@Test
	void shouldFindAllProjectAllocationsByProjectId() throws Exception {
		//given
		final PolicyId policyId = policyDocumentRepository.create(defaultPolicy()
				.siteId(site.getId())
				.build());
		final String serviceName = UUID.randomUUID().toString();
		final String serviceId = infraServiceRepository.create(defaultService()
				.siteId(site.getId())
				.name(serviceName)
				.policyId(policyId)
				.build());
		final String resourceType = resourceTypeRepository.create(defaultResourceType()
				.siteId(site.getId())
				.serviceId(serviceId)
				.name(UUID.randomUUID().toString())
				.build());
		final String resourceCredit = resourceCreditRepository.create(defaultResourceCredit()
				.siteId(site.getId())
				.resourceTypeId(resourceType)
				.name("RC 1")
				.amount(BigDecimal.TEN)
				.build());
		final String community = createCommunity();
		final String communityAllocation = createCommunityAllocation(community, resourceCredit);
		final String project1 = createProject(community);
		final String project2 = createProject(community);
		final String projectAllocation1 = createProjectAllocation(communityAllocation, project1, BigDecimal.ONE);
		final String projectAllocation2 = createProjectAllocation(communityAllocation, project1, BigDecimal.ONE);
		createProjectAllocation(communityAllocation, project2, BigDecimal.ONE);

		projectAdmin.addProjectAdmin(community, project1);
		projectAdmin.addProjectAdmin(community, project2);
		setupUser(projectAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/projects/{projectId}/allocations", project1)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].id", in(Set.of(projectAllocation1, projectAllocation2))))
				.andExpect(jsonPath("$.[0].communityAllocationId", equalTo(communityAllocation)))
				.andExpect(jsonPath("$.[0].resourceTypeId", equalTo(resourceType)))
				.andExpect(jsonPath("$.[0].resourceUnit", equalTo("GB")))
				.andExpect(jsonPath("$.[0].siteId", equalTo(site.getId())))
				.andExpect(jsonPath("$.[0].siteName", equalTo(site.getName())))
				.andExpect(jsonPath("$.[0].serviceId", equalTo(serviceId)))
				.andExpect(jsonPath("$.[0].serviceName", equalTo(serviceName)))
				.andExpect(jsonPath("$.[0].amount", equalTo(1)))
				.andExpect(jsonPath("$.[1].id", in(Set.of(projectAllocation1, projectAllocation2))))
				.andExpect(jsonPath("$.[1].communityAllocationId", equalTo(communityAllocation)))
				.andExpect(jsonPath("$.[1].resourceTypeId", equalTo(resourceType)))
				.andExpect(jsonPath("$.[1].resourceUnit", equalTo("GB")))
				.andExpect(jsonPath("$.[1].siteId", equalTo(site.getId())))
				.andExpect(jsonPath("$.[1].siteName", equalTo(site.getName())))
				.andExpect(jsonPath("$.[1].serviceId", equalTo(serviceId)))
				.andExpect(jsonPath("$.[1].serviceName", equalTo(serviceName)))
				.andExpect(jsonPath("$.[1].amount", equalTo(1)));
	}

	@Test
	void shouldNotFindAllProjectAllocationsByProjectIdProjectDoesNotExist() throws Exception {
		//given
		final String community = createCommunity();

		projectAdmin.addCommunityAdmin(community);
		setupUser(projectAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/projects/{projectId}/allocations", UUID.randomUUID().toString())
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldFindProjectAllocationsByProjectIdWhenUserIsNotAnAdminButProjectIsInstalledOnManagedSite() throws Exception {
		//given
		final String resourceCredit = createResourceCredit(site.getId(), "RC 1", BigDecimal.TEN);
		final String community = createCommunity();
		final String project = createProject(community);
		final String communityAllocation = createCommunityAllocation(community, resourceCredit);
		createProjectInstallation(project, site.getId(), INSTALLED);
		createProjectAllocation(communityAllocation, project, BigDecimal.ONE);
		createProjectAllocation(communityAllocation, project, BigDecimal.ONE);

		final TestUser siteAdmin = basicUser();
		createUserAddition(project, site.getId(), siteAdmin);
		siteAdmin.addSiteAdmin(site.getId());
		setupUser(siteAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/projects/{projectId}/allocations", project)
				.with(siteAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)));
	}

	@Test
	void shouldFindProjectAllocationByProjectIdAndAllocationId() throws Exception {
		//given
		final String resourceCredit = createResourceCredit(site.getId(), "RC 1", BigDecimal.TEN);
		final String resourceType = resourceCreditRepository.findById(resourceCredit).get().resourceTypeId;
		final String community = createCommunity();
		final String communityAllocation = createCommunityAllocation(community, resourceCredit);
		final String project = createProject(community);
		final String projectAllocation1 = createProjectAllocation(communityAllocation, project, BigDecimal.valueOf(2));
		createProjectAllocation(communityAllocation, project, BigDecimal.ONE);

		projectAdmin.addProjectAdmin(community, project);
		projectAdmin.addProjectAdmin(community, project);
		setupUser(projectAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/projects/{projectId}/allocations/{allocationId}", project, projectAllocation1)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", equalTo(projectAllocation1)))
				.andExpect(jsonPath("$.communityAllocationId", equalTo(communityAllocation)))
				.andExpect(jsonPath("$.resourceTypeId", equalTo(resourceType)))
				.andExpect(jsonPath("$.amount", equalTo(2)));
	}

	@Test
	void shouldNotFindProjectAllocationByProjectIdAndAllocationIDueToLackOfCorrectRights() throws Exception {
		//given
		final String resourceCredit = createResourceCredit(site.getId(), "RC 1", BigDecimal.TEN);
		final String community = createCommunity();
		final String communityAllocation = createCommunityAllocation(community, resourceCredit);
		final String project1 = createProject(community);
		final String project2 = createProject(community);
		final String projectAllocation1 = createProjectAllocation(communityAllocation, project1, BigDecimal.valueOf(2));

		projectAdmin.addProjectAdmin(community, project2);
		setupUser(projectAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/projects/{projectId}/allocations/{allocationId}", project1, projectAllocation1)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldNotFindProjectAllocationByProjectIdAndAllocationWhenProjectDoesNotExist() throws Exception {
		//given
		final String resourceCredit = createResourceCredit(site.getId(), "RC 1", BigDecimal.TEN);
		final String community = createCommunity();
		final String communityAllocation = createCommunityAllocation(community, resourceCredit);
		final String project = createProject(community);
		final String projectAllocation1 = createProjectAllocation(communityAllocation, project, BigDecimal.valueOf(2));

		projectAdmin.addProjectAdmin(community, project);
		setupUser(projectAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/projects/{projectId}/allocations/{allocationId}",
				UUID.randomUUID().toString(), projectAllocation1)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldNotFindProjectAllocationByProjectIdAndAllocationAndReturnBadRequestWhenAllocationNotBelongsToProject() throws Exception {
		//given
		final String community = createCommunity();
		final String project = createProject(community);

		projectAdmin.addProjectAdmin(community, project);
		setupUser(projectAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/projects/{projectId}/allocations/{allocationId}",
				project, UUID.randomUUID().toString())
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldAddAllocationToProject() throws Exception {
		//given
		final String resourceCredit = createResourceCredit(site.getId(), "RC 1", BigDecimal.TEN);
		final String resourceType = resourceCreditRepository.findById(resourceCredit).get().resourceTypeId;
		final String community = createCommunity();
		final String communityAllocation = createCommunityAllocation(community, resourceCredit);
		final String project = createProject(community);

		projectAdmin.addCommunityAdmin(community);
		setupUser(projectAdmin);

		final ProjectAllocationAddRequest request = new ProjectAllocationAddRequest(
				communityAllocation,
				community,
				UUID.randomUUID().toString(),
				resourceType,
				BigDecimal.ONE);

		//when
		mockMvc.perform(post("/rest-api/v1/projects/{projectId}/allocations", project)
				.content(objectMapper.writeValueAsString(request))
				.contentType(APPLICATION_JSON)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$.[0].communityAllocationId", equalTo(communityAllocation)))
				.andExpect(jsonPath("$.[0].resourceTypeId", equalTo(resourceType)))
				.andExpect(jsonPath("$.[0].resourceUnit", equalTo("GB")))
				.andExpect(jsonPath("$.[0].siteId", equalTo(site.getId())))
				.andExpect(jsonPath("$.[0].siteName", equalTo(site.getName())))
				.andExpect(jsonPath("$.[0].serviceId").isNotEmpty())
				.andExpect(jsonPath("$.[0].serviceName").isNotEmpty())
				.andExpect(jsonPath("$.[0].amount", equalTo(1)));
	}

	@Test
	void shouldNotAllowAddAllocationToProjectDueToWrongRequest() throws Exception {
		//given
		final String resourceCredit = createResourceCredit(site.getId(), "RC 1", BigDecimal.TEN);
		final String resourceType = resourceCreditRepository.findById(resourceCredit).get().resourceTypeId;
		final String community = createCommunity();
		final String project = createProject(community);

		projectAdmin.addCommunityAdmin(community);
		setupUser(projectAdmin);

		final ProjectAllocationAddRequest request = new ProjectAllocationAddRequest(
				null,
				community,
				UUID.randomUUID().toString(),
				resourceType,
				BigDecimal.ONE);

		//when
		mockMvc.perform(post("/rest-api/v1/projects/{projectId}/allocations", project)
				.content(objectMapper.writeValueAsString(request))
				.contentType(APPLICATION_JSON)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isBadRequest());

		mockMvc.perform(post("/rest-api/v1/projects/{projectId}/allocations", project)
				.contentType(APPLICATION_JSON)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldNotAllowAddAllocationToProjectDueToLackOfRights() throws Exception {
		//given
		final String resourceCredit = createResourceCredit(site.getId(), "RC 1", BigDecimal.TEN);
		final String resourceType = resourceCreditRepository.findById(resourceCredit).get().resourceTypeId;
		final String community = createCommunity();
		final String communityAllocation = createCommunityAllocation(community, resourceCredit);
		final String project = createProject(community);

		setupUser(projectAdmin);

		final ProjectAllocationAddRequest request = new ProjectAllocationAddRequest(
				communityAllocation,
				community,
				UUID.randomUUID().toString(),
				resourceType,
				BigDecimal.ONE);

		//when
		mockMvc.perform(post("/rest-api/v1/projects/{projectId}/allocations", project)
				.content(objectMapper.writeValueAsString(request))
				.contentType(APPLICATION_JSON)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}


	@Test
	void shouldFindAllocationByProjectIdAndAllocationIdWhenUserIsNotAnAdminButProjectIsInstalledOnManagedSite() throws Exception {
		//given
		final String resourceCredit = createResourceCredit(site.getId(), "RC 1", BigDecimal.TEN);
		final String community = createCommunity();
		final String project = createProject(community);
		final String communityAllocation = createCommunityAllocation(community, resourceCredit);
		createProjectInstallation(project, site.getId(), INSTALLED);
		final String projectAllocation = createProjectAllocation(communityAllocation, project, BigDecimal.ONE);

		final TestUser siteAdmin = basicUser();
		createUserAddition(project, site.getId(), siteAdmin);
		siteAdmin.addSiteAdmin(site.getId());
		setupUser(siteAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/projects/{projectId}/allocations/{allocationId}",
					project, projectAllocation)
				.with(siteAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(projectAllocation));
	}

	private String createCommunity() {
		return communityRepository.create(defaultCommunity()
				.name(UUID.randomUUID().toString())
				.build());
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

	private String createCommunityAllocation(String communityId, String resourceCredit) {
		return communityAllocationRepository.create(defaultCommunityAllocation()
				.communityId(communityId)
				.resourceCreditId(resourceCredit)
				.name(UUID.randomUUID().toString())
				.amount(BigDecimal.TEN)
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

	private void createProjectInstallation(String projectId, String siteId, ProjectInstallationStatus status) {
		final String id = projectOperationRepository.createOrUpdate(defaultProjectInstallationJob()
				.projectId(projectId)
				.siteId(siteId)
				.status(status)
				.build());
		if (status == INSTALLED) {
			projectOperationRepository.update(id, new ProjectInstallationResult(Map.of(
					"gid", UUID.randomUUID().toString()), INSTALLED, new Error("", "")));
		}
	}

	private void createUserAddition(String projectId, String siteId, TestUser testUser) {
		userOperationRepository.create(defaultUserAddition()
			.projectId(projectId)
			.siteId(new SiteId(siteId))
			.userId(testUser.getFenixId())
			.correlationId(new CorrelationId(UUID.randomUUID().toString()))
			.build());
	}

	private static class ProjectAllocationAddRequest {
		public final String communityAllocationId;
		public final String communityId;
		public final String name;
		public final String resourceTypeId;
		public final BigDecimal amount;

		public ProjectAllocationAddRequest(String communityAllocationId, String communityId, String name,
		                                   String resourceTypeId, BigDecimal amount) {
			this.communityAllocationId = communityAllocationId;
			this.communityId = communityId;
			this.name = name;
			this.resourceTypeId = resourceTypeId;
			this.amount = amount;
		}
	}

}
