/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.projects;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.authz.CapabilityCollector;
import io.imunity.furms.api.project_installation.ProjectInstallationsService;
import io.imunity.furms.core.invitations.InvitatoryService;
import io.imunity.furms.core.project_installation.ProjectInstallationService;
import io.imunity.furms.core.user_operation.UserOperationService;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectAdminControlledAttributes;
import io.imunity.furms.domain.projects.ProjectCreatedEvent;
import io.imunity.furms.domain.projects.ProjectGroup;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.projects.ProjectRemovedEvent;
import io.imunity.furms.domain.projects.ProjectUpdatedEvent;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.projects.ProjectGroupsDAO;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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
	private ProjectInstallationsService projectInstallationsService;
	@Mock
	private UserOperationService userOperationService;
	@Mock
	private CapabilityCollector capabilityCollector;
	@Mock
	private InvitatoryService invitatoryService;

	private ProjectServiceImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		ProjectServiceValidator validator = new ProjectServiceValidator(projectRepository, communityRepository);
		service = new ProjectServiceImpl(
			projectRepository, projectGroupsDAO, usersDAO, validator,
			publisher, authzService, userOperationService, projectInstallationService,
			projectInstallationsService, capabilityCollector, invitatoryService);
		orderVerifier = inOrder(projectRepository, projectGroupsDAO, publisher);
	}

	@Test
	void shouldReturnProjectIfExistsInRepository() {
		//given
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		when(projectRepository.findById(projectId)).thenReturn(Optional.of(Project.builder()
			.id(projectId)
			.name("userFacingName")
			.build())
		);

		//when
		Optional<Project> byId = service.findById(projectId);
		Optional<Project> otherId = service.findById(new ProjectId(UUID.randomUUID()));

		//then
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(projectId);
		assertThat(otherId).isEmpty();
	}

	@Test
	void shouldReturnAllProjectsIfExistsInRepository() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		ProjectId projectId1 = new ProjectId(UUID.randomUUID());
		when(projectRepository.findAllByCommunityId(communityId)).thenReturn(Set.of(
			Project.builder().id(projectId).name("userFacingName").build(),
			Project.builder().id(projectId1).name("userFacingName2").build()));

		//when
		Set<Project> allProjects = service.findAllByCommunityId(communityId);

		//then
		assertThat(allProjects).hasSize(2);
	}

	@Test
	void shouldAllowToCreateProject() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		Project request = Project.builder()
			.id(projectId)
			.communityId(communityId)
			.name("userFacingName")
			.acronym("acronym")
			.researchField("research field")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now().plusWeeks(1))
			.leaderId(new PersistentId(UUID.randomUUID().toString()))
			.build();
		ProjectGroup groupRequest = ProjectGroup.builder()
			.id(projectId)
			.communityId(communityId)
			.name("userFacingName")
			.build();
		when(communityRepository.exists(communityId)).thenReturn(true);
		when(projectRepository.isNamePresent(request.getCommunityId(), request.getName())).thenReturn(true);
		when(projectRepository.findById(projectId)).thenReturn(Optional.of(request));
		when(projectRepository.create(request)).thenReturn(projectId);

		//when
		service.create(request);

		orderVerifier.verify(projectRepository).create(eq(request));
		orderVerifier.verify(projectGroupsDAO).create(eq(groupRequest));

		orderVerifier.verify(publisher).publishEvent(eq(new ProjectCreatedEvent(request)));
	}

	@Test
	void shouldNotAllowToCreateProjectDueToNonUniqueuserFacingName() {
		//given
		String id = UUID.randomUUID().toString();
		Project request = Project.builder()
			.id(id)
			.name("userFacingName")
			.build();

		//when
		assertThrows(IllegalArgumentException.class, () -> service.create(request));
		orderVerifier.verify(projectRepository, times(0)).create(eq(request));
		orderVerifier.verify(publisher, times(0)).publishEvent(eq(new ProjectCreatedEvent(null)));
	}

	@Test
	void shouldAllowToUpdateProject() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		Project request = Project.builder()
			.id(projectId)
			.communityId(communityId)
			.name("userFacingName")
			.acronym("acronym")
			.researchField("research field")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now().plusWeeks(1))
			.leaderId(new PersistentId(UUID.randomUUID().toString()))
			.build();
		ProjectGroup groupRequest = ProjectGroup.builder()
			.id(projectId)
			.name("userFacingName")
			.communityId(communityId)
			.build();
		when(communityRepository.exists(request.getCommunityId())).thenReturn(true);
		when(projectRepository.exists(request.getId())).thenReturn(true);
		when(projectRepository.isNamePresent(request.getCommunityId(), request.getName())).thenReturn(true);
		when(projectRepository.findById(request.getId())).thenReturn(Optional.of(request));

		//when
		service.update(request);

		orderVerifier.verify(projectRepository).update(eq(request));
		orderVerifier.verify(projectGroupsDAO).update(eq(groupRequest));
		orderVerifier.verify(publisher).publishEvent(eq(new ProjectUpdatedEvent(request, request)));
	}

	@Test
	void shouldAllowToUpdateLimitedProject() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		LocalDateTime startTime = LocalDateTime.now();
		LocalDateTime endTime = LocalDateTime.now().plusWeeks(1);
		Project project = Project.builder()
			.id(projectId)
			.communityId(communityId)
			.description("description")
			.name("userFacingName")
			.acronym("acronym")
			.researchField("research field")
			.utcStartTime(startTime)
			.utcEndTime(endTime)
			.build();
		FurmsImage empty = FurmsImage.empty();
		ProjectAdminControlledAttributes request = new ProjectAdminControlledAttributes(
				projectId, "description_new", "researchField_new", empty);
		ProjectGroup groupRequest = ProjectGroup.builder()
			.id(projectId)
			.name("userFacingName")
			.communityId(communityId)
			.build();
		when(projectRepository.exists(request.getId())).thenReturn(true);
		when(projectRepository.findById(request.getId())).thenReturn(Optional.of(project));

		//when
		service.update(request);

		Project updatedProject = Project.builder()
			.id(projectId)
			.communityId(communityId)
			.description("description_new")
			.logo(empty)
			.name("userFacingName")
			.acronym("acronym")
			.researchField("researchField_new")
			.utcStartTime(startTime)
			.utcEndTime(endTime)
			.build();
		orderVerifier.verify(projectRepository).update(eq(updatedProject));
		orderVerifier.verify(projectGroupsDAO).update(eq(groupRequest));
		orderVerifier.verify(publisher).publishEvent(eq(new ProjectUpdatedEvent( project, updatedProject)));
	}

	@Test
	void shouldAllowToDeleteProject() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		when(projectRepository.exists(projectId)).thenReturn(true);
		List<FURMSUser> users = Collections.singletonList(FURMSUser.builder().id(new PersistentId("id")).email("email@test.com").build());
		when(projectGroupsDAO.getAllUsers(communityId, projectId)).thenReturn(users);
		Project project = Project.builder().build();
		when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

		//when
		service.delete(projectId, communityId);

		orderVerifier.verify(projectRepository).delete(eq(projectId));
		orderVerifier.verify(projectGroupsDAO).delete(eq(communityId), eq(projectId));
		orderVerifier.verify(publisher).publishEvent(eq(new ProjectRemovedEvent(users, project)));
	}

	@Test
	void shouldNotAllowToDeleteProjectDueToProjectNotExists() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		when(projectRepository.exists(projectId)).thenReturn(false);

		//when
		assertThrows(IllegalArgumentException.class, () -> service.delete(projectId, communityId));
		orderVerifier.verify(projectRepository, times(0)).delete(eq(projectId));
		orderVerifier.verify(publisher, times(0)).publishEvent(eq(new ProjectUpdatedEvent(null, null)));
	}

}