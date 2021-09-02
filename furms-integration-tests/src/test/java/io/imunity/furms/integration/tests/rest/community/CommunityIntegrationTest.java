/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.rest.community;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.integration.tests.IntegrationTestBase;
import io.imunity.furms.integration.tests.tools.users.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.edu.icm.unity.types.basic.AttributeExt;
import pl.edu.icm.unity.types.basic.GroupMember;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
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
import static java.util.stream.Collectors.toSet;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

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

		ADMIN_USER.addCommunityAdmin(community1);
		server.stubFor(WireMock.get("/unity/group-members/%2Ffenix%2Fcommunities%2F"+community1+"%2Fusers")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(objectMapper.writeValueAsString(Set.of(new GroupMember(
								"/fenix/communities/"+community1+"/users", ADMIN_USER.getEntity(),
								ADMIN_USER.getAttributes().values().stream()
										.flatMap(Collection::stream)
										.map(attribute -> new AttributeExt(attribute, false))
										.collect(toSet())))))));
		server.stubFor(WireMock.get("/unity/group-members/%2Ffenix%2Fcommunities%2F"+community2+"%2Fusers")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(objectMapper.writeValueAsString(Set.of()))));

		//when
		mockMvc.perform(adminGET("/rest-api/v1/communities"))
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
	void shouldNotFindAllCommunitiesDueToLackOfRights() throws Exception {
		//when
		mockMvc.perform(adminGET("/rest-api/v1/communities"))
				.andDo(print())
				.andExpect(status().isNotFound());
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
		final String resourceCredit = createResourceCredit(site.getId(), "RC 1", BigDecimal.TEN);
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
				.andExpect(jsonPath("$.[0].amount", equalTo(10)))
				.andExpect(jsonPath("$.[1].id", in(Set.of(communityAllocation1, communityAllocation2))))
				.andExpect(jsonPath("$.[1].creditId", equalTo(resourceCredit)))
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
}
