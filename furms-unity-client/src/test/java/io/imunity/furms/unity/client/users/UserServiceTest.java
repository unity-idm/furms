/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.users;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.users.User;
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

import java.util.List;
import java.util.Map;

import static io.imunity.furms.unity.common.UnityConst.*;
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
		String userId = "userId";
		String group = "fenix/group";
		String path = "/group/fenix%2Fgroup/entity/" + userId;

		userService.addUserToGroup(userId, group);

		verify(unityClient, times(1)).post(eq(path), eq(Map.of(IDENTITY_TYPE, PERSISTENT_IDENTITY)));
	}

	@Test
	void shouldAddUserARole() {
		String userId = "userId";
		String group = "fenix/group";
		String path = "/entity/" + userId + "/attribute";
		Role role = Role.COMMUNITY_ADMIN;

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
	void shouldReturnEmptyListOfUsers() {
		String group = "fenix/group";
		String path = "/group-members/fenix%2Fgroup";

		when(unityClient.get(path, new ParameterizedTypeReference<List<GroupMember>>() {}))
			.thenReturn(List.of(new GroupMember("1", mock(Entity.class), List.of())));

		List<User> allUsersFromGroup = userService.getAllUsersFromGroup(group, attr -> true);

		assertThat(allUsersFromGroup).isEmpty();

	}

	@Test
	void shouldRemoveUserRole() {
		String userId = "userId";
		String group = "fenix/group";
		String getAttributesPath = "/entity/" + userId + "/groups/attributes";
		String putAttributePath = "/entity/" + userId + "/attribute";
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
		String userId = "userId";
		String group = "fenix/group";
		String path = "/group/fenix%2Fgroup/entity/" + userId;

		userService.removeUserFromGroup(userId, group);

		verify(unityClient, times(1)).delete(eq(path), eq(Map.of(IDENTITY_TYPE, PERSISTENT_IDENTITY)));
	}
}
