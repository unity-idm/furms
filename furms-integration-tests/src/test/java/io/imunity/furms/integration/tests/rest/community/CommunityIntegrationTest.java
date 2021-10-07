/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.rest.community;

import io.imunity.furms.domain.generic_groups.GenericGroup;
import io.imunity.furms.domain.generic_groups.GenericGroupMembership;
import io.imunity.furms.domain.generic_groups.GenericGroupId;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.integration.tests.IntegrationTestBase;
import io.imunity.furms.integration.tests.tools.users.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultCommunity;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultCommunityAllocation;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultPolicy;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultProject;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultResourceCredit;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultResourceType;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultService;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultSite;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.basicUser;
import static java.math.BigDecimal.ONE;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CommunityIntegrationTest extends IntegrationTestBase {

	private Site site;
	private TestUser communityAdmin;

	@BeforeEach
	void setUp() {
		Site.SiteBuilder siteBuilder = defaultSite();
		site = siteBuilder
				.name("site1")
				.id(siteRepository.create(siteBuilder.build(), siteBuilder.build().getExternalId()))
				.build();
		communityAdmin = basicUser();
	}

	@Test
	void shouldFindAllCommunitiesThatBelongsToUser() throws Exception {
		//given
		final String resourceCredit = createResourceCredit(site.getId(), "RC 1", BigDecimal.TEN);
		final String community1 = createCommunity();
		final String community2 = createCommunity();
		final String communityAllocation1 = createCommunityAllocation(community1, resourceCredit);
		final String communityAllocation2 = createCommunityAllocation(community1, resourceCredit);
		final String communityAllocation3 = createCommunityAllocation(community2, resourceCredit);

		final TestUser user = basicUser();
		user.addCommunityAdmin(community1);

		setupUser(user);

		//when
		mockMvc.perform(get("/rest-api/v1/communities")
				.with(user.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$.[0].id", equalTo(community1)))
				.andExpect(jsonPath("$.[0].name", notNullValue()))
				.andExpect(jsonPath("$.[0].allocations", hasSize(2)))
				.andExpect(jsonPath("$.[0].allocations").value(anyOf(
						containsInAnyOrder(communityAllocation1, communityAllocation2))));
	}

	@Test
	void shouldGetAllCommunitiesAsFenixAdmin() throws Exception {
		//given
		final String community1 = createCommunity();
		final String community2 = createCommunity();
		final String community3 = createCommunity();
		final String community4 = createCommunity();

		//when
		mockMvc.perform(adminGET("/rest-api/v1/communities"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(4)));
	}

	@Test
	void shouldFindCommunityById() throws Exception {
		//given
		final String resourceCredit = createResourceCredit(site.getId(), "RC 1", BigDecimal.TEN);
		final String community1 = createCommunity();
		final String community2 = createCommunity();
		final String communityAllocation1 = createCommunityAllocation(community1, resourceCredit);
		final String communityAllocation2 = createCommunityAllocation(community1, resourceCredit);
		final String communityAllocation3 = createCommunityAllocation(community2, resourceCredit);

		communityAdmin.addCommunityAdmin(community1);
		setupUser(communityAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/communities/{communityId}", community1)
				.with(communityAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", equalTo(community1)))
				.andExpect(jsonPath("$.name", notNullValue()))
				.andExpect(jsonPath("$.allocations", hasSize(2)))
				.andExpect(jsonPath("$.allocations").value(anyOf(
						containsInAnyOrder(communityAllocation1, communityAllocation2))));
	}

	@Test
	void shouldNotFoundCommunityByIdDueToLackOfRights() throws Exception {
		//given
		final String community1 = createCommunity();

		setupUser(communityAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/communities/{communityId}", community1)
				.with(communityAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldNotFoundCommunityByIdWhenCommunityDoesNotExist() throws Exception {
		//given
		setupUser(communityAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/communities/{communityId}", UUID.randomUUID().toString())
				.with(communityAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldFindAllProjectsByCommunityId() throws Exception {
		//given
		final String community1 = createCommunity();
		final String community2 = createCommunity();
		final String project1 = createProject(community1);
		final String project2 = createProject(community1);
		final String project3 = createProject(community2);

		//when
		mockMvc.perform(adminGET("/rest-api/v1/communities/{communityId}/projects", community1))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].id", in(Set.of(project1, project2))))
				.andExpect(jsonPath("$.[0].communityId", equalTo(community1)));
	}

	@Test
	void shouldNotFindAllProjectsByCommunityIdDueToLackOfCorrectRights() throws Exception {
		//given
		final String community1 = createCommunity();
		final String community2 = createCommunity();
		final String project1 = createProject(community1);
		final String project2 = createProject(community1);
		final String project3 = createProject(community2);

		communityAdmin.addCommunityAdmin(community2);
		setupUser(communityAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/communities/{communityId}/projects", community1)
				.with(communityAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldNotFindAllProjectsByCommunityIdWhenCommunityDoesNotExists() throws Exception {
		//when
		mockMvc.perform(adminGET("/rest-api/v1/communities/{communityId}/projects", UUID.randomUUID().toString()))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldFindAllAllocationsByCommunityId() throws Exception {
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
		final String community1 = createCommunity();
		final String community2 = createCommunity();
		final String communityAllocation1 = createCommunityAllocation(community1, resourceCredit);
		final String communityAllocation2 = createCommunityAllocation(community1, resourceCredit);
		final String communityAllocation3 = createCommunityAllocation(community2, resourceCredit);

		//when
		mockMvc.perform(adminGET("/rest-api/v1/communities/{communityId}/allocations", community1))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].id", in(Set.of(communityAllocation1, communityAllocation2))))
				.andExpect(jsonPath("$.[0].creditId", equalTo(resourceCredit)))
				.andExpect(jsonPath("$.[0].resourceUnit", equalTo("GB")))
				.andExpect(jsonPath("$.[0].siteId", equalTo(site.getId())))
				.andExpect(jsonPath("$.[0].siteName", equalTo(site.getName())))
				.andExpect(jsonPath("$.[0].serviceId", equalTo(serviceId)))
				.andExpect(jsonPath("$.[0].serviceName", equalTo(serviceName)))
				.andExpect(jsonPath("$.[0].amount", equalTo(10)))
				.andExpect(jsonPath("$.[1].id", in(Set.of(communityAllocation1, communityAllocation2))))
				.andExpect(jsonPath("$.[1].creditId", equalTo(resourceCredit)))
				.andExpect(jsonPath("$.[1].resourceUnit", equalTo("GB")))
				.andExpect(jsonPath("$.[1].siteId", equalTo(site.getId())))
				.andExpect(jsonPath("$.[1].siteName", equalTo(site.getName())))
				.andExpect(jsonPath("$.[1].serviceId", equalTo(serviceId)))
				.andExpect(jsonPath("$.[1].serviceName", equalTo(serviceName)))
				.andExpect(jsonPath("$.[1].amount", equalTo(10)));
	}

	@Test
	void shouldNotFindAllAllocationsByCommunityIdDueToLackOfCorrectRights() throws Exception {
		//given
		final String resourceCredit = createResourceCredit(site.getId(), "RC 1", BigDecimal.TEN);
		final String community1 = createCommunity();
		final String community2 = createCommunity();
		createCommunityAllocation(community1, resourceCredit);
		createCommunityAllocation(community1, resourceCredit);
		createCommunityAllocation(community2, resourceCredit);

		setupUser(communityAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/communities/{communityId}/allocations", community1)
				.with(communityAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldNotFindAllAllocationsByCommunityIdWhenCommunityDoesNotExist() throws Exception {
		//when
		mockMvc.perform(adminGET("/rest-api/v1/communities/{communityId}/allocations", UUID.randomUUID().toString()))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldFindAllocationByIdAndCommunityId() throws Exception {
		//given
		final String resourceCredit = createResourceCredit(site.getId(), "RC 1", BigDecimal.TEN);
		final String community = createCommunity();
		final String communityAllocation1 = createCommunityAllocation(community, resourceCredit);
		final String communityAllocation2 = createCommunityAllocation(community, resourceCredit);

		//when
		mockMvc.perform(adminGET("/rest-api/v1/communities/{communityId}/allocations/{communityAllocationId}",
					community, communityAllocation1))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", equalTo(communityAllocation1)))
				.andExpect(jsonPath("$.creditId", equalTo(resourceCredit)))
				.andExpect(jsonPath("$.resourceUnit", equalTo("GB")))
				.andExpect(jsonPath("$.siteId", equalTo(site.getId())))
				.andExpect(jsonPath("$.siteName", equalTo(site.getName())))
				.andExpect(jsonPath("$.serviceId").isNotEmpty())
				.andExpect(jsonPath("$.serviceName").isNotEmpty())
				.andExpect(jsonPath("$.amount", equalTo(10)));
	}

	@Test
	void shouldNotFindFindAllocationByIdAndCommunityIdDueToLackOfCorrectRights() throws Exception {
		//given
		final String resourceCredit = createResourceCredit(site.getId(), "RC 1", BigDecimal.TEN);
		final String community = createCommunity();
		final String communityAllocation = createCommunityAllocation(community, resourceCredit);

		setupUser(communityAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/communities/{communityId}/allocations/{communityAllocationId}",
				community, communityAllocation)
				.with(communityAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldNotFindFindAllocationByIdAndCommunityIdWhenIdOrCommunityIdIsWrong() throws Exception {
		//given
		final String resourceCredit = createResourceCredit(site.getId(), "RC 1", BigDecimal.TEN);
		final String community = createCommunity();
		final String communityAllocation = createCommunityAllocation(community, resourceCredit);

		//when
		mockMvc.perform(adminGET("/rest-api/v1/communities/{communityId}/allocations/{communityAllocationId}",
				UUID.randomUUID().toString(), communityAllocation))
				.andDo(print())
				.andExpect(status().isNotFound());

		mockMvc.perform(adminGET("/rest-api/v1/communities/{communityId}/allocations/{communityAllocationId}",
				community, UUID.randomUUID().toString()))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldAddCommunityAllocationToCommunity() throws Exception {
		//given
		final String resourceCredit = createResourceCredit(site.getId(), "RC 1", BigDecimal.TEN);
		final String community = createCommunity();

		final CommunityAllocationAddRequest request = new CommunityAllocationAddRequest(resourceCredit, "Test 1", ONE);

		//when
		mockMvc.perform(post("/rest-api/v1/communities/{communityId}/allocations", community)
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.with(ADMIN_USER.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$.[0].id", notNullValue()))
				.andExpect(jsonPath("$.[0].name", equalTo(request.name)))
				.andExpect(jsonPath("$.[0].creditId", equalTo(request.creditId)))
				.andExpect(jsonPath("$.[0].amount", equalTo(request.amount.intValue())));
	}

	@Test
	void shouldFindGroups() throws Exception {
		//given
		String community = createCommunity();
		String group1 = createGroup(community);
		String group2 = createGroup(community);

		//when
		mockMvc.perform(adminGET("/rest-api/v1/communities/{communityId}/groups", community))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(2)))
			.andExpect(jsonPath("$.[0].id", in(Set.of(group1, group2))))
			.andExpect(jsonPath("$.[0].name", equalTo("name")))
			.andExpect(jsonPath("$.[0].description", equalTo("description")))
			.andExpect(jsonPath("$.[1].id", in(Set.of(group1, group2))))
			.andExpect(jsonPath("$.[1].name", equalTo("name")))
			.andExpect(jsonPath("$.[1].description", equalTo("description")));
	}

	@Test
	void shouldFindGroupWithMembers() throws Exception {
		//given
		String community = createCommunity();
		String group = createGroup(community);
		createGroupMember(group, communityAdmin.getFenixId());

		//when
		mockMvc.perform(adminGET("/rest-api/v1/communities/{communityId}/groups/{groupId}", community, group))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id", equalTo(group)))
			.andExpect(jsonPath("$.name", equalTo("name")))
			.andExpect(jsonPath("$.description", equalTo("description")))
			.andExpect(jsonPath("$.memberFenixUserIds", equalTo(List.of(communityAdmin.getFenixId()))));
	}

	@Test
	void shouldAddGenericGroupToCommunity() throws Exception {
		//given
		String community = createCommunity();

		GroupDefinitionRequest request = new GroupDefinitionRequest("name", "description");

		//when
		mockMvc.perform(post("/rest-api/v1/communities/{communityId}/groups", community)
			.contentType(APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(request))
			.with(ADMIN_USER.getHttpBasic()))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id", notNullValue()))
			.andExpect(jsonPath("$.name", equalTo(request.name)))
			.andExpect(jsonPath("$.description", equalTo(request.description)));
	}

	@Test
	void shouldDeleteGenericGroupToCommunity() throws Exception {
		//given
		String community = createCommunity();
		String group = createGroup(community);

		//when
		mockMvc.perform(delete("/rest-api/v1/communities/{communityId}/groups/{groupId}", community, group)
			.with(ADMIN_USER.getHttpBasic()))
			.andDo(print())
			.andExpect(status().isOk());
	}

	@Test
	void shouldUpdateGenericGroupToCommunity() throws Exception {
		//given
		String community = createCommunity();
		String group = createGroup(community);

		GroupDefinitionRequest request = new GroupDefinitionRequest("name2", "description2");

		//when
		mockMvc.perform(put("/rest-api/v1/communities/{communityId}/groups/{groupId}", community, group)
			.contentType(APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(request))
			.with(ADMIN_USER.getHttpBasic()))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id", equalTo(group)))
			.andExpect(jsonPath("$.name", equalTo(request.name)))
			.andExpect(jsonPath("$.description", equalTo(request.description)));
	}

	@Test
	void shouldNotAllowToAddCommunityAllocationToCommunityDueToLackOfCorrectRights() throws Exception {
		//given
		final String resourceCredit = createResourceCredit(site.getId(), "RC 1", BigDecimal.TEN);
		final String community = createCommunity();

		setupUser(communityAdmin);

		final CommunityAllocationAddRequest request = new CommunityAllocationAddRequest(resourceCredit, "Test 1", ONE);

		//when
		mockMvc.perform(post("/rest-api/v1/communities/{communityId}/allocations", community)
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.with(communityAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldNotAllowToAddCommunityAllocationToCommunityAndReturnBadRequestForWrongOrEmptyRequest() throws Exception {
		//given
		final String community = createCommunity();

		final CommunityAllocationAddRequest request = new CommunityAllocationAddRequest(null, "Test 1", ONE);

		//when
		mockMvc.perform(post("/rest-api/v1/communities/{communityId}/allocations", community)
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.with(ADMIN_USER.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isBadRequest());

		mockMvc.perform(post("/rest-api/v1/communities/{communityId}/allocations", community)
				.contentType(APPLICATION_JSON)
				.with(ADMIN_USER.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldNotAllowToAddCommunityAllocationToCommunityWhenCommunityDoesNotExists() throws Exception {
		//given
		final String resourceCredit = createResourceCredit(site.getId(), "RC 1", BigDecimal.TEN);

		final CommunityAllocationAddRequest request = new CommunityAllocationAddRequest(resourceCredit, "Test 1", ONE);

		//when
		mockMvc.perform(post("/rest-api/v1/communities/{communityId}/allocations", UUID.randomUUID().toString())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.with(ADMIN_USER.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isNotFound());
	}


	private String createCommunity() {
		return communityRepository.create(defaultCommunity()
				.name(UUID.randomUUID().toString())
				.build());
	}

	private String createGroup(String communityId) {
		return genericGroupRepository.create(
			GenericGroup.builder()
				.name("name")
				.communityId(communityId)
				.description("description")
				.build()
		).id.toString();
	}

	private void createGroupMember(String genericGroupId, String userId) {
		genericGroupRepository.createMembership(
			GenericGroupMembership.builder()
				.fenixUserId(userId)
				.genericGroupId(new GenericGroupId(genericGroupId))
				.utcMemberSince(LocalDate.now().atStartOfDay())
				.build()
		);
	}

	private String createProject(String communityId) {
		return projectRepository.create(defaultProject()
				.communityId(communityId)
				.name(UUID.randomUUID().toString())
				.leaderId(new PersistentId(ADMIN_USER.getUserId()))
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

	private static class CommunityAllocationAddRequest {
		public final String creditId;
		public final String name;
		public final BigDecimal amount;

		public CommunityAllocationAddRequest(String creditId, String name, BigDecimal amount) {
			this.creditId = creditId;
			this.name = name;
			this.amount = amount;
		}
	}

	private static class GroupDefinitionRequest {
		public final String name;
		public final String description;

		public GroupDefinitionRequest(String name, String description) {
			this.name = name;
			this.description = description;
		}

	}
}
