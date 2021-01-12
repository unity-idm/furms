/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.communites;

import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.spi.communites.CommunityRepository;
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
class CommunityServiceImpValidatorTest {
	@Mock
	private CommunityRepository communityRepository;

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
			.id("id")
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
			.id("id")
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
			.id("id")
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
		final String id = "id";

		when(communityRepository.exists(id)).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateDelete(id));
	}

	@Test
	void shouldNotPassDeleteForNonExistingId() {
		//given
		final String id = "id";

		when(communityRepository.exists(id)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateDelete(id));
	}

}