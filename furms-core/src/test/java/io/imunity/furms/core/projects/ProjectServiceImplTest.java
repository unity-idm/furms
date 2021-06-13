/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.projects;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.core.project_installation.ProjectInstallationService;
import io.imunity.furms.core.user_operation.UserOperationService;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.projects.*;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.InviteUserEvent;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.projects.ProjectGroupsDAO;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.TestInstance.Lifecycle;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@TestInstance(Lifecycle.PER_CLASS)
class ProjectServiceImplTest {
	@Mock
	private ProjectRepository projectRepository;
	@Mock
	private CommunityRepository communityRepository;
	@Mock
	private ProjectGroupsDAO projectGroupsDAO;
	@Mock
	private UsersDAO usersDAO;
	@Mock
	private ApplicationEventPublisher publisher;
	@Mock
	private AuthzService authzService;
	@Mock
	private ProjectInstallationService projectInstallationService;
	@Mock
	private UserOperationService userOperationService;

	private ProjectServiceImpl service;
	private InOrder orderVerifier;

	@BeforeAll
	void init() {
		MockitoAnnotations.initMocks(this);
		ProjectServiceValidator validator = new ProjectServiceValidator(projectRepository, communityRepository);
		service = new ProjectServiceImpl(
			projectRepository, projectGroupsDAO, usersDAO, validator,
			publisher, authzService, userOperationService, projectInstallationService
		);
		orderVerifier = inOrder(projectRepository, projectGroupsDAO, publisher);
	}

	@Test
	void shouldReturnProjectIfExistsInRepository() {
		//given
		String id = "id";
		when(projectRepository.findById(id)).thenReturn(Optional.of(Project.builder()
			.id(id)
			.name("userFacingName")
			.build())
		);

		//when
		Optional<Project> byId = service.findById(id);
		Optional<Project> otherId = service.findById("otherId");

		//then
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(id);
		assertThat(otherId).isEmpty();
	}

	@Test
	void shouldReturnAllProjectsIfExistsInRepository() {
		//given
		when(projectRepository.findAllByCommunityId("1")).thenReturn(Set.of(
			Project.builder().id("id1").name("userFacingName").build(),
			Project.builder().id("id2").name("userFacingName2").build()));

		//when
		Set<Project> allProjects = service.findAllByCommunityId("1");

		//then
		assertThat(allProjects).hasSize(2);
	}

	@Test
	void shouldAllowToCreateProject() {
		//given
		String id = UUID.randomUUID().toString();
		Project request = Project.builder()
			.id(id)
			.communityId("id")
			.name("userFacingName")
			.acronym("acronym")
			.researchField("research field")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now().plusWeeks(1))
			.leaderId(new PersistentId(UUID.randomUUID().toString()))
			.build();
		ProjectGroup groupRequest = ProjectGroup.builder()
			.id(id)
			.communityId("id")
			.name("userFacingName")
			.build();
		when(communityRepository.exists("id")).thenReturn(true);
		when(projectRepository.isNamePresent(request.getCommunityId(), request.getName())).thenReturn(true);
		when(projectRepository.create(request)).thenReturn(id);

		//when
		service.create(request);

		orderVerifier.verify(projectRepository).create(eq(request));
		orderVerifier.verify(projectGroupsDAO).create(eq(groupRequest));

		orderVerifier.verify(publisher).publishEvent(eq(new InviteUserEvent(request.getLeaderId(), new ResourceId(id, PROJECT))));
		orderVerifier.verify(publisher).publishEvent(eq(new CreateProjectEvent(id)));
	}

	@Test
	void shouldNotAllowToCreateProjectDueToNonUniqueuserFacingName() {
		//given
		String id = UUID.randomUUID().toString();
		Project request = Project.builder()
			.id(id)
			.name("userFacingName")
			.build();
		when(projectRepository.isNamePresent(request.getCommunityId(), request.getName())).thenReturn(false);

		//when
		assertThrows(IllegalArgumentException.class, () -> service.create(request));
		orderVerifier.verify(projectRepository, times(0)).create(eq(request));
		orderVerifier.verify(publisher, times(0)).publishEvent(eq(new CreateProjectEvent("id")));
		orderVerifier.verify(publisher, times(0)).publishEvent(eq(new InviteUserEvent(request.getLeaderId(), new ResourceId(id, PROJECT))));

	}

	@Test
	void shouldAllowToUpdateProject() {
		//given
		String id = UUID.randomUUID().toString();
		Project request = Project.builder()
			.id(id)
			.communityId("id")
			.name("userFacingName")
			.acronym("acronym")
			.researchField("research field")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now().plusWeeks(1))
			.leaderId(new PersistentId(UUID.randomUUID().toString()))
			.build();
		ProjectGroup groupRequest = ProjectGroup.builder()
			.id(id)
			.name("userFacingName")
			.communityId("id")
			.build();
		when(communityRepository.exists(request.getCommunityId())).thenReturn(true);
		when(projectRepository.exists(request.getId())).thenReturn(true);
		when(projectRepository.isNamePresent(request.getCommunityId(), request.getName())).thenReturn(true);
		when(projectRepository.findById(request.getId())).thenReturn(Optional.of(request));

		//when
		service.update(request);

		orderVerifier.verify(projectRepository).update(eq(request));
		orderVerifier.verify(projectGroupsDAO).update(eq(groupRequest));
		orderVerifier.verify(publisher).publishEvent(eq(new InviteUserEvent(request.getLeaderId(), new ResourceId(id, PROJECT))));
		orderVerifier.verify(publisher).publishEvent(eq(new UpdateProjectEvent(id)));
	}

	@Test
	void shouldAllowToUpdateLimitedProject() {
		//given
		LocalDateTime startTime = LocalDateTime.now();
		LocalDateTime endTime = LocalDateTime.now().plusWeeks(1);
		Project project = Project.builder()
			.id("id")
			.communityId("id")
			.description("description")
			.name("userFacingName")
			.acronym("acronym")
			.researchField("research field")
			.utcStartTime(startTime)
			.utcEndTime(endTime)
			.build();
		FurmsImage empty = FurmsImage.empty();
		ProjectAdminControlledAttributes request = new ProjectAdminControlledAttributes("id", "description_new", empty);
		ProjectGroup groupRequest = ProjectGroup.builder()
			.id("id")
			.name("userFacingName")
			.communityId("id")
			.build();
		when(communityRepository.exists(request.getId())).thenReturn(true);
		when(projectRepository.exists(request.getId())).thenReturn(true);
		when(projectRepository.findById(request.getId())).thenReturn(Optional.of(project));

		//when
		service.update(request);

		Project updatedProject = Project.builder()
			.id("id")
			.communityId("id")
			.description("description_new")
			.logo(empty)
			.name("userFacingName")
			.acronym("acronym")
			.researchField("research field")
			.utcStartTime(startTime)
			.utcEndTime(endTime)
			.build();
		orderVerifier.verify(projectRepository).update(eq(updatedProject));
		orderVerifier.verify(projectGroupsDAO).update(eq(groupRequest));
		orderVerifier.verify(publisher).publishEvent(eq(new UpdateProjectEvent("id")));
	}

	@Test
	void shouldAllowToDeleteProject() {
		//given
		String id = "id";
		String id2 = "id";
		when(projectRepository.exists(id)).thenReturn(true);
		List<FURMSUser> users = Arrays.asList(FURMSUser.builder().id(new PersistentId("id")).email("email@test.com").build());
		when(projectGroupsDAO.getAllUsers("id", "id")).thenReturn(users);
		
		//when
		service.delete(id, id2);

		orderVerifier.verify(projectRepository).delete(eq(id));
		orderVerifier.verify(projectGroupsDAO).delete(eq(id), eq(id2));
		orderVerifier.verify(publisher).publishEvent(eq(new RemoveProjectEvent("id", users)));
	}

	@Test
	void shouldNotAllowToDeleteProjectDueToProjectNotExists() {
		//given
		String id = "id";
		String id2 = "id2";
		when(projectRepository.exists(id)).thenReturn(false);

		//when
		assertThrows(IllegalArgumentException.class, () -> service.delete(id, id2));
		orderVerifier.verify(projectRepository, times(0)).delete(eq(id));
		orderVerifier.verify(publisher, times(0)).publishEvent(eq(new UpdateProjectEvent("id")));
	}

}