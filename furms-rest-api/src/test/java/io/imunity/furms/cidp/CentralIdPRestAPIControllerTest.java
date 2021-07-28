/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.cidp;

import io.imunity.furms.TestBeansRegistry;
import io.imunity.furms.core.config.security.SecurityProperties;
import io.imunity.furms.core.config.security.WebAppSecurityConfiguration;
import io.imunity.furms.domain.policy_documents.PolicyAgreementExtended;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.users.CommunityMembership;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.ProjectMembership;
import io.imunity.furms.domain.users.SiteSSHKeys;
import io.imunity.furms.domain.users.UserAttribute;
import io.imunity.furms.domain.users.UserRecord;
import io.imunity.furms.domain.users.UserSiteInstallation;
import io.imunity.furms.domain.users.UserSiteInstallationProject;
import io.imunity.furms.domain.users.UserStatus;
import io.imunity.furms.rest.cidp.CentralIdPRestAPIController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.domain.policy_documents.PolicyAgreementStatus.ACCEPTED;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = {CentralIdPRestAPIController.class}, 
	excludeFilters = {@Filter(type = FilterType.ASSIGNABLE_TYPE, value = WebAppSecurityConfiguration.class)},
	includeFilters = {@Filter(type = FilterType.ASSIGNABLE_TYPE, value = SecurityProperties.class)})
public class CentralIdPRestAPIControllerTest extends TestBeansRegistry {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void shouldGetUserRecord() throws Exception {
		final UUID policy1 = UUID.randomUUID();
		final UUID policy2 = UUID.randomUUID();
		when(userService.getUserRecord(new FenixUserId("F_ID"))).thenReturn(new UserRecord(
				UserStatus.ENABLED,
				Set.of(new UserAttribute("attr1", "attr1val")),
				Set.of(new CommunityMembership(
						"commId",
						"commName",
						Set.of(new ProjectMembership(
								"projId",
								"projName",
								Set.of(new UserAttribute("attr3", "attr3val")))),
						Set.of(new UserAttribute("attr4", "attr4val")))),
				Set.of(new SiteSSHKeys("siteId", "siteName", Set.of("sshKey1"))),
				Set.of(new UserSiteInstallation(
						"siteId",
						"siteOauthClientId",
						Set.of(new UserSiteInstallationProject("localUserId", "projId")),
						new PolicyAgreementExtended(new PolicyId(policy1), "siteId", 1,
								ACCEPTED, Instant.now()),
						Set.of(new PolicyAgreementExtended(new PolicyId(policy2), "siteId", 2,
								ACCEPTED, Instant.now()))))));

		this.mockMvc.perform(get("/rest-api/v1/cidp/user/F_ID")
				.with(httpBasic("cidp", "cidppass"))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userStatus").value("ENABLED"))
				.andExpect(jsonPath("$.attributes[0].name").value("attr1"))
				.andExpect(jsonPath("$.attributes[0].values[0]").value("attr1val"))
				.andExpect(jsonPath("$.communities[0].id").value("commId"))
				.andExpect(jsonPath("$.communities[0].name").value("commName"))
				.andExpect(jsonPath("$.communities[0].groups").isEmpty())
				.andExpect(jsonPath("$.communities[0].projects[0].id").value("projId"))
				.andExpect(jsonPath("$.communities[0].projects[0].name").value("projName"))
				.andExpect(jsonPath("$.communities[0].projects[0].attributes[0].name").value("attr3"))
				.andExpect(jsonPath("$.communities[0].projects[0].attributes[0].values[0]").value("attr3val"))
				.andExpect(jsonPath("$.communities[0].attributes[0].name").value("attr4"))
				.andExpect(jsonPath("$.communities[0].attributes[0].values[0]").value("attr4val"))
				.andExpect(jsonPath("$.sshKeys[0].siteId").value("siteId"))
				.andExpect(jsonPath("$.sshKeys[0].siteName").value("siteName"))
				.andExpect(jsonPath("$.sshKeys[0].sshKeys[0]").value("sshKey1"))
				.andExpect(jsonPath("$.siteInstallations[0].siteId").value("siteId"))
				.andExpect(jsonPath("$.siteInstallations[0].siteOauthClientId").value("siteOauthClientId"))
				.andExpect(jsonPath("$.siteInstallations[0].projectSitesMemberships[0].localUserId").value("localUserId"))
				.andExpect(jsonPath("$.siteInstallations[0].projectSitesMemberships[0].projectId").value("projId"))
				.andExpect(jsonPath("$.siteInstallations[0].sitePolicyAcceptance.policyId").value(policy1.toString()))
				.andExpect(jsonPath("$.siteInstallations[0].sitePolicyAcceptance.accepted").value("ACCEPTED"))
				.andExpect(jsonPath("$.siteInstallations[0].sitePolicyAcceptance.processedOn").isNotEmpty())
				.andExpect(jsonPath("$.siteInstallations[0].sitePolicyAcceptance.revision").value(1))
				.andExpect(jsonPath("$.siteInstallations[0].servicesPolicyAcceptance[0].policyId").value(policy2.toString()))
				.andExpect(jsonPath("$.siteInstallations[0].servicesPolicyAcceptance[0].accepted").value("ACCEPTED"))
				.andExpect(jsonPath("$.siteInstallations[0].servicesPolicyAcceptance[0].processedOn").isNotEmpty())
				.andExpect(jsonPath("$.siteInstallations[0].servicesPolicyAcceptance[0].revision").value(2));
	}

	@Test
	void shouldSetStatus() throws Exception {
		this.mockMvc.perform(post("/rest-api/v1/cidp/user/F_ID/status")
				.with(httpBasic("cidp", "cidppass"))
				.contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"ENABLED\"}"))
			.andExpect(status().isOk());
		
		verify(userService).setUserStatus(new FenixUserId("F_ID"), UserStatus.ENABLED);
	}
}
