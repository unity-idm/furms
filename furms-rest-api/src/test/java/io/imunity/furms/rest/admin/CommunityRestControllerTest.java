/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.admin;

import io.imunity.furms.rest.error.exceptions.CommunityAllocationRestNotFoundException;
import io.imunity.furms.rest.error.exceptions.CommunityRestNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

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
	void shouldFindAllRelatedProjectsByCommunityId() throws Exception {
		//given
		final String communityId = "communityId";
		final String projectId1 = "projectId1";
		final String projectId2 = "projectId2";
		when(communityRestService.findAllProjectsByCommunityId(communityId))
				.thenReturn(List.of(createProject(projectId1), createProject(projectId2)));

		//when + then
		mockMvc.perform(get(BASE_URL_COMMUNITIES+"/{communityId}/projects", communityId)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].id").value(projectId1))
				.andExpect(jsonPath("$.[0].communityId").value("communityId"))
				.andExpect(jsonPath("$.[0].acronym").value("acronym"))
				.andExpect(jsonPath("$.[0].installations[0].siteId").value("siteId"))
				.andExpect(jsonPath("$.[0].installations[0].gid").value("gid"))
				.andExpect(jsonPath("$.[0].name").value("name"))
				.andExpect(jsonPath("$.[0].description").value("description"))
				.andExpect(jsonPath("$.[0].validity.from").isNotEmpty())
				.andExpect(jsonPath("$.[0].validity.to").isNotEmpty())
				.andExpect(jsonPath("$.[0].researchField").value("researchField"))
				.andExpect(jsonPath("$.[0].projectLeader.fenixIdentifier").value("fenixIdentifier"))
				.andExpect(jsonPath("$.[0].projectLeader.title").value("title"))
				.andExpect(jsonPath("$.[0].projectLeader.firstname").value("firstname"))
				.andExpect(jsonPath("$.[0].projectLeader.lastname").value("lastname"))
				.andExpect(jsonPath("$.[0].projectLeader.email").value("email"))
				.andExpect(jsonPath("$.[0].projectLeader.affiliation.name").value("name"))
				.andExpect(jsonPath("$.[0].projectLeader.affiliation.email").value("email"))
				.andExpect(jsonPath("$.[0].projectLeader.affiliation.country").value("country"))
				.andExpect(jsonPath("$.[0].projectLeader.affiliation.postalAddress").value("postalAddress"))
				.andExpect(jsonPath("$.[0].projectLeader.nationality").value("nationality"))
				.andExpect(jsonPath("$.[0].projectLeader.dateOfBirth").isNotEmpty())
				.andExpect(jsonPath("$.[0].projectLeader.placeOfBirth").value("placeOfBirth"))
				.andExpect(jsonPath("$.[0].projectLeader.postalAddress").value("postalAddress"));
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
		return new CommunityAllocation(id, "name", "creditId", "resourceUnit", "siteId", "siteName", "serviceId",
				"serviceName", ONE);
	}

	private Project createProject(String id) {
		return new Project(id, "acronym", "name", "communityId", "researchField",
				Set.of(new ProjectSiteInstallation("siteId", "gid")), "description",
				new Validity(sampleFrom, sampleTo), sampleUser);
	}

}