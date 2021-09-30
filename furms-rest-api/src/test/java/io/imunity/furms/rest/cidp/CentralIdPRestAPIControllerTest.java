/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.rest.cidp;

import io.imunity.furms.TestBeansRegistry;
import io.imunity.furms.core.config.security.SecurityProperties;
import io.imunity.furms.core.config.security.WebAppSecurityConfiguration;
import io.imunity.furms.domain.generic_groups.GroupAccess;
import io.imunity.furms.domain.policy_documents.PolicyAcceptanceAtSite;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.projects.ProjectMembershipOnSite;
import io.imunity.furms.domain.sites.SiteUser;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.SiteSSHKeys;
import io.imunity.furms.domain.users.UserRecord;
import io.imunity.furms.domain.users.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.domain.policy_documents.PolicyAcceptanceStatus.ACCEPTED;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
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
@Import(CentralIdpRestAPIConfiguration.class)
public class CentralIdPRestAPIControllerTest extends TestBeansRegistry {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void shouldGetUserRecord() throws Exception {
		final UUID policy1 = UUID.randomUUID();
		final UUID policy2 = UUID.randomUUID();
		final FenixUserId fenixUserId = new FenixUserId("F_ID");
		final PersistentId persistentId = new PersistentId("ID");
		when(userService.getUserRecord(fenixUserId)).thenReturn(new UserRecord(
				FURMSUser.builder()
						.id(persistentId)
						.fenixUserId(fenixUserId)
						.firstName("firstName")
						.lastName("lastName")
						.email("email@domain.com")
						.status(UserStatus.ENABLED)
						.build(),
				Set.of(new SiteUser(
						"siteId",
						"siteOauthClientId",
						Set.of(new ProjectMembershipOnSite("localUserId", "projId")),
						new PolicyAcceptanceAtSite(new PolicyId(policy1), "siteId", 1,
								ACCEPTED, Instant.now()),
						Set.of(new PolicyAcceptanceAtSite(new PolicyId(policy2), "siteId", 2,
								ACCEPTED, Instant.now())),
						Set.of(new SiteSSHKeys("siteId", Set.of("sshKey1"))))),
				Set.of(
					new GroupAccess("communityId", Set.of("group1", "group2")),
					new GroupAccess("communityId2", Set.of("group1", "group2"))
				)
			));

		this.mockMvc.perform(get("/rest-api/v1/cidp/user/F_ID")
				.with(httpBasic("cidp", "cidppass"))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.user.fenixIdentifier").value(fenixUserId.id))
				.andExpect(jsonPath("$.user.title").isEmpty())
				.andExpect(jsonPath("$.user.firstname").value("firstName"))
				.andExpect(jsonPath("$.user.lastname").value("lastName"))
				.andExpect(jsonPath("$.user.email").value("email@domain.com"))
				.andExpect(jsonPath("$.user.affiliation.name").value("firstName"))
				.andExpect(jsonPath("$.user.affiliation.email").value("email@domain.com"))
				.andExpect(jsonPath("$.user.affiliation.country").isEmpty())
				.andExpect(jsonPath("$.user.affiliation.postalAddress").isEmpty())
				.andExpect(jsonPath("$.user.nationality").isEmpty())
				.andExpect(jsonPath("$.user.phone").isEmpty())
				.andExpect(jsonPath("$.user.dateOfBirth").isEmpty())
				.andExpect(jsonPath("$.user.placeOfBirth").isEmpty())
				.andExpect(jsonPath("$.user.postalAddress").isEmpty())
				.andExpect(jsonPath("$.groupAccess[0].communityId").value(in(Set.of("communityId", "communityId2"))))
				.andExpect(jsonPath("$.groupAccess[0].groups[0]").value(in(Set.of("group1", "group2"))))
				.andExpect(jsonPath("$.groupAccess[0].groups[1]").value(in(Set.of("group1", "group2"))))
				.andExpect(jsonPath("$.groupAccess[1].communityId").value(in(Set.of("communityId", "communityId2"))))
				.andExpect(jsonPath("$.groupAccess[1].groups[0]").value(in(Set.of("group1", "group2"))))
				.andExpect(jsonPath("$.groupAccess[1].groups[1]").value(in(Set.of("group1", "group2"))))
				.andExpect(jsonPath("$.siteAccess[0].siteId").value("siteId"))
				.andExpect(jsonPath("$.siteAccess[0].siteOauthClientId").value("siteOauthClientId"))
				.andExpect(jsonPath("$.siteAccess[0].projectMemberships[0].localUserId").value("localUserId"))
				.andExpect(jsonPath("$.siteAccess[0].projectMemberships[0].projectId").value("projId"))
				.andExpect(jsonPath("$.siteAccess[0].sitePolicyAcceptance.policyId").value(policy1.toString()))
				.andExpect(jsonPath("$.siteAccess[0].sitePolicyAcceptance.acceptanceStatus").value("ACCEPTED"))
				.andExpect(jsonPath("$.siteAccess[0].sitePolicyAcceptance.processedOn").isNotEmpty())
				.andExpect(jsonPath("$.siteAccess[0].sitePolicyAcceptance.revision").value(1))
				.andExpect(jsonPath("$.siteAccess[0].servicesPolicyAcceptance[0].policyId").value(policy2.toString()))
				.andExpect(jsonPath("$.siteAccess[0].servicesPolicyAcceptance[0].acceptanceStatus").value("ACCEPTED"))
				.andExpect(jsonPath("$.siteAccess[0].servicesPolicyAcceptance[0].processedOn").isNotEmpty())
				.andExpect(jsonPath("$.siteAccess[0].servicesPolicyAcceptance[0].revision").value(2))
				.andExpect(jsonPath("$.siteAccess[0].sshKeys[0].sshKeys[0]").value("sshKey1"));
	}

	@Test
	void shouldGetUserRecordOnlyForSpecificSite() throws Exception {
		final UUID policy1 = UUID.randomUUID();
		final UUID policy2 = UUID.randomUUID();
		final FenixUserId fenixUserId = new FenixUserId("F_ID");
		when(userService.getUserRecord(fenixUserId)).thenReturn(new UserRecord(
				FURMSUser.builder()
						.id(new PersistentId("ID"))
						.fenixUserId(fenixUserId)
						.firstName("firstName")
						.lastName("lastName")
						.email("email@domain.com")
						.status(UserStatus.ENABLED)
						.build(),
				Set.of(new SiteUser(
						"siteId",
						"siteOauthClientId",
						Set.of(new ProjectMembershipOnSite("localUserId", "projId")),
						new PolicyAcceptanceAtSite(new PolicyId(policy1), "siteId", 1,
								ACCEPTED, Instant.now()),
						Set.of(new PolicyAcceptanceAtSite(new PolicyId(policy2), "siteId", 2,
								ACCEPTED, Instant.now())),
						Set.of(new SiteSSHKeys("siteId", Set.of("sshKey1")))),
					new SiteUser(
						"siteId",
						"otherSiteOauthClientId",
						Set.of(new ProjectMembershipOnSite("localUserId2", "projId")),
						new PolicyAcceptanceAtSite(new PolicyId(policy1), "siteId", 1,
								ACCEPTED, Instant.now()),
						Set.of(new PolicyAcceptanceAtSite(new PolicyId(policy2), "siteId", 2,
								ACCEPTED, Instant.now())),
						Set.of(new SiteSSHKeys("siteId", Set.of("sshKey1"))))),
				Set.of(
					new GroupAccess("communityId", Set.of("group1", "group2")),
					new GroupAccess("communityId2", Set.of("group1", "group2"))
				)
		));

		this.mockMvc.perform(get("/rest-api/v1/cidp/user/F_ID/site/siteOauthClientId")
				.with(httpBasic("cidp", "cidppass"))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.user.fenixIdentifier").value(fenixUserId.id))
				.andExpect(jsonPath("$.user.title").isEmpty())
				.andExpect(jsonPath("$.user.firstname").value("firstName"))
				.andExpect(jsonPath("$.user.lastname").value("lastName"))
				.andExpect(jsonPath("$.user.email").value("email@domain.com"))
				.andExpect(jsonPath("$.user.affiliation.name").value("firstName"))
				.andExpect(jsonPath("$.user.affiliation.email").value("email@domain.com"))
				.andExpect(jsonPath("$.user.affiliation.country").isEmpty())
				.andExpect(jsonPath("$.user.affiliation.postalAddress").isEmpty())
				.andExpect(jsonPath("$.user.nationality").isEmpty())
				.andExpect(jsonPath("$.user.phone").isEmpty())
				.andExpect(jsonPath("$.user.dateOfBirth").isEmpty())
				.andExpect(jsonPath("$.user.placeOfBirth").isEmpty())
				.andExpect(jsonPath("$.user.postalAddress").isEmpty())
				.andExpect(jsonPath("$.userStatus").value("ENABLED"))
				.andExpect(jsonPath("$.groupAccess[0].communityId").value(in(Set.of("communityId", "communityId2"))))
				.andExpect(jsonPath("$.groupAccess[0].groups[0]").value(in(Set.of("group1", "group2"))))
				.andExpect(jsonPath("$.groupAccess[0].groups[1]").value(in(Set.of("group1", "group2"))))
				.andExpect(jsonPath("$.groupAccess[1].communityId").value(in(Set.of("communityId", "communityId2"))))
				.andExpect(jsonPath("$.groupAccess[1].groups[0]").value(in(Set.of("group1", "group2"))))
				.andExpect(jsonPath("$.groupAccess[1].groups[1]").value(in(Set.of("group1", "group2"))))
				.andExpect(jsonPath("$.siteAccess", hasSize(1)))
				.andExpect(jsonPath("$.siteAccess[0].siteId").value("siteId"))
				.andExpect(jsonPath("$.siteAccess[0].siteOauthClientId").value("siteOauthClientId"))
				.andExpect(jsonPath("$.siteAccess[0].projectMemberships[0].localUserId").value("localUserId"))
				.andExpect(jsonPath("$.siteAccess[0].projectMemberships[0].projectId").value("projId"))
				.andExpect(jsonPath("$.siteAccess[0].sitePolicyAcceptance.policyId").value(policy1.toString()))
				.andExpect(jsonPath("$.siteAccess[0].sitePolicyAcceptance.acceptanceStatus").value("ACCEPTED"))
				.andExpect(jsonPath("$.siteAccess[0].sitePolicyAcceptance.processedOn").isNotEmpty())
				.andExpect(jsonPath("$.siteAccess[0].sitePolicyAcceptance.revision").value(1))
				.andExpect(jsonPath("$.siteAccess[0].servicesPolicyAcceptance[0].policyId").value(policy2.toString()))
				.andExpect(jsonPath("$.siteAccess[0].servicesPolicyAcceptance[0].acceptanceStatus").value("ACCEPTED"))
				.andExpect(jsonPath("$.siteAccess[0].servicesPolicyAcceptance[0].processedOn").isNotEmpty())
				.andExpect(jsonPath("$.siteAccess[0].servicesPolicyAcceptance[0].revision").value(2))
				.andExpect(jsonPath("$.siteAccess[0].sshKeys[0].sshKeys[0]").value("sshKey1"));
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
