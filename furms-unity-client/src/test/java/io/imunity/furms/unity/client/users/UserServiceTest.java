/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.users;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyAcceptanceStatus;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.GroupedUsers;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.unity.client.UnityClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.edu.icm.unity.types.basic.Attribute;
import pl.edu.icm.unity.types.basic.AttributeExt;
import pl.edu.icm.unity.types.basic.Entity;
import pl.edu.icm.unity.types.basic.EntityInformation;
import pl.edu.icm.unity.types.basic.GroupMember;
import pl.edu.icm.unity.types.basic.Identity;
import pl.edu.icm.unity.types.basic.MultiGroupMembers;

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
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
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

		when(unityClient.get(getAttributesPath, new ParameterizedTypeReference<Map<String, List<Attribute>>>() {}, Map.of(GROUPS_PATTERNS, ALL_GROUPS_PATTERNS)))
			.thenReturn(Map.of(group, emptyList()));
		userService.addUserRole(userId, group, role);

		Attribute attribute = new Attribute(
			role.unityRoleAttribute,
			ENUMERATION,
			group,
			List.of(role.unityRoleValue)
		);
		verify(unityClient, times(1)).put(eq(path), eq(attribute));
	}

	@Test
	void shouldAddUserPolicyAcceptance() {
		FenixUserId userId = new FenixUserId("userId");
		String group = "/";
		String path = "/entity/" + userId.id + "/attribute";
		String getAttributesPath = "/entity/" + userId.id + "/attributes";

		when(unityClient.get(getAttributesPath, new ParameterizedTypeReference<List<Attribute>>() {}, Map.of(GROUP, ROOT_GROUP, IDENTITY_TYPE, IDENTIFIER_IDENTITY)))
			.thenReturn(emptyList());
		String id = UUID.randomUUID().toString();
		PolicyAcceptance policyAcceptance = PolicyAcceptance.builder()
			.policyDocumentId(new PolicyId(id))
			.acceptanceStatus(PolicyAcceptanceStatus.ACCEPTED)
			.build();
		userService.addUserPolicyAcceptance(userId, policyAcceptance);

		Attribute attribute = new Attribute(
				FURMS_POLICY_ACCEPTANCE_STATE_ATTRIBUTE,
			STRING,
			group,
			List.of(PolicyAcceptanceParser.parse(PolicyAcceptanceArgument.valueOf(policyAcceptance)))
		);
		verify(unityClient, times(1)).put(eq(path), eq(attribute), eq(Map.of(IDENTITY_TYPE, IDENTIFIER_IDENTITY)));
	}

	@Test
	void shouldReturnUserEmptyPolicyAcceptancesList() {
		FenixUserId userId = new FenixUserId("userId");
		String getAttributesPath = "/entity/" + userId.id + "/attributes";

		when(unityClient.get(getAttributesPath, new ParameterizedTypeReference<List<Attribute>>() {}, Map.of(GROUP, ROOT_GROUP, IDENTITY_TYPE, IDENTIFIER_IDENTITY)))
			.thenReturn(emptyList());
		userService.getPolicyAcceptances(userId);

		verify(unityClient, times(1)).get(getAttributesPath, new ParameterizedTypeReference<List<Attribute>>() {}, Map.of(GROUP, ROOT_GROUP, IDENTITY_TYPE, IDENTIFIER_IDENTITY));
	}

	@Test
	void shouldReturnEmptyListOfUsers() {
		String group = "fenix/group";
		String path = "/group-members/fenix%2Fgroup";

		when(unityClient.get(path, new ParameterizedTypeReference<List<GroupMember>>() {}))
			.thenReturn(List.of(new GroupMember("1", mock(Entity.class), List.of())));

		List<FURMSUser> allUsersFromGroup = userService.getAllUsersFromGroup(group, attr -> true);

		assertThat(allUsersFromGroup).isEmpty();
	}


	@Test
	void shouldReturnUsersInFenixAndCommunityGroups() {
		String path = "/group-members-multi/%2Ffenix";
		UUID communityId = UUID.randomUUID();
		String communityPath = "/fenix/communities/" + communityId + "/users";

		Map<String, Set<Role>> groupWithRoles = Map.of(
			FENIX_PATTERN,
			Set.of(),
			communityPath,
			Set.of(COMMUNITY_ADMIN)
		);

		MultiGroupMembers multiGroupMembers = new MultiGroupMembers(
			List.of(
				new Entity(List.of(new Identity(PERSISTENT_IDENTITY, "1", 1, "1")), new EntityInformation(1), null),
				new Entity(List.of(new Identity(PERSISTENT_IDENTITY, "2", 2, "2")), new EntityInformation(2), null),
				new Entity(List.of(new Identity(PERSISTENT_IDENTITY, "3", 3, "3")), new EntityInformation(3), null)
			),
			Map.of(
				FENIX_PATTERN,
				List.of(
					new MultiGroupMembers.EntityGroupAttributes(1, List.of(
						new AttributeExt(new Attribute("name", null, FENIX_PATTERN, List.of("name")), true, null, null),
						new AttributeExt(new Attribute("email", "", FENIX_PATTERN, List.of("email")), true, null, null)
					))
				),
				communityPath,
				List.of(
					new MultiGroupMembers.EntityGroupAttributes(2, List.of(
						new AttributeExt(
							new Attribute(COMMUNITY_ADMIN.unityRoleAttribute, "", communityPath,
								List.of(COMMUNITY_ADMIN.unityRoleValue)),true, null, null),
						new AttributeExt(new Attribute("email", "", FENIX_PATTERN, List.of("email2")), true, null, null)

					)),
					new MultiGroupMembers.EntityGroupAttributes(3, List.of(
						new AttributeExt(new Attribute(PROJECT_ADMIN.unityRoleAttribute, "", communityPath,
							List.of(PROJECT_ADMIN.unityRoleValue)), true, null, null),
						new AttributeExt(new Attribute("email", "", FENIX_PATTERN, List.of("email3")), true, null, null)
					))
				)
			)
			);
		when(unityClient.post(path, groupWithRoles.keySet(), Map.of(), new ParameterizedTypeReference<MultiGroupMembers>() {}))
			.thenReturn(multiGroupMembers);
		GroupedUsers groupedUsers = userService.getUsersFromGroupsFilteredByRoles(groupWithRoles);

		assertThat(groupedUsers.getUsers(FENIX_PATTERN)).isEqualTo(List.of(
			FURMSUser.builder()
				.id(new PersistentId("1"))
				.email("email")
				.roles(Map.of(new ResourceId((String) null, APP_LEVEL), Set.of()))
				.build()
		));
		assertThat(groupedUsers.getUsers(communityPath)).isEqualTo(List.of(
			FURMSUser.builder()
				.id(new PersistentId("2"))
				.email("email2")
				.roles(Map.of(new ResourceId(communityId, COMMUNITY), Set.of(COMMUNITY_ADMIN)))
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
		Attribute attribute = new Attribute(role.unityRoleAttribute, "", group, List.of(role.unityRoleValue, role2.unityRoleValue));

		when(unityClient.get(getAttributesPath, new ParameterizedTypeReference<Map<String, List<Attribute>>>() {}, Map.of(GROUPS_PATTERNS, ALL_GROUPS_PATTERNS)))
			.thenReturn(Map.of(group, List.of(attribute)));

		userService.removeUserRole(userId, group, role);

		Attribute updatedAttribute = new Attribute(
			role.unityRoleAttribute,
			ENUMERATION,
			group,
			List.of(role2.unityRoleValue)
		);

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
}
