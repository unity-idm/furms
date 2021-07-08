/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.admin;

import io.imunity.furms.rest.error.exceptions.CommunityAllocationRestNotFoundException;
import io.imunity.furms.rest.error.exceptions.CommunityRestNotFoundException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommunityRestControllerTest extends RestApiControllerIntegrationTest {

	private final static String BASE_URL_COMMUNITIES = "/rest-api/v1/communities";

	@Test
	void shouldFindAllCommunities() throws Exception {
		//given
		when(communityRestService.findAll()).thenReturn(List.of(createCommunity("id1"), createCommunity("id2")));

		//when + then
		mockMvc.perform(get(BASE_URL_COMMUNITIES)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[0].id").value("id1"))
				.andExpect(jsonPath("$.[1].id").value("id2"))
				.andExpect(jsonPath("$", hasSize(2)));
	}

	@Test
	void shouldFindCommunityByCommunityId() throws Exception {
		//given
		final String communityId = "communityId";
		when(communityRestService.findOneById(communityId)).thenReturn(createCommunity(communityId));

		//when + then
		mockMvc.perform(get(BASE_URL_COMMUNITIES+"/{communityId}", communityId)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(communityId))
				.andExpect(jsonPath("$.name").value("name1"))
				.andExpect(jsonPath("$.allocations", hasSize(2)))
				.andExpect(jsonPath("$.allocations[0]").value("allocation11"))
				.andExpect(jsonPath("$.allocations[1]").value("allocation12"));
	}

	@Test
	void shouldReturn404IfCommunityHasNotBeenFound() throws Exception {
		//given
		final String communityId = "communityId";
		when(communityRestService.findOneById(communityId)).thenThrow(new CommunityRestNotFoundException("message"));

		//when + then
		mockMvc.perform(get(BASE_URL_COMMUNITIES+"/{communityId}", communityId)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldFindCommunityAllocationsByCommunityId() throws Exception {
		//given
		final String communityId = "communityId";
		when(communityRestService.findAllocationByCommunityId(communityId)).thenReturn(List.of(
				createCommunityAllocation("id1", communityId),
				createCommunityAllocation("id2", communityId)));

		//when + then
		mockMvc.perform(get(BASE_URL_COMMUNITIES+"/{communityId}/allocations", communityId)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)));
	}

	@Test
	void shouldFindCommunityAllocationByCommunityIdAndId() throws Exception {
		//given
		final String communityId = "communityId";
		final String allocationId = "allocationId";
		when(communityRestService.findAllocationByIdAndCommunityId(allocationId, communityId)).thenReturn(
				createCommunityAllocation(allocationId, communityId));

		//when + then
		mockMvc.perform(get(BASE_URL_COMMUNITIES+"/{communityId}/allocations/{allocationId}",
					communityId, allocationId)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id.communityId").value(communityId))
				.andExpect(jsonPath("$.id.allocationId").value(allocationId))
				.andExpect(jsonPath("$.siteAllocationId.siteId").value("siteId"))
				.andExpect(jsonPath("$.siteAllocationId.creditId").value("creditId"))
				.andExpect(jsonPath("$.name").value("name"))
				.andExpect(jsonPath("$.resourceType.id.siteId").value("siteId"))
				.andExpect(jsonPath("$.resourceType.id.typeId").value("typeId"))
				.andExpect(jsonPath("$.resourceType.name").value("name"))
				.andExpect(jsonPath("$.resourceType.serviceId.siteId").value("siteId"))
				.andExpect(jsonPath("$.resourceType.serviceId.serviceId").value("serviceId"))
				.andExpect(jsonPath("$.amount.amount").value("1"))
				.andExpect(jsonPath("$.amount.unit").value("none"));
	}

	@Test
	void shouldReturn404IfCommunityAllocationHasNotBeenFoundInFindingCommunityAllocations() throws Exception {
		//given
		final String communityId = "communityId";
		final String allocationId = "allocationId";
		when(communityRestService.findAllocationByIdAndCommunityId(allocationId, communityId)).thenThrow(
				new CommunityAllocationRestNotFoundException("message"));

		//when + then
		mockMvc.perform(get(BASE_URL_COMMUNITIES+"/{communityId}/allocations/{allocationId}",
					communityId, allocationId)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldCallAddAllocationWithProperBody() throws Exception {
		//given
		final String communityId = "communityId";
		final CommunityAllocationDefinition request = new CommunityAllocationDefinition(
				new SiteCreditId("siteId", "creditId"),
				"name",
				new ResourceType(new ResourceTypeId("siteId", "typeId"), "name",
						new ServiceId("siteId", "serviceId")),
				new ResourceAmount(BigDecimal.ONE, "none"));
		when(communityRestService.addAllocation(communityId, request)).thenReturn(List.of());

		//when + then
		mockMvc.perform(post(BASE_URL_COMMUNITIES+"/{communityId}/allocations", communityId)
				.header(AUTHORIZATION, authKey())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk());

		verify(communityRestService, times(1)).addAllocation(communityId, request);
	}

	private Community createCommunity(String id) {
		return new Community(id, "name1", List.of("allocation11", "allocation12"));
	}

	private CommunityAllocation createCommunityAllocation(String id, String communityId) {
		return new CommunityAllocation(
				new SiteCreditId("siteId", "creditId"),
				"name",
				new ResourceType(new ResourceTypeId("siteId", "typeId"), "name",
						new ServiceId("siteId", "serviceId")),
				new ResourceAmount(BigDecimal.ONE, "none"),
				new CommunityAllocationId(communityId, id));
	}

}