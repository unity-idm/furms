/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.communites;

import io.imunity.furms.api.validation.exceptions.RemovingCommunityException;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityServiceImplValidatorTest {
	@Mock
	private CommunityRepository communityRepository;

	@Mock
	private ProjectRepository projectRepository;

	@InjectMocks
	private CommunityServiceValidator validator;

	@Test
	void shouldPassCreateForUniqueName() {
		//given
		Community community = Community.builder()
			.name("name")
			.build();

		when(communityRepository.isUniqueName(any())).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateCreate(community));
	}

	@Test
	void shouldNotPassCreateForNonUniqueName() {
		//given
		final Community community = Community.builder()
			.name("name")
			.build();

		when(communityRepository.isUniqueName(any())).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(community));
	}

	@Test
	void shouldPassUpdateForUniqueName() {
		//given
		final Community community = Community.builder()
			.id(new CommunityId(UUID.randomUUID()))
			.name("name")
			.build();

		when(communityRepository.exists(community.getId())).thenReturn(true);
		when(communityRepository.isUniqueName(any())).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateUpdate(community));
	}

	@Test
	void shouldNotPassUpdateForNonExistingObject() {
		//given
		Community community = Community.builder()
			.id(new CommunityId(UUID.randomUUID()))
			.name("name")
			.build();

		when(communityRepository.exists(community.getId())).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(community));
	}

	@Test
	void shouldNotPassUpdateForNonUniqueName() {
		//given
		Community community = Community.builder()
			.id(new CommunityId(UUID.randomUUID()))
			.name("name")
			.build();

		when(communityRepository.exists(community.getId())).thenReturn(true);
		when(communityRepository.isUniqueName(any())).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(community));
	}

	@Test
	void shouldPassDeleteForExistingId() {
		//given
		CommunityId id = new CommunityId(UUID.randomUUID());

		when(communityRepository.exists(id)).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateDelete(id));
	}

	@Test
	void shouldNotPassDeleteForNonExistingId() {
		//given
		CommunityId id = new CommunityId(UUID.randomUUID());

		when(communityRepository.exists(id)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateDelete(id));
	}

	@Test
	void shouldNotPassDeleteForExistingProjects() {
		//given
		CommunityId id = new CommunityId(UUID.randomUUID());

		when(communityRepository.exists(id)).thenReturn(true);
		when(projectRepository.findAllByCommunityId(id)).thenReturn(Set.of(mock(Project.class)));

		//when+then
		assertThrows(RemovingCommunityException.class, () -> validator.validateDelete(id));
	}

}