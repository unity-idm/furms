/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.applications;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.applications.AcceptProjectApplicationEvent;
import io.imunity.furms.domain.applications.CreateProjectApplicationEvent;
import io.imunity.furms.domain.applications.RemoveProjectApplicationEvent;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.applications.ApplicationRepository;
import io.imunity.furms.spi.notifications.NotificationDAO;
import io.imunity.furms.spi.projects.ProjectGroupsDAO;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectApplicationsServiceImplTest {

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
	@Mock
	private NotificationDAO notificationDAO;
	@Mock
	private ApplicationEventPublisher publisher;

	@InjectMocks
	private ProjectApplicationsServiceImpl applicationService;

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
		PersistentId persistentId = new PersistentId("id");
		FURMSUser user = FURMSUser.builder()
			.id(persistentId)
			.fenixUserId(id)
			.email("email")
			.build();
		when(authzService.getCurrentAuthNUser()).thenReturn(user);
		when(projectRepository.findById("projectId")).thenReturn(Optional.of(
			Project.builder()
				.communityId("communityId")
				.name("name")
				.id("projectId")
				.build()
		));
		when(projectGroupsDAO.getAllAdmins("communityId", "projectId"))
			.thenReturn(List.of(user));

		applicationService.createForCurrentUser("projectId");

		verify(applicationRepository).create("projectId", id);
		verify(notificationDAO).notifyAdminAboutApplicationRequest(persistentId, "projectId",  "name", "email");
		verify(publisher).publishEvent(new CreateProjectApplicationEvent(id, "projectId", List.of(user)));
	}

	@Test
	void shouldRemoveForCurrentUser(){
		FenixUserId id = new FenixUserId("id");
		FURMSUser user = FURMSUser.builder()
			.fenixUserId(id)
			.email("email")
			.build();
		when(authzService.getCurrentAuthNUser()).thenReturn(user);
		when(projectRepository.findById("projectId")).thenReturn(Optional.of(
			Project.builder()
				.communityId("communityId")
				.id("projectId")
				.build()
		));
		when(projectGroupsDAO.getAllAdmins("communityId", "projectId"))
			.thenReturn(List.of(user));
		when(applicationRepository.existsBy("projectId", id)).thenReturn(true);

		applicationService.removeForCurrentUser("projectId");

		verify(applicationRepository).remove("projectId", id);
		verify(publisher).publishEvent(new RemoveProjectApplicationEvent(id, "projectId", List.of(user)));
	}

	@Test
	void shouldAcceptApplication(){
		String projectId = UUID.randomUUID().toString();
		PersistentId persistentId = new PersistentId("id");
		FenixUserId fenixUserId = new FenixUserId("id");
		FURMSUser user = FURMSUser.builder()
			.fenixUserId(fenixUserId)
			.email("email")
			.build();

		when(usersDAO.getPersistentId(fenixUserId)).thenReturn(persistentId);
		when(applicationRepository.existsBy(projectId, fenixUserId)).thenReturn(true);
		when(projectRepository.findById(projectId)).thenReturn(Optional.of(
			Project.builder()
				.name("name")
				.communityId("communityId")
				.id(projectId)
				.build()
		));
		when(projectGroupsDAO.getAllAdmins("communityId", projectId))
			.thenReturn(List.of(user));
		when(applicationRepository.existsBy(projectId, fenixUserId)).thenReturn(true);


		applicationService.accept(projectId, fenixUserId);

		verify(projectGroupsDAO).addProjectUser("communityId", projectId, persistentId, Role.PROJECT_USER);
		verify(applicationRepository).remove(projectId, fenixUserId);
		verify(publisher).publishEvent(new AcceptProjectApplicationEvent(fenixUserId, projectId, List.of(user)));
		verify(notificationDAO).notifyUserAboutApplicationAcceptance(persistentId, "name");
	}

	@Test
	void shouldRemoveApplication(){
		FenixUserId id = new FenixUserId("id");
		PersistentId persistentId = new PersistentId("id");
		FURMSUser user = FURMSUser.builder()
			.id(persistentId)
			.fenixUserId(id)
			.email("email")
			.build();

		when(projectRepository.findById("projectId")).thenReturn(Optional.of(
			Project.builder()
				.communityId("communityId")
				.id("projectId")
				.name("name")
				.build()
		));
		when(applicationRepository.existsBy("projectId", id)).thenReturn(true);
		when(usersDAO.getPersistentId(id)).thenReturn(persistentId);
		when(projectGroupsDAO.getAllAdmins("communityId", "projectId"))
			.thenReturn(List.of(user));

		applicationService.remove("projectId", id);

		verify(applicationRepository).remove("projectId", id);
		verify(publisher).publishEvent(new RemoveProjectApplicationEvent(id, "projectId", List.of(user)));
		verify(notificationDAO).notifyUserAboutApplicationRejection(persistentId, "name");
	}
}