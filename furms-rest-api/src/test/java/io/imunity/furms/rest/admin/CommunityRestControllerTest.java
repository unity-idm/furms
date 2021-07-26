/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.admin;

import io.imunity.furms.rest.error.exceptions.CommunityAllocationRestNotFoundException;
import io.imunity.furms.rest.error.exceptions.CommunityRestNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static java.math.BigDecimal.ONE;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
				.andExpect(jsonPath("$.id").value(allocationId))
				.andExpect(jsonPath("$.creditId").value("creditId"))
				.andExpect(jsonPath("$.name").value("name"))
				.andExpect(jsonPath("$.amount").value("1"));
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
		final CommunityAllocationAddRequest request = new CommunityAllocationAddRequest("creditId", "name", ONE);
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
		return new CommunityAllocation(id, "creditId", "name", ONE);
	}

}