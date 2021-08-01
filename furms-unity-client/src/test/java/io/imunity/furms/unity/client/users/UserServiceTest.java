/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.users;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.policy_documents.PolicyAgreement;
import io.imunity.furms.domain.policy_documents.PolicyAgreementStatus;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.UserPolicyAgreements;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.unity.client.UnityClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.edu.icm.unity.types.basic.Attribute;
import pl.edu.icm.unity.types.basic.Entity;
import pl.edu.icm.unity.types.basic.GroupMember;
import pl.edu.icm.unity.types.basic.MultiGroupMembers;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.unity.common.UnityConst.*;
import static io.imunity.furms.unity.common.UnityPaths.GROUP;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

		verify(unityClient, times(1)).post(eq(path), eq(Map.of(IDENTITY_TYPE, PERSISTENT_IDENTITY)));
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
	void shouldAddUserPolicyAgreement() {
		FenixUserId userId = new FenixUserId("userId");
		String group = "/";
		String path = "/entity/" + userId.id + "/attribute";
		String getAttributesPath = "/entity/" + userId.id + "/attributes";

		when(unityClient.get(getAttributesPath, new ParameterizedTypeReference<List<Attribute>>() {}, Map.of(GROUP, ROOT_GROUP, IDENTITY_TYPE, IDENTIFIER_IDENTITY)))
			.thenReturn(emptyList());
		String id = UUID.randomUUID().toString();
		PolicyAgreement policyAgreement = PolicyAgreement.builder()
			.policyDocumentId(new PolicyId(id))
			.acceptanceStatus(PolicyAgreementStatus.ACCEPTED)
			.build();
		userService.addUserPolicyAgreement(userId, policyAgreement);

		Attribute attribute = new Attribute(
			FURMS_POLICY_AGREEMENT_STATE,
			STRING,
			group,
			List.of(PolicyAgreementParser.parse(PolicyAgreementArgument.valueOf(policyAgreement)))
		);
		verify(unityClient, times(1)).put(eq(path), eq(attribute), eq(Map.of(IDENTITY_TYPE, IDENTIFIER_IDENTITY)));
	}

	@Test
	void shouldReturnUserEmptyPolicyAgreementsList() {
		FenixUserId userId = new FenixUserId("userId");
		String getAttributesPath = "/entity/" + userId.id + "/attributes";

		when(unityClient.get(getAttributesPath, new ParameterizedTypeReference<List<Attribute>>() {}, Map.of(GROUP, ROOT_GROUP, IDENTITY_TYPE, IDENTIFIER_IDENTITY)))
			.thenReturn(emptyList());
		userService.getPolicyAgreements(userId);

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

//	@Test
//	void shouldReturnEmptyUsersListFromMultipleGroups() {
//		String path = "/group-members-multi/%2F";
//
//		when(unityClient.post(
//			path,
//			List.of("/fenix/communities/communityId/projects/projectId2/users", "/fenix/communities/communityId/projects/projectId1/users"),
//			Map.of(),
//			new ParameterizedTypeReference<MultiGroupMembers>() {})
//		)
//			.thenReturn(new MultiGroupMembers(List.of(), Map.of()));
//
//		Set<UserPolicyAgreements> allUsersFromGroup = userService.getAllUsersPolicyAcceptanceFromGroups("/", Map.of("communityId", Set.of("projectId1", "projectId2")));
//
//		assertThat(allUsersFromGroup).isEmpty();
//	}

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
