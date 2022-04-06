/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.rest.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import io.imunity.furms.domain.project_installation.Error;
import io.imunity.furms.domain.project_installation.ProjectInstallationResult;
import io.imunity.furms.domain.project_installation.ProjectInstallationStatus;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.integration.tests.IntegrationTestBase;
import io.imunity.furms.integration.tests.tools.users.TestUser;
import io.imunity.furms.rest.admin.Validity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.ACKNOWLEDGED;
import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.INSTALLED;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultCommunity;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultProject;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultProjectInstallationJob;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultSite;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultUserAddition;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.basicUser;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

public class ProjectsIntegrationTest extends IntegrationTestBase {

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
	void shouldGetAllProjectsWithInstallationsForCurrentUser() throws Exception {
		//given
		final Site.SiteBuilder siteBuilder = defaultSite();
		final Site site2 = siteBuilder
				.name("site2")
				.externalId(new SiteExternalId("s2id"))
				.id(siteRepository.create(siteBuilder.build(), siteBuilder.build().getExternalId()))
				.build();
		final String community = createCommunity();
		final String project1 = createProject(community);
		final String project2 = createProject(community);
		createProjectInstallation(project1, site.getId(), INSTALLED);
		createProjectInstallation(project1, site2.getId(), ACKNOWLEDGED);
		createProjectInstallation(project2, site.getId(), INSTALLED);

		projectAdmin.addProjectAdmin(community, project1);
		setupUser(projectAdmin);

		//when
		final Project expectedProject = projectRepository.findById(project1).get();
		mockMvc.perform(MockMvcRequestBuilders.get("/rest-api/v1/projects")
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$.[0].id", equalTo(project1)))
				.andExpect(jsonPath("$.[0].acronym", equalTo(expectedProject.getAcronym())))
				.andExpect(jsonPath("$.[0].name", equalTo(expectedProject.getName())))
				.andExpect(jsonPath("$.[0].communityId", equalTo(expectedProject.getCommunityId())))
				.andExpect(jsonPath("$.[0].researchField", equalTo(expectedProject.getResearchField())))
				.andExpect(jsonPath("$.[0].installations", hasSize(2)))
				.andExpect(jsonPath("$.[0].description", equalTo(expectedProject.getDescription())))
				.andExpect(jsonPath("$.[0].validity", notNullValue()))
				.andExpect(jsonPath("$.[0].projectLeader.fenixIdentifier", equalTo(ADMIN_USER.getFenixId())));
	}

	@Test
	void shouldFindProjectsWhenUserIsAlreadyInstalledEvenHeIsNotAProjectAdmin() throws Exception {
		//given
		final Site.SiteBuilder siteBuilder = defaultSite();
		final Site site2 = siteBuilder
				.name("site2")
				.externalId(new SiteExternalId("s2id"))
				.id(siteRepository.create(siteBuilder.build(), siteBuilder.build().getExternalId()))
				.build();
		final String community = createCommunity();
		final String project1 = createProject(community);
		createProjectInstallation(project1, site.getId(), INSTALLED);
		createProjectInstallation(project1, site2.getId(), INSTALLED);

		final TestUser siteAdmin = basicUser();
		siteAdmin.addSiteAdmin(site.getId());
		setupUser(siteAdmin);

		//when
		mockMvc.perform(MockMvcRequestBuilders.get("/rest-api/v1/projects")
				.with(siteAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));
	}

	@Test
	void shouldFindProjectsWhenUserIsAlreadyInstalledEvenHeIsNotAnAnyAdmin() throws Exception {
		//given
		final Site.SiteBuilder siteBuilder = defaultSite();
		final Site site2 = siteBuilder
				.name("site2")
				.externalId(new SiteExternalId("s2id"))
				.id(siteRepository.create(siteBuilder.build(), siteBuilder.build().getExternalId()))
				.build();
		final String community = createCommunity();
		final String project1 = createProject(community);
		final TestUser siteAdmin = basicUser();
		createProjectInstallation(project1, site.getId(), INSTALLED);
		createProjectInstallation(project1, site2.getId(), INSTALLED);

		createUserAddition(project1, site.getId(), siteAdmin);
		setupUser(siteAdmin);

		//when
		mockMvc.perform(MockMvcRequestBuilders.get("/rest-api/v1/projects")
				.with(siteAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));
	}

	@Test
	void shouldFindProjectByProjectId() throws Exception {
		//given
		final String community = createCommunity();
		final String project1 = createProject(community);
		final String project2 = createProject(community);
		createProjectInstallation(project1, site.getId(), INSTALLED);
		createProjectInstallation(project2, site.getId(), INSTALLED);

		createUserAddition(project1, site.getId(), projectAdmin);
		createUserAddition(project1, site.getId(), ADMIN_USER);

		projectAdmin.addProjectAdmin(community, project1);
		setupUser(projectAdmin);

		//when
		final Project expectedProject = projectRepository.findById(project1).get();
		mockMvc.perform(get("/rest-api/v1/projects/{projectId}", project1)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.project.id", equalTo(project1)))
				.andExpect(jsonPath("$.project.acronym", equalTo(expectedProject.getAcronym())))
				.andExpect(jsonPath("$.project.name", equalTo(expectedProject.getName())))
				.andExpect(jsonPath("$.project.communityId", equalTo(expectedProject.getCommunityId())))
				.andExpect(jsonPath("$.project.researchField", equalTo(expectedProject.getResearchField())))
				.andExpect(jsonPath("$.project.installations", hasSize(1)))
				.andExpect(jsonPath("$.project.description", equalTo(expectedProject.getDescription())))
				.andExpect(jsonPath("$.project.validity", notNullValue()))
				.andExpect(jsonPath("$.project.projectLeader.fenixIdentifier", equalTo(ADMIN_USER.getFenixId())))
				.andExpect(jsonPath("$.userFenixUserIds").value(anyOf(
						containsInAnyOrder(ADMIN_USER.getFenixId(), projectAdmin.getFenixId()))));
	}

	@Test
	void shouldNotBeAbleToShowProjectWhenUserIsJustSiteAdmin() throws Exception {
		//given
		final String community = createCommunity();
		final String project1 = createProject(community);
		createProject(community);

		final TestUser siteAdmin = basicUser();
		siteAdmin.addSiteAdmin(site.getId());
		setupUser(siteAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/projects/{projectId}", project1)
				.with(siteAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldFindProjectWhenUserIsAlreadyInstalledEvenHeIsNotAProjectAdmin() throws Exception {
		//given
		final String community = createCommunity();
		final String project1 = createProject(community);
		final String project2 = createProject(community);
		createProjectInstallation(project1, site.getId(), INSTALLED);
		createProjectInstallation(project2, site.getId(), INSTALLED);
		final TestUser siteAdmin = basicUser();

		createUserAddition(project1, site.getId(), siteAdmin);
		createUserAddition(project2, site.getId(), ADMIN_USER);

		siteAdmin.addSiteAdmin(site.getId());
		setupUser(siteAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/projects/{projectId}", project1)
				.with(siteAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.project.id", equalTo(project1)));
	}

	@Test
	void shouldReturnForbiddenIfUserDoesNotHaveRightsToProjectWhileGettingProject() throws Exception {
		//given
		final String community = createCommunity();
		final String project1 = createProject(community);
		final String project2 = createProject(community);
		createProjectInstallation(project1, site.getId(), INSTALLED);
		createProjectInstallation(project2, site.getId(), INSTALLED);

		projectAdmin.addProjectAdmin(community, project1);
		setupUser(projectAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/projects/{projectId}", project2)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldReturnNotFoundIfProjectDoesNotExistsWhileGettingProject() throws Exception {
		//given
		setupUser(projectAdmin);

		//when
		mockMvc.perform(get("/rest-api/v1/projects/{projectId}", UUID.randomUUID().toString())
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldDeleteProjectByProjectId() throws Exception {
		//given
		final String community = createCommunity();
		final String project = createProject(community);

		server.stubFor(WireMock.get("/unity/group-members-attributes/%2Ffenix%2Fcommunities%2F"+community+"%2Fprojects%2F"+project+"%2Fusers")
				.willReturn(aResponse().withStatus(200)
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(new ObjectMapper().writeValueAsString(new String[0]))));
		server.stubFor(WireMock.delete("/unity/group/%2Ffenix%2Fcommunities%2F"+community+"%2Fprojects%2F"+project+"?recursive=true")
				.willReturn(aResponse().withStatus(200)));

		projectAdmin.addCommunityAdmin(community);
		setupUser(projectAdmin);

		//when
		mockMvc.perform(delete("/rest-api/v1/projects/{projectId}", project)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk());
		//then
		assertThat(projectRepository.findById(project)).isEmpty();
	}

	@Test
	void shouldNotAllowToDeleteWhenUserDoesNotHaveCorrectRights() throws Exception {
		//given
		final String community = createCommunity();
		final String project = createProject(community);

		//when
		setupUser(projectAdmin);
		mockMvc.perform(delete("/rest-api/v1/projects/{projectId}", project)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());

		projectAdmin.addProjectAdmin(community, project);
		projectAdmin.registerUserMock(server);
		mockMvc.perform(delete("/rest-api/v1/projects/{projectId}", project)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldNotAllowToDeleteWhenProjectDoesNotExists() throws Exception {
		setupUser(projectAdmin);
		//when
		mockMvc.perform(delete("/rest-api/v1/projects/{projectId}", UUID.randomUUID().toString())
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldUpdateProject() throws Exception {
		//given
		final String community = createCommunity();
		final String project = createProject(community);

		server.stubFor(WireMock.post("/unity/group/%2Ffenix%2Fcommunities%2F"+community+"%2Fprojects%2F"+project+"%2Fusers"+
							"/entity/"+projectAdmin.getUserId())
				.willReturn(aResponse().withStatus(200)));
		server.stubFor(WireMock.put("/unity/group/")
				.willReturn(aResponse().withStatus(200)));
		server.stubFor(WireMock.put("/unity/entity/"+projectAdmin.getUserId()+"/attribute")
				.willReturn(aResponse().withStatus(200)));

		projectAdmin.addProjectAdmin(community, project);
		projectAdmin.addCommunityAdmin(community);
		setupUser(projectAdmin);

		final ProjectUpdateRequest request = defaultUpdateProject();
		final Project old = projectRepository.findById(project).get();

		//when
		mockMvc.perform(put("/rest-api/v1/projects/{projectId}", project)
				.content(objectMapper.writeValueAsString(request))
				.contentType(APPLICATION_JSON)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", equalTo(project)))
				.andExpect(jsonPath("$.acronym", equalTo(old.getAcronym())))
				.andExpect(jsonPath("$.name", equalTo(request.name)))
				.andExpect(jsonPath("$.communityId", equalTo(old.getCommunityId())))
				.andExpect(jsonPath("$.researchField", equalTo(request.researchField)))
				.andExpect(jsonPath("$.description", equalTo(request.description)))
				.andExpect(jsonPath("$.projectLeader.fenixIdentifier", equalTo(projectAdmin.getFenixId())));
	}

	@Test
	void shouldNotAllowToUpdateProjectDueToEmptyRequiredFields() throws Exception {
		//given
		final String community = createCommunity();
		final String project = createProject(community);

		projectAdmin.addProjectAdmin(community, project);
		projectAdmin.addCommunityAdmin(community);
		setupUser(projectAdmin);

		final ProjectUpdateRequest request = new ProjectUpdateRequest(
				null,
				"new Description",
				new Validity(LocalDateTime.now(), LocalDateTime.now().plusDays(1)),
				"new researchField",
				projectAdmin.getFenixId());

		//when
		mockMvc.perform(put("/rest-api/v1/projects/{projectId}", project)
				.content(objectMapper.writeValueAsString(request))
				.contentType(APPLICATION_JSON)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isBadRequest());

		mockMvc.perform(put("/rest-api/v1/projects/{projectId}", project)
				.contentType(APPLICATION_JSON)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldNotAllowToUpdateProjectDueToLackOfCorrectRights() throws Exception {
		//given
		final String community = createCommunity();
		final String project = createProject(community);

		setupUser(projectAdmin);

		final ProjectUpdateRequest request = defaultUpdateProject();

		//when
		mockMvc.perform(put("/rest-api/v1/projects/{projectId}", project)
				.content(objectMapper.writeValueAsString(request))
				.contentType(APPLICATION_JSON)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldNotAllowToUpdateProjectWhenProjectDoesNotExist() throws Exception {
		//given
		setupUser(projectAdmin);

		final ProjectUpdateRequest request = defaultUpdateProject();

		//when
		mockMvc.perform(put("/rest-api/v1/projects/{projectId}", UUID.randomUUID().toString())
				.content(objectMapper.writeValueAsString(request))
				.contentType(APPLICATION_JSON)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldCreateProject() throws Exception {
		//given
		final String community = createCommunity();

		server.stubFor(WireMock.post(new UrlPattern(matching(
				"/unity/group/.*fenix.*communities.*projects.*users.*"), true))
				.willReturn(aResponse().withStatus(200)));
		server.stubFor(WireMock.put("/unity/group/")
				.willReturn(aResponse().withStatus(200)));
		server.stubFor(WireMock.put("/unity/entity/"+projectAdmin.getUserId()+"/attribute")
				.willReturn(aResponse().withStatus(200)));

		projectAdmin.addCommunityAdmin(community);
		setupUser(projectAdmin);

		final ProjectCreateRequest request = defaultCreateRequest(community);

		//when
		mockMvc.perform(post("/rest-api/v1/projects")
				.content(objectMapper.writeValueAsString(request))
				.contentType(APPLICATION_JSON)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", notNullValue()))
				.andExpect(jsonPath("$.acronym", equalTo(request.acronym)))
				.andExpect(jsonPath("$.name", equalTo(request.name)))
				.andExpect(jsonPath("$.communityId", equalTo(request.communityId)))
				.andExpect(jsonPath("$.researchField", equalTo(request.researchField)))
				.andExpect(jsonPath("$.installations", hasSize(0)))
				.andExpect(jsonPath("$.description", equalTo(request.description)))
				.andExpect(jsonPath("$.validity", notNullValue()))
				.andExpect(jsonPath("$.projectLeader.fenixIdentifier", equalTo(projectAdmin.getFenixId())));
	}

	@Test
	void shouldNotAllowToCreateProjectDueToEmptyRequiredFields() throws Exception {
		//given
		final String community = createCommunity();

		projectAdmin.addCommunityAdmin(community);
		setupUser(projectAdmin);

		final ProjectCreateRequest request = new ProjectCreateRequest(
				community,
				"acronym",
				"gid",
				null,
				"description",
				new Validity(LocalDateTime.now(), LocalDateTime.now().plusDays(1)),
				"researchField",
				projectAdmin.getFenixId());

		//when
		mockMvc.perform(post("/rest-api/v1/projects")
				.content(objectMapper.writeValueAsString(request))
				.contentType(APPLICATION_JSON)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isBadRequest());

		mockMvc.perform(post("/rest-api/v1/projects")
				.contentType(APPLICATION_JSON)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldNotAllowToCreateProjectDueToLackOfCorrectRights() throws Exception {
		//given
		final String community = createCommunity();

		setupUser(projectAdmin);

		final ProjectCreateRequest request = defaultCreateRequest(community);

		//when
		mockMvc.perform(post("/rest-api/v1/projects")
				.content(objectMapper.writeValueAsString(request))
				.contentType(APPLICATION_JSON)
				.with(projectAdmin.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());
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

	private ProjectUpdateRequest defaultUpdateProject() {
		return new ProjectUpdateRequest(
				"name",
				"new Description",
				new Validity(LocalDateTime.now(), LocalDateTime.now().plusDays(1)),
				"new researchField",
				projectAdmin.getFenixId());
	}

	private ProjectCreateRequest defaultCreateRequest(String community) {
		return new ProjectCreateRequest(
				community,
				"acronym",
				"gid",
				"Name",
				"description",
				new Validity(LocalDateTime.now(), LocalDateTime.now().plusDays(1)),
				"researchField",
				projectAdmin.getFenixId());
	}

	@SuppressWarnings("unused")
	private static class ProjectUpdateRequest {

		public final String name;
		public final String description;
		public final Validity validity;
		public final String researchField;
		public final String projectLeaderId;

		ProjectUpdateRequest(String name, String description, Validity validity,
		                     String researchField, String projectLeaderId) {
			this.name = name;
			this.description = description;
			this.validity = validity;
			this.researchField = researchField;
			this.projectLeaderId = projectLeaderId;
		}
	}

	@SuppressWarnings("unused")
	private static class ProjectCreateRequest {
		public final String communityId;
		public final String acronym;
		public final String gid;
		public final String name;
		public final String description;
		public final Validity validity;
		public final String researchField;
		public final String projectLeaderId;

		ProjectCreateRequest(String communityId, String acronym, String gid, String name, String description,
		                     Validity validity, String researchField, String projectLeaderId) {
			this.communityId = communityId;
			this.acronym = acronym;
			this.gid = gid;
			this.name = name;
			this.description = description;
			this.validity = validity;
			this.researchField = researchField;
			this.projectLeaderId = projectLeaderId;
		}
	}

}
