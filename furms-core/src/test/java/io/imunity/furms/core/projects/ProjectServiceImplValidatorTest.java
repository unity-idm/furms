/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.projects;

import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
		Project community = Project.builder()
			.name("name")
			.build();

		when(projectRepository.isUniqueName(any())).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateCreate(community));
	}

	@Test
	void shouldNotPassCreateForNonUniqueName() {
		//given
		final Project community = Project.builder()
			.name("name")
			.build();

		when(projectRepository.isUniqueName(any())).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(community));
	}

	@Test
	void shouldPassUpdateForUniqueName() {
		//given
		final Project community = Project.builder()
			.id("id")
			.name("name")
			.build();

		when(projectRepository.exists(community.getId())).thenReturn(true);
		when(projectRepository.isUniqueName(any())).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateUpdate(community));
	}

	@Test
	void shouldNotPassUpdateForNonExistingObject() {
		//given
		Project community = Project.builder()
			.id("id")
			.name("name")
			.build();

		when(projectRepository.exists(community.getId())).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(community));
	}

	@Test
	void shouldNotPassUpdateForNonUniqueName() {
		//given
		Project community = Project.builder()
			.id("id")
			.name("name")
			.build();

		when(projectRepository.exists(community.getId())).thenReturn(true);
		when(projectRepository.isUniqueName(any())).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(community));
	}

	@Test
	void shouldPassDeleteForExistingId() {
		//given
		final String id = "id";

		when(projectRepository.exists(id)).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateDelete(id));
	}

	@Test
	void shouldNotPassDeleteForNonExistingId() {
		//given
		final String id = "id";

		when(projectRepository.exists(id)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateDelete(id));
	}

}