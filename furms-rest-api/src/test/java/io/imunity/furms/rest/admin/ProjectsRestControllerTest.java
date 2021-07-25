/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.admin;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.imunity.furms.rest.error.exceptions.ProjectRestNotFoundException;

class ProjectsRestControllerTest extends RestApiControllerIntegrationTest {

	private final static String BASE_URL_PROJECTS = "/rest-api/v1/projects";

	@Test
	void shouldFindAllCommunities() throws Exception {
		//given
		when(projectsRestService.findAll()).thenReturn(List.of(createProject("id1"), createProject("id2")));

		//when + then
		mockMvc.perform(get(BASE_URL_PROJECTS)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[0].id").value("id1"))
				.andExpect(jsonPath("$.[1].id").value("id2"))
				.andExpect(jsonPath("$", hasSize(2)));
	}

	@Test
	void shouldFindProjectByProjectId() throws Exception {
		//given
		final String projectId = "projectId";
		when(projectsRestService.findOneById(projectId)).thenReturn(createProjectWithUsers(projectId));

		//when + then
		mockMvc.perform(get(BASE_URL_PROJECTS+"/{projectId}", projectId)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.project.id").value(projectId))
				.andExpect(jsonPath("$.project.communityId").value("communityId"))
				.andExpect(jsonPath("$.project.acronym").value("acronym"))
				.andExpect(jsonPath("$.project.gid").value("gid"))
				.andExpect(jsonPath("$.project.name").value("name"))
				.andExpect(jsonPath("$.project.description").value("description"))
				.andExpect(jsonPath("$.project.validity.from").isNotEmpty())
				.andExpect(jsonPath("$.project.validity.to").isNotEmpty())
				.andExpect(jsonPath("$.project.researchField").value("researchField"))
				.andExpect(jsonPath("$.project.projectLeader.fenixIdentifier").value("fenixIdentifier"))
				.andExpect(jsonPath("$.project.projectLeader.title").value("title"))
				.andExpect(jsonPath("$.project.projectLeader.firstname").value("firstname"))
				.andExpect(jsonPath("$.project.projectLeader.lastname").value("lastname"))
				.andExpect(jsonPath("$.project.projectLeader.email").value("email"))
				.andExpect(jsonPath("$.project.projectLeader.affiliation.name").value("name"))
				.andExpect(jsonPath("$.project.projectLeader.affiliation.email").value("email"))
				.andExpect(jsonPath("$.project.projectLeader.affiliation.country").value("country"))
				.andExpect(jsonPath("$.project.projectLeader.affiliation.postalAddress").value("postalAddress"))
				.andExpect(jsonPath("$.project.projectLeader.nationality").value("nationality"))
				.andExpect(jsonPath("$.project.projectLeader.dateOfBirth").isNotEmpty())
				.andExpect(jsonPath("$.project.projectLeader.placeOfBirth").value("placeOfBirth"))
				.andExpect(jsonPath("$.project.projectLeader.postalAddress").value("postalAddress"))
				.andExpect(jsonPath("$.userFenixUserIds", hasSize(2)));
	}

	@Test
	void shouldReturn404IfProjectNotFound() throws Exception {
		//given
		final String projectId = "projectId";
		when(projectsRestService.findOneById(projectId)).thenThrow(new ProjectRestNotFoundException("message"));

		//when + then
		mockMvc.perform(get(BASE_URL_PROJECTS+"/{projectId}", projectId)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldCallDeleteProject() throws Exception {
		//given
		final String projectId = "projectId";
		when(projectsRestService.findOneById(projectId)).thenReturn(createProjectWithUsers(projectId));

		//when + then
		mockMvc.perform(delete(BASE_URL_PROJECTS+"/{projectId}", projectId)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk());

		verify(projectsRestService, times(1)).delete(projectId);
	}

	@Test
	void shouldReturn404IfProjectNotFoundDuringDelete() throws Exception {
		//given
		final String projectId = "projectId";
		doThrow(new ProjectRestNotFoundException("message")).when(projectsRestService).delete(projectId);

		//when + then
		mockMvc.perform(delete(BASE_URL_PROJECTS+"/{projectId}", projectId)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldCallUpdateWithProperBody() throws Exception {
		//given
		final String projectId = "projectId";
		final ProjectMutableDefinition request = new ProjectMutableDefinition("name", "description",
				new Validity(sampleFrom, sampleTo), "researchField", sampleUser);
		when(projectsRestService.update(projectId, request)).thenReturn(createProject(projectId));

		//when + then
		mockMvc.perform(put(BASE_URL_PROJECTS+"/{projectId}", projectId)
				.header(AUTHORIZATION, authKey())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk());

		verify(projectsRestService, times(1)).update(projectId, request);
	}

	@Test
	void shouldCallAddProjectWithProperBody() throws Exception {
		//given
		final String projectId = "projectId";
		final ProjectDefinition request = new ProjectDefinition("name", "description", new Validity(sampleFrom, sampleTo),
				"researchField", sampleUser, "communityId", "acronym", "gid");
		when(projectsRestService.create(request)).thenReturn(createProject(projectId));

		//when + then
		mockMvc.perform(post(BASE_URL_PROJECTS)
				.header(AUTHORIZATION, authKey())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk());

		verify(projectsRestService, times(1)).create(request);
	}

	@Test
	void shouldFindAllProjectAllocationsByProjectId() throws Exception {
		//given
		final String projectId = "projectId";
		when(projectsRestService.findAllProjectAllocationsByProjectId(projectId)).thenReturn(List.of(
				createProjectAllocation("id1"), createProjectAllocation("id2")));

		//when + then
		mockMvc.perform(get(BASE_URL_PROJECTS + "/{projectId}/allocations", projectId)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[0].id.allocationId").value("id1"))
				.andExpect(jsonPath("$.[1].id.allocationId").value("id2"))
				.andExpect(jsonPath("$", hasSize(2)));
	}

	@Test
	void shouldFindProjectByProjectIdAndAllocationId() throws Exception {
		//given
		final String projectId = "projectId";
		final String allocationId = "allocationId";
		when(projectsRestService.findByIdAndProjectAllocationId(projectId, allocationId))
				.thenReturn(createProjectAllocation(allocationId));

		//when + then
		mockMvc.perform(get(BASE_URL_PROJECTS+"/{projectId}/allocations/{projectAllocationId}", projectId, allocationId)
				.header(AUTHORIZATION, authKey()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id.projectId").value(projectId))
				.andExpect(jsonPath("$.id.allocationId").value(allocationId))
				.andExpect(jsonPath("$.communityAllocationId.communityId").value("communityId"))
				.andExpect(jsonPath("$.communityAllocationId.allocationId").value("allocationId"))
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
	void shouldCallAddProjectAllocationWithProperBody() throws Exception {
		//given
		final String projectId = "projectId";
		final ProjectAllocationDefinition request = new ProjectAllocationDefinition(
				new CommunityAllocationId("communityId", "allocationId"),
				"name",
				new ResourceType(new ResourceTypeId("siteId", "typeId"), "name", new ServiceId("siteId", "serviceId")),
				new ResourceAmount(BigDecimal.ONE, "none"));

		//when + then
		mockMvc.perform(post(BASE_URL_PROJECTS+"/{projectId}/allocations", projectId)
				.header(AUTHORIZATION, authKey())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk());

		verify(projectsRestService, times(1)).addAllocation(projectId, request);
	}

	private ProjectWithUsers createProjectWithUsers(String id) {
		return new ProjectWithUsers(createProject(id), List.of("fenixId1", "fenixId2"));
	}

	private Project createProject(String id) {
		return new Project("name", "description", new Validity(sampleFrom, sampleTo), "researchField", sampleUser, "communityId",
				"acronym", "gid", id);
	}

	private ProjectAllocation createProjectAllocation(String id) {
		return new ProjectAllocation(
				new CommunityAllocationId("communityId", "allocationId"),
				"name",
				new ResourceType(new ResourceTypeId("siteId", "typeId"), "name", new ServiceId("siteId", "serviceId")),
				new ResourceAmount(BigDecimal.ONE, "none"),
				new ProjectAllocationId("projectId", id));
	}

}