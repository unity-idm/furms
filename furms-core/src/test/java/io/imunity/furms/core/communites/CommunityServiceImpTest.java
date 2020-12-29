/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.communites;

import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.communites.CommunityWebClient;
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
import static org.mockito.Mockito.*;

@TestInstance(Lifecycle.PER_CLASS)
class CommunityServiceImpTest {
	@Mock
	private CommunityRepository communityRepository;
	@Mock
	private CommunityWebClient communityWebClient;

	private CommunityServiceImp service;
	private InOrder orderVerifier;

	@BeforeAll
	void init() {
		MockitoAnnotations.initMocks(this);
		CommunityServiceValidator validator = new CommunityServiceValidator(communityRepository);
		service = new CommunityServiceImp(communityRepository, communityWebClient, validator);
		orderVerifier = inOrder(communityRepository, communityWebClient);
	}

	@Test
	void shouldReturnCommunityIfExistsInRepository() {
		//given
		String id = "id";
		when(communityRepository.findById(id)).thenReturn(Optional.of(Community.builder()
			.id(id)
			.name("userFacingName")
			.build())
		);

		//when
		Optional<Community> byId = service.findById(id);
		Optional<Community> otherId = service.findById("otherId");

		//then
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(id);
		assertThat(otherId).isEmpty();
	}

	@Test
	void shouldReturnAllCommunitysIfExistsInRepository() {
		//given
		when(communityRepository.findAll()).thenReturn(Set.of(
			Community.builder().id("id1").name("userFacingName").build(),
			Community.builder().id("id2").name("userFacingName2").build()));

		//when
		Set<Community> allCommunitys = service.findAll();

		//then
		assertThat(allCommunitys).hasSize(2);
	}

	@Test
	void shouldAllowToCreateCommunity() {
		//given
		Community request = Community.builder()
			.name("userFacingName")
			.build();
		when(communityRepository.isUniqueName(request.getName())).thenReturn(true);

		//when
		service.create(request);

		orderVerifier.verify(communityRepository).create(eq(request));
		orderVerifier.verify(communityWebClient).create(eq(request));
	}

	@Test
	void shouldNotAllowToCreateCommunityDueToNonUniqueuserFacingName() {
		//given
		Community request = Community.builder()
			.name("userFacingName")
			.build();
		when(communityRepository.isUniqueName(request.getName())).thenReturn(false);

		//when
		assertThrows(IllegalArgumentException.class, () -> service.create(request));
	}

	@Test
	void shouldAllowToUpdateCommunity() {
		//given
		Community request = Community.builder()
			.id("id")
			.name("userFacingName")
			.build();
		when(communityRepository.exists(request.getId())).thenReturn(true);
		when(communityRepository.isUniqueName(request.getName())).thenReturn(true);

		//when
		service.update(request);

		orderVerifier.verify(communityRepository).update(eq(request));
		orderVerifier.verify(communityWebClient).update(eq(request));
	}

	@Test
	void shouldAllowToDeleteCommunity() {
		//given
		String id = "id";
		when(communityRepository.exists(id)).thenReturn(true);

		//when
		service.delete(id);

		orderVerifier.verify(communityRepository).delete(eq(id));
		orderVerifier.verify(communityWebClient).delete(eq(id));
	}

	@Test
	void shouldNotAllowToDeleteCommunityDueToCommunityNotExists() {
		//given
		String id = "id";
		when(communityRepository.exists(id)).thenReturn(false);

		//when
		assertThrows(IllegalArgumentException.class, () -> service.delete(id));
	}

}