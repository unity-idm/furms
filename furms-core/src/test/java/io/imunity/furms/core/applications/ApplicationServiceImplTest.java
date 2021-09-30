/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.applications;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.applications.ApplicationRepository;
import io.imunity.furms.spi.projects.ProjectGroupsDAO;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

	@Mock
	private ApplicationRepository applicationRepository;
	@Mock
	private UsersDAO usersDAO;
	@Mock
	private ProjectGroupsDAO projectGroupsDAO;
	@Mock
	private ProjectRepository projectRepository;
	@Mock
	private AuthzService authzService;

	@InjectMocks
	private ApplicationServiceImpl applicationService;

	@Test
	void shouldFindAllApplyingUsers() {
		FenixUserId id = new FenixUserId("id");
		FenixUserId id1 = new FenixUserId("id1");
		FenixUserId id2 = new FenixUserId("id2");
		String projectId = "projectId";
		FURMSUser furmsUser = FURMSUser.builder()
			.fenixUserId(id)
			.email("email")
			.build();
		FURMSUser furmsUser1 = FURMSUser.builder()
			.fenixUserId(id1)
			.email("email1")
			.build();
		FURMSUser furmsUser2 = FURMSUser.builder()
			.fenixUserId(id2)
			.email("email2")
			.build();

		when(applicationRepository.findAllApplyingUsers(projectId)).thenReturn(
			Set.of(id1, id2)
		);
		when(usersDAO.getAllUsers()).thenReturn(
			List.of(furmsUser, furmsUser1, furmsUser2)
		);

		List<FURMSUser> allApplyingUsers = applicationService.findAllApplyingUsers(projectId);
		assertEquals(List.of(furmsUser1, furmsUser2), allApplyingUsers);
	}

	@Test
	void shouldFindAllAppliedProjectsIdsForCurrentUser() {
		FenixUserId id = new FenixUserId("id");
		when(authzService.getCurrentAuthNUser()).thenReturn(
			FURMSUser.builder()
				.fenixUserId(id)
				.email("email")
				.build()
		);

		applicationService.findAllAppliedProjectsIdsForCurrentUser();

		verify(applicationRepository).findAllAppliedProjectsIds(id);
	}

	@Test
	void shouldCreateForCurrentUser(){
		FenixUserId id = new FenixUserId("id");
		when(authzService.getCurrentAuthNUser()).thenReturn(
			FURMSUser.builder()
				.fenixUserId(id)
				.email("email")
				.build()
		);

		applicationService.createForCurrentUser("projectId");

		verify(applicationRepository).create("projectId", id);
	}

	@Test
	void shouldRemoveForCurrentUser(){
		FenixUserId id = new FenixUserId("id");
		when(authzService.getCurrentAuthNUser()).thenReturn(
			FURMSUser.builder()
				.fenixUserId(id)
				.email("email")
				.build()
		);

		applicationService.removeForCurrentUser("projectId");

		verify(applicationRepository).remove("projectId", id);
	}

	@Test
	void shouldAcceptApplication(){
		PersistentId persistentId = new PersistentId("id");
		FenixUserId fenixUserId = new FenixUserId("id");

		when(usersDAO.getPersistentId(fenixUserId)).thenReturn(persistentId);
		when(applicationRepository.existsBy("projectId", fenixUserId)).thenReturn(true);
		when(projectRepository.findById("projectId")).thenReturn(Optional.of(
			Project.builder()
				.communityId("communityId")
				.id("projectId")
				.build()
		));

		applicationService.accept("projectId", fenixUserId);

		verify(projectGroupsDAO).addProjectUser("communityId", "projectId", persistentId, Role.PROJECT_USER);
		verify(applicationRepository).remove("projectId", fenixUserId);
	}

	@Test
	void shouldRemoveApplication(){
		FenixUserId id = new FenixUserId("id");

		applicationService.remove("projectId", id);

		verify(applicationRepository).remove("projectId", id);
	}
}