/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.users;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyAcceptanceStatus;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.GroupedUsers;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserStatus;
import io.imunity.furms.unity.client.UnityClient;
import io.imunity.rest.api.RestGroupMemberWithAttributes;
import io.imunity.rest.api.RestMultiGroupMembersWithAttributes;
import io.imunity.rest.api.types.basic.RestAttribute;
import io.imunity.rest.api.types.basic.RestAttributeExt;
import io.imunity.rest.api.types.basic.RestEntityInformation;
import io.imunity.rest.api.types.basic.RestIdentity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;
import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;
import static io.imunity.furms.domain.authz.roles.Role.COMMUNITY_ADMIN;
import static io.imunity.furms.domain.authz.roles.Role.PROJECT_ADMIN;
import static io.imunity.furms.unity.common.UnityConst.ALL_GROUPS_PATTERNS;
import static io.imunity.furms.unity.common.UnityConst.ENUMERATION;
import static io.imunity.furms.unity.common.UnityConst.FENIX_PATTERN;
import static io.imunity.furms.unity.common.UnityConst.FURMS_POLICY_ACCEPTANCE_STATE_ATTRIBUTE;
import static io.imunity.furms.unity.common.UnityConst.GROUPS_PATTERNS;
import static io.imunity.furms.unity.common.UnityConst.IDENTIFIER_IDENTITY;
import static io.imunity.furms.unity.common.UnityConst.IDENTITY_TYPE;
import static io.imunity.furms.unity.common.UnityConst.PERSISTENT_IDENTITY;
import static io.imunity.furms.unity.common.UnityConst.ROOT_GROUP;
import static io.imunity.furms.unity.common.UnityConst.STRING;
import static io.imunity.furms.unity.common.UnityPaths.GROUP;
import static io.imunity.furms.unity.common.UnityPaths.GROUP_MEMBERS_MULTI;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class UserServiceTest {

	@Mock
	private UnityClient unityClient;

	@InjectMocks
	private UserService userService;

	@Test
	void shouldAddUserToGroup() {
		PersistentId userId = new PersistentId("userId");
		String group = "fenix/group";
		String path = "/group/fenix%2Fgroup/entity/" + userId.id;

		userService.addUserToGroup(userId, group);

		verify(unityClient, times(1)).post(eq(path));
	}

	@Test
	void shouldSendUserNotification() {
		PersistentId userId = new PersistentId("userId");

		userService.sendUserNotification(userId, "templateId", Map.of("custom.name", "name"));

		verify(unityClient).post("/userNotification-trigger/entity/userId/template/templateId", null, Map.of("custom.name", "name"));
	}

	@Test
	void shouldAddUserARole() {
		PersistentId userId = new PersistentId("userId");
		String group = "fenix/group";
		String path = "/entity/" + userId.id + "/attribute";
		String getAttributesPath = "/entity/" + userId.id + "/groups/attributes";
		Role role = Role.COMMUNITY_ADMIN;

		when(unityClient.get(getAttributesPath, new ParameterizedTypeReference<Map<String, List<RestAttribute>>>() {},
			Map.of(GROUPS_PATTERNS, ALL_GROUPS_PATTERNS)))
			.thenReturn(Map.of(group, emptyList()));
		userService.addUserRole(userId, group, role);

		RestAttribute attribute = RestAttribute.builder()
			.withName(role.unityRoleAttribute)
			.withValueSyntax(ENUMERATION)
			.withGroupPath(group)
			.withValues(List.of(role.unityRoleValue))
			.build();
		verify(unityClient, times(1)).put(eq(path), eq(attribute));
	}

	@Test
	void shouldAddUserPolicyAcceptance() {
		FenixUserId userId = new FenixUserId("userId");
		String group = "/";
		String path = "/entity/" + userId.id + "/attribute";
		String getAttributesPath = "/entity/" + userId.id + "/attributes";

		when(unityClient.get(getAttributesPath, new ParameterizedTypeReference<List<RestAttribute>>() {}, Map.of(GROUP,
			ROOT_GROUP, IDENTITY_TYPE, IDENTIFIER_IDENTITY)))
			.thenReturn(emptyList());
		String id = UUID.randomUUID().toString();
		PolicyAcceptance policyAcceptance = PolicyAcceptance.builder()
			.policyDocumentId(new PolicyId(id))
			.acceptanceStatus(PolicyAcceptanceStatus.ACCEPTED)
			.build();
		userService.addUserPolicyAcceptance(userId, policyAcceptance);

		RestAttribute attribute = RestAttribute.builder()
			.withName(FURMS_POLICY_ACCEPTANCE_STATE_ATTRIBUTE)
			.withValueSyntax(STRING)
			.withGroupPath(group)
			.withValues(List.of(PolicyAcceptanceParser.parse(PolicyAcceptanceArgument.valueOf(policyAcceptance))))
			.build();
		verify(unityClient, times(1)).put(eq(path), eq(attribute), eq(Map.of(IDENTITY_TYPE, IDENTIFIER_IDENTITY)));
	}

	@Test
	void shouldReturnUserEmptyPolicyAcceptancesList() {
		FenixUserId userId = new FenixUserId("userId");
		String getAttributesPath = "/entity/" + userId.id + "/attributes";

		when(unityClient.get(getAttributesPath, new ParameterizedTypeReference<List<RestAttribute>>() {}, Map.of(GROUP,
			ROOT_GROUP, IDENTITY_TYPE, IDENTIFIER_IDENTITY)))
			.thenReturn(emptyList());
		userService.getPolicyAcceptances(userId);

		verify(unityClient, times(1)).get(getAttributesPath, new ParameterizedTypeReference<List<RestAttribute>>() {},
			Map.of(GROUP, ROOT_GROUP, IDENTITY_TYPE, IDENTIFIER_IDENTITY));
	}

	@Test
	void shouldReturnEmptyListOfUsers() {
		String group = "fenix/group";
		String path = "/group-members-attributes/fenix%2Fgroup";

		when(unityClient.get(path, new ParameterizedTypeReference<List<RestGroupMemberWithAttributes>>() {}))
			.thenReturn(List.of(RestGroupMemberWithAttributes.builder()
					.withEntityInformation(mock(RestEntityInformation.class))
					.withIdentities(List.of())
					.withAttributes(List.of())
					.build()
			));

		List<FURMSUser> allUsersFromGroup = userService.getAllUsersFromGroup(group, attr -> true);

		assertThat(allUsersFromGroup).isEmpty();
	}


	@Test
	void shouldReturnUsersInFenixAndCommunityGroups() {
		String path = "/multi-group-members-attributes/";
		UUID communityId = UUID.randomUUID();
		String communityPath = "/fenix/communities/" + communityId + "/users";

		Map<String, Set<Role>> groupWithRoles = Map.of(
			FENIX_PATTERN,
			Set.of(),
			communityPath,
			Set.of(COMMUNITY_ADMIN)
		);

		RestMultiGroupMembersWithAttributes multiGroupMembers = RestMultiGroupMembersWithAttributes.builder().withMembers(
			Map.of(
				FENIX_PATTERN,
				List.of(
					RestGroupMemberWithAttributes.builder()
						.withEntityInformation(
							RestEntityInformation.builder()
								.withEntityId(1L)
								.withState("valid")
								.build()
						)
						.withIdentities(
							List.of(RestIdentity.builder()
								.withEntityId(1L)
								.withComparableValue("1")
								.withTypeId(PERSISTENT_IDENTITY)
								.build())
						)
						.withAttributes(
							List.of(
								RestAttributeExt.builder()
									.withName("name")
									.withDirect(true)
									.withGroupPath(FENIX_PATTERN)
									.withValues(List.of("name"))
									.withValueSyntax("")
									.build(),
								RestAttributeExt.builder()
									.withName("email")
									.withDirect(true)
									.withGroupPath(FENIX_PATTERN)
									.withValues(List.of("email"))
									.withValueSyntax("")
									.build()
							)
						).build()
				),
				communityPath,
				List.of(
					RestGroupMemberWithAttributes.builder()
						.withEntityInformation(
							RestEntityInformation.builder()
								.withEntityId(2L)
								.withState("valid")
								.build()
						)
						.withIdentities(
							List.of(RestIdentity.builder()
								.withEntityId(2L)
								.withComparableValue("2")
								.withTypeId(PERSISTENT_IDENTITY)
								.build())
						)
						.withAttributes(
							List.of(
								RestAttributeExt.builder()
									.withName(COMMUNITY_ADMIN.unityRoleAttribute)
									.withDirect(true)
									.withGroupPath(communityPath)
									.withValues(List.of(COMMUNITY_ADMIN.unityRoleValue))
									.withValueSyntax("")
									.build(),
								RestAttributeExt.builder()
									.withName("email")
									.withDirect(true)
									.withGroupPath(FENIX_PATTERN)
									.withValues(List.of("email2"))
									.withValueSyntax("")
									.build()
							)
						).build(),
					RestGroupMemberWithAttributes.builder()
						.withEntityInformation(
							RestEntityInformation.builder()
								.withEntityId(3L)
								.withState("valid")
								.build()
						)
						.withIdentities(
							List.of(RestIdentity.builder()
								.withEntityId(2L)
								.withComparableValue("3")
								.withTypeId(PERSISTENT_IDENTITY)
								.build())
						)
						.withAttributes(
							List.of(
								RestAttributeExt.builder()
									.withName(PROJECT_ADMIN.unityRoleAttribute)
									.withDirect(true)
									.withGroupPath(communityPath)
									.withValues(List.of(PROJECT_ADMIN.unityRoleValue))
									.withValueSyntax("")
									.build(),
								RestAttributeExt.builder()
									.withName("email")
									.withDirect(true)
									.withGroupPath(FENIX_PATTERN)
									.withValues(List.of("email3"))
									.withValueSyntax("")
									.build()
							)
						).build()
				)
			)
		).build();
		when(unityClient.get(eq(path), anyMap(),
				eq(new ParameterizedTypeReference<RestMultiGroupMembersWithAttributes>() {})))
			.thenReturn(multiGroupMembers);
		GroupedUsers groupedUsers = userService.getUsersFromGroupsFilteredByRoles(groupWithRoles);

		assertThat(groupedUsers.getUsers(FENIX_PATTERN)).isEqualTo(List.of(
			FURMSUser.builder()
				.id(new PersistentId("1"))
				.email("email")
				.status(UserStatus.ENABLED)
				.roles(Map.of(new ResourceId(null, APP_LEVEL), Set.of()))
				.build()
		));
		assertThat(groupedUsers.getUsers(communityPath)).isEqualTo(List.of(
			FURMSUser.builder()
				.id(new PersistentId("2"))
				.email("email2")
				.status(UserStatus.ENABLED)
				.roles(Map.of(new ResourceId(new CommunityId(communityId), COMMUNITY), Set.of(COMMUNITY_ADMIN)))
				.build()
		));
	}

	@Test
	void shouldRemoveUserRole() {
		PersistentId userId = new PersistentId("userId");
		String group = "fenix/group";
		String getAttributesPath = "/entity/" + userId.id + "/groups/attributes";
		String putAttributePath = "/entity/" + userId.id + "/attribute";
		Role role = Role.PROJECT_ADMIN;
		Role role2 = Role.PROJECT_USER;

		RestAttribute attribute = RestAttribute.builder()
			.withName(role.unityRoleAttribute)
			.withValueSyntax("")
			.withGroupPath(group)
			.withValues(List.of(role.unityRoleValue, role2.unityRoleValue))
			.build();
		when(unityClient.get(getAttributesPath, new ParameterizedTypeReference<Map<String, List<RestAttribute>>>() {},
			Map.of(GROUPS_PATTERNS, ALL_GROUPS_PATTERNS)))
			.thenReturn(Map.of(group, List.of(attribute)));

		userService.removeUserRole(userId, group, role);

		RestAttribute updatedAttribute = RestAttribute.builder()
			.withName(role.unityRoleAttribute)
			.withValueSyntax(ENUMERATION)
			.withGroupPath(group)
			.withValues(List.of(role2.unityRoleValue))
			.build();

		verify(unityClient, times(1)).put(eq(putAttributePath), eq(updatedAttribute));
	}

	@Test
	void shouldRemoveUserFromGroup() {
		PersistentId userId = new PersistentId("userId");
		String group = "fenix/group";
		String path = "/group/fenix%2Fgroup/entity/" + userId.id;

		userService.removeUserFromGroup(userId, group);

		verify(unityClient, times(1)).delete(eq(path), eq(Map.of(IDENTITY_TYPE, PERSISTENT_IDENTITY)));
	}
	
	@Test
	void shouldQueryTwiceForGroupMembers() {
		// given
		String path = UriComponentsBuilder.newInstance()
				.path(GROUP_MEMBERS_MULTI)
				.encode()
				.toUriString();
		when(unityClient.get(eq(path), anyMap(), any()))
			.thenReturn(RestMultiGroupMembersWithAttributes.builder().withMembers(Map.of()).build());
		
		// when
		userService.getAllUsersPolicyAcceptanceFromGroups(Map.of("communityId", Set.of("1".repeat(5024), "2".repeat(5024))));

		// then
		verify(unityClient, times(2)).get(eq(path), anyMap(), any());
	}
}
