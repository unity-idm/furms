/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.projects;

import io.imunity.furms.domain.projects.ProjectGroup;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.unity.client.UnityClient;
import io.imunity.furms.unity.client.users.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.edu.icm.unity.types.I18nString;
import pl.edu.icm.unity.types.basic.Group;

import java.util.*;

import static io.imunity.furms.domain.authz.roles.Role.PROJECT_ADMIN;
import static io.imunity.furms.domain.authz.roles.Role.PROJECT_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class UnityProjectGroupsDAOTest {

	@Mock
	private UnityClient unityClient;

	@Mock
	private UserService userService;

	@InjectMocks
	private UnityProjectGroupsDAO unityProjectGroupsDAO;

	@Test
	void shouldGetMetaInfoAboutProject() {
		//given
		String communityId = UUID.randomUUID().toString();
		String projectId = UUID.randomUUID().toString();
		Group group = new Group("/path/" + communityId + "/projects/" + projectId);
		group.setDisplayedName(new I18nString("test"));
		when(unityClient.get(contains(projectId), eq(Group.class))).thenReturn(group);

		//when
		Optional<ProjectGroup> project = unityProjectGroupsDAO.get(communityId, projectId);

		//then
		assertThat(project).isPresent();
		assertThat(project.get().getId()).isEqualTo(projectId);
		assertThat(project.get().getName()).isEqualTo("test");
		assertThat(project.get().getCommunityId()).isEqualTo(communityId);
	}

	@Test
	void shouldCreateCommunity() {
		//given
		ProjectGroup project = ProjectGroup.builder()
			.id(UUID.randomUUID().toString())
			.communityId(UUID.randomUUID().toString())
			.name("test")
			.build();
		doNothing().when(unityClient).post(contains(project.getId()), any());
		doNothing().when(unityClient).post(contains(project.getCommunityId()), any());
		doNothing().when(unityClient).post(contains("users"), any());

		//when
		unityProjectGroupsDAO.create(project);

		//then
		verify(unityClient, times(1)).post(anyString(), any(), any());
		verify(unityClient, times(1)).put(anyString(), any());
	}

	@Test
	void shouldUpdateCommunity() {
		//given
		ProjectGroup project = ProjectGroup.builder()
				.id(UUID.randomUUID().toString())
				.communityId(UUID.randomUUID().toString())
				.name("test")
				.build();
		Group group = new Group("/path/"+project.getId());
		group.setDisplayedName(new I18nString("test"));
		when(unityClient.get(contains(project.getId()), eq(Group.class))).thenReturn(group);
		doNothing().when(unityClient).put(contains(project.getId()), eq(Group.class));

		//when
		unityProjectGroupsDAO.update(project);

		//then
		verify(unityClient, times(1)).put(anyString(), any());
	}

	@Test
	void shouldRemoveProject() {
		//given
		String communityId = UUID.randomUUID().toString();
		String projectId = UUID.randomUUID().toString();
		doNothing().when(unityClient).delete(contains(projectId), anyMap());

		//when
		unityProjectGroupsDAO.delete(communityId, projectId);

		//then
		verify(unityClient, times(1)).delete(anyString(), any());
	}

	@Test
	void shouldGetProjectAdministrators() {
		//given
		String communityId = UUID.randomUUID().toString();
		String projectId = UUID.randomUUID().toString();
		String groupPath = "/fenix/communities/"+ communityId + "/projects/" + projectId +"/users";
		when(userService.getAllUsersByRole(groupPath, PROJECT_ADMIN))
			.thenReturn(List.of(
				new FURMSUser("1", "firstName", "lastName", "email", Map.of()),
				new FURMSUser("3", "firstName", "lastName", "email", Map.of()))
			);

		//when
		List<FURMSUser> admins = unityProjectGroupsDAO.getAllAdmins(communityId, projectId);

		//then
		assertThat(admins).hasSize(2);
		assertThat(admins.stream()
			.allMatch(user -> user.id.id.equals("1") || user.id.id.equals("3"))).isTrue();
	}

	@Test
	void shouldAddAdminToProject() {
		//given
		String communityId = UUID.randomUUID().toString();
		String projectId = UUID.randomUUID().toString();
		String groupPath = "/fenix/communities/"+ communityId + "/projects/" + projectId +"/users";
		PersistentId userId = new PersistentId("userId");
		//when
		unityProjectGroupsDAO.addAdmin(communityId, projectId, userId);

		//then
		verify(userService, times(1)).addUserRole(eq(userId), eq(groupPath), eq(PROJECT_ADMIN));
		verify(userService, times(1)).addUserToGroup(eq(userId), eq(groupPath));
	}

	@Test
	void shouldRemoveAdminFromGroup() {
		//given
		String communityId = UUID.randomUUID().toString();
		String projectId = UUID.randomUUID().toString();
		String groupPath = "/fenix/communities/"+ communityId + "/projects/" + projectId +"/users";
		PersistentId userId = new PersistentId("userId");
		
		//when
		when(userService.getRoleValues(Mockito.any(), Mockito.anyString(), Mockito.any()))
			.thenReturn(Set.of("ADMIN"));
		unityProjectGroupsDAO.removeAdmin(communityId, projectId, userId);

		//then
		verify(userService, times(1)).getRoleValues(Mockito.eq(userId), Mockito.eq(groupPath), Mockito.eq(PROJECT_ADMIN));
		verify(userService, times(0)).removeUserRole(eq(userId), eq(groupPath), eq(PROJECT_ADMIN));
		verify(userService, times(1)).removeUserFromGroup(eq(userId), eq(groupPath));
	}

	@Test
	void shouldRemoveAdminRole() {
		//given
		String communityId = UUID.randomUUID().toString();
		String projectId = UUID.randomUUID().toString();
		String groupPath = "/fenix/communities/"+ communityId + "/projects/" + projectId +"/users";
		PersistentId userId = new PersistentId("userId");

		//when
		when(userService.getRoleValues(eq(userId), eq(groupPath), eq(PROJECT_ADMIN))).thenReturn(Set.of(PROJECT_ADMIN.unityRoleValue, PROJECT_USER.unityRoleValue));
		unityProjectGroupsDAO.removeAdmin(communityId, projectId, userId);

		//then
		verify(userService, times(1)).removeUserRole(eq(userId), eq(groupPath), eq(PROJECT_ADMIN));
		verify(userService, times(0)).removeUserFromGroup(eq(userId), eq(groupPath));
	}

	@Test
	void shouldGetUserAdministrators() {
		//given
		String communityId = UUID.randomUUID().toString();
		String projectId = UUID.randomUUID().toString();
		String groupPath = "/fenix/communities/"+ communityId + "/projects/" + projectId +"/users";
		when(userService.getAllUsersByRole(groupPath, PROJECT_USER))
			.thenReturn(List.of(
				new FURMSUser("1", "firstName", "lastName", "email", Map.of()),
				new FURMSUser("3", "firstName", "lastName", "email", Map.of()))
			);

		//when
		List<FURMSUser> admins = unityProjectGroupsDAO.getAllUsers(communityId, projectId);

		//then
		assertThat(admins).hasSize(2);
		assertThat(admins.stream()
			.allMatch(user -> user.id.id.equals("1") || user.id.id.equals("3"))).isTrue();
	}

	@Test
	void shouldAddUserToProject() {
		//given
		String communityId = UUID.randomUUID().toString();
		String projectId = UUID.randomUUID().toString();
		String groupPath = "/fenix/communities/"+ communityId + "/projects/" + projectId +"/users";
		PersistentId userId = new PersistentId("userId");
		//when
		unityProjectGroupsDAO.addUser(communityId, projectId, userId);

		//then
		verify(userService, times(1)).addUserRole(eq(userId), eq(groupPath), eq(PROJECT_USER));
		verify(userService, times(1)).addUserToGroup(eq(userId), eq(groupPath));
	}

	@Test
	void shouldRemoveUserFromGroup() {
		//given
		String communityId = UUID.randomUUID().toString();
		String projectId = UUID.randomUUID().toString();
		String groupPath = "/fenix/communities/"+ communityId + "/projects/" + projectId +"/users";
		PersistentId userId = new PersistentId("userId");

		//when
		when(userService.getRoleValues(Mockito.any(), Mockito.anyString(), Mockito.any()))
			.thenReturn(Set.of("USER"));
		unityProjectGroupsDAO.removeUser(communityId, projectId, userId);

		//then
		verify(userService, times(1)).getRoleValues(Mockito.eq(userId), Mockito.eq(groupPath), Mockito.eq(PROJECT_USER));
		verify(userService, times(0)).removeUserRole(eq(userId), eq(groupPath), eq(PROJECT_USER));
		verify(userService, times(1)).removeUserFromGroup(eq(userId), eq(groupPath));
	}

	@Test
	void shouldRemoveUserRole() {
		//given
		String communityId = UUID.randomUUID().toString();
		String projectId = UUID.randomUUID().toString();
		String groupPath = "/fenix/communities/"+ communityId + "/projects/" + projectId +"/users";
		PersistentId userId = new PersistentId("userId");

		//when
		when(userService.getRoleValues(eq(userId), eq(groupPath), eq(PROJECT_USER))).thenReturn(Set.of(PROJECT_ADMIN.unityRoleValue, PROJECT_USER.unityRoleValue));
		unityProjectGroupsDAO.removeUser(communityId, projectId, userId);

		//then
		verify(userService, times(1)).removeUserRole(eq(userId), eq(groupPath), eq(PROJECT_USER));
		verify(userService, times(0)).removeUserFromGroup(eq(userId), eq(groupPath));
	}
}