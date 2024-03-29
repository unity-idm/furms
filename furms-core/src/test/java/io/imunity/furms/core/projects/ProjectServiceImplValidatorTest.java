/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.projects;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplValidatorTest {
	@Mock
	private ProjectRepository projectRepository;
	@Mock
	private CommunityRepository communityRepository;

	@InjectMocks
	private ProjectServiceValidator validator;

	@Test
	void shouldPassCreateForUniqueName() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		Project project = Project.builder()
			.communityId(communityId)
			.name("name")
			.acronym("acronym")
			.researchField("research field")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now().plusWeeks(1))
			.build();

		when(communityRepository.exists(project.getCommunityId())).thenReturn(true);
		when(projectRepository.isNamePresent(project.getCommunityId(), project.getName())).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateCreate(project));
	}

	@Test
	void shouldNotPassCreateForNonUniqueName() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		Project project = Project.builder()
			.communityId(communityId)
			.name("name")
			.acronym("acronym")
			.researchField("research field")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now().plusWeeks(1))
			.build();

		when(projectRepository.isNamePresent(project.getCommunityId(), project.getName())).thenReturn(false);
		when(communityRepository.exists(project.getCommunityId())).thenReturn(true);
		Project secondProject = Project.builder().name("a").build();
		when(projectRepository.findById(any())).thenReturn(Optional.of(secondProject));

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(project));
	}

	@Test
	void shouldNotPassCreateForNonExistingCommunityId() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		Project project = Project.builder()
			.communityId(communityId)
			.name("name")
			.acronym("acronym")
			.researchField("research field")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now().plusWeeks(1))
			.build();

		when(communityRepository.exists(project.getCommunityId())).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(project));
	}

	@Test
	void shouldNotPassCreateForNullCommunityId() {
		//given
		Project project = Project.builder()
			.name("name")
			.acronym("acronym")
			.researchField("research field")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now().plusWeeks(1))
			.build();

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(project));
	}

	@Test
	void shouldNotPassCreateForNullAcronym() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		Project project = Project.builder()
			.communityId(communityId)
			.name("name")
			.researchField("research field")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now().plusWeeks(1))
			.build();

		when(projectRepository.isNamePresent(project.getCommunityId(), project.getName())).thenReturn(false);
		when(communityRepository.exists(project.getCommunityId())).thenReturn(true);
		Project secondProject = Project.builder().name("a").build();
		when(projectRepository.findById(any())).thenReturn(Optional.of(secondProject));

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(project));
	}

	@Test
	void shouldNotPassCreateForNullResearchField() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		Project project = Project.builder()
			.communityId(communityId)
			.name("name")
			.acronym("Acronym")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now().plusWeeks(1))
			.build();

		when(projectRepository.isNamePresent(project.getCommunityId(), project.getName())).thenReturn(false);
		when(communityRepository.exists(project.getCommunityId())).thenReturn(true);
		Project secondProject = Project.builder().name("a").build();
		when(projectRepository.findById(any())).thenReturn(Optional.of(secondProject));

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(project));
	}

	@Test
	void shouldNotPassCreateForInvalidTime() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		Project project = Project.builder()
			.communityId(communityId)
			.name("name")
			.researchField("research field")
			.utcStartTime(LocalDateTime.now().plusWeeks(1))
			.utcEndTime(LocalDateTime.now())
			.build();

		when(projectRepository.isNamePresent(project.getCommunityId(), project.getName())).thenReturn(false);
		when(communityRepository.exists(project.getCommunityId())).thenReturn(true);
		Project secondProject = Project.builder().name("a").build();
		when(projectRepository.findById(any())).thenReturn(Optional.of(secondProject));

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(project));
	}

	@Test
	void shouldPassUpdateForUniqueName() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		final Project project = Project.builder()
			.id(projectId)
			.communityId(communityId)
			.name("name")
			.acronym("acronym")
			.researchField("research field")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now().plusWeeks(1))
			.build();

		when(communityRepository.exists(project.getCommunityId())).thenReturn(true);
		when(projectRepository.exists(project.getId())).thenReturn(true);
		when(projectRepository.isNamePresent(project.getCommunityId(), project.getName())).thenReturn(true);
		when(projectRepository.findById(any())).thenReturn(Optional.of(project));

		//when+then
		assertDoesNotThrow(() -> validator.validateUpdate(project));
	}

	@Test
	void shouldNotPassUpdateForNonExistingObject() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		Project community = Project.builder()
			.id(projectId)
			.communityId(communityId)
			.name("name")
			.acronym("acronym")
			.researchField("research field")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now().plusWeeks(1))
			.build();

		when(projectRepository.exists(community.getId())).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(community));
	}

	@Test
	void shouldNotPassUpdateForNonUniqueName() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		Project community = Project.builder()
			.id(projectId)
			.communityId(communityId)
			.name("name")
			.acronym("acronym")
			.researchField("research field")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now().plusWeeks(1))
			.build();
		Project secondProject = Project.builder()
			.communityId(communityId)
			.name("a")
			.build();

		when(projectRepository.exists(community.getId())).thenReturn(true);
		when(projectRepository.isNamePresent(any(), any())).thenReturn(false);
		when(communityRepository.exists(any())).thenReturn(true);
		when(projectRepository.findById(any())).thenReturn(Optional.of(secondProject));
		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(community));
	}

	@Test
	void shouldPassDeleteForExistingId() {
		//given
		ProjectId projectId = new ProjectId(UUID.randomUUID());

		when(projectRepository.exists(projectId)).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateDelete(projectId));
	}

	@Test
	void shouldNotPassDeleteForNonExistingId() {
		//given
		ProjectId projectId = new ProjectId(UUID.randomUUID());

		when(projectRepository.exists(projectId)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateDelete(projectId));
	}

}