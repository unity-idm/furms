/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.projects;

import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectGroup;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.projects.ProjectGroupsDAO;
import io.imunity.furms.spi.projects.ProjectRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.TestInstance.Lifecycle;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@TestInstance(Lifecycle.PER_CLASS)
class ProjectServiceImplTest {
	@Mock
	private ProjectRepository projectRepository;
	@Mock
	private CommunityRepository communityRepository;
	@Mock
	private ProjectGroupsDAO projectGroupsDAO;

	private ProjectServiceImpl service;
	private InOrder orderVerifier;

	@BeforeAll
	void init() {
		MockitoAnnotations.initMocks(this);
		ProjectServiceValidator validator = new ProjectServiceValidator(projectRepository, communityRepository);
		service = new ProjectServiceImpl(projectRepository, projectGroupsDAO, validator);
		orderVerifier = inOrder(projectRepository, projectGroupsDAO);
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
		when(projectRepository.findAll()).thenReturn(Set.of(
			Project.builder().id("id1").name("userFacingName").build(),
			Project.builder().id("id2").name("userFacingName2").build()));

		//when
		Set<Project> allProjects = service.findAll();

		//then
		assertThat(allProjects).hasSize(2);
	}

	@Test
	void shouldAllowToCreateProject() {
		//given
		Project request = Project.builder()
			.id("id")
			.name("userFacingName")
			.build();
		ProjectGroup groupRequest = ProjectGroup.builder()
			.id("id")
			.name("userFacingName")
			.build();
		when(projectRepository.isUniqueName(request.getName())).thenReturn(true);
		when(projectRepository.create(request)).thenReturn("id");

		//when
		service.create(request);

		orderVerifier.verify(projectRepository).create(eq(request));
		orderVerifier.verify(projectGroupsDAO).create(eq(groupRequest));
	}

	@Test
	void shouldNotAllowToCreateProjectDueToNonUniqueuserFacingName() {
		//given
		Project request = Project.builder()
			.name("userFacingName")
			.build();
		when(projectRepository.isUniqueName(request.getName())).thenReturn(false);

		//when
		assertThrows(IllegalArgumentException.class, () -> service.create(request));
	}

	@Test
	void shouldAllowToUpdateProject() {
		//given
		Project request = Project.builder()
			.id("id")
			.name("userFacingName")
			.build();
		ProjectGroup groupRequest = ProjectGroup.builder()
			.id("id")
			.name("userFacingName")
			.build();
		when(projectRepository.exists(request.getId())).thenReturn(true);
		when(projectRepository.isUniqueName(request.getName())).thenReturn(true);

		//when
		service.update(request);

		orderVerifier.verify(projectRepository).update(eq(request));
		orderVerifier.verify(projectGroupsDAO).update(eq(groupRequest));
	}

	@Test
	void shouldAllowToDeleteProject() {
		//given
		String id = "id";
		String id2 = "id";
		when(projectRepository.exists(id)).thenReturn(true);

		//when
		service.delete(id, id2);

		orderVerifier.verify(projectRepository).delete(eq(id));
		orderVerifier.verify(projectGroupsDAO).delete(eq(id), eq(id2));
	}

	@Test
	void shouldNotAllowToDeleteProjectDueToProjectNotExists() {
		//given
		String id = "id";
		String id2 = "id2";
		when(projectRepository.exists(id)).thenReturn(false);

		//when
		assertThrows(IllegalArgumentException.class, () -> service.delete(id, id2));
	}

}