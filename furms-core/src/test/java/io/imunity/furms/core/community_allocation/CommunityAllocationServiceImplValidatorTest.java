/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.community_allocation;

import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityAllocationServiceImplValidatorTest {
	@Mock
	private CommunityRepository communityRepository;
	@Mock
	private ResourceCreditRepository resourceCreditRepository;
	@Mock
	private CommunityAllocationRepository communityAllocationRepository;
	@Mock
	private ProjectAllocationRepository projectAllocationRepository;

	@InjectMocks
	private CommunityAllocationServiceValidator validator;

	@Test
	void shouldPassCreateForUniqueName() {
		//given
		CommunityAllocation communityAllocation = CommunityAllocation.builder()
			.communityId("id")
			.resourceCreditId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(communityRepository.exists(communityAllocation.communityId)).thenReturn(true);
		when(resourceCreditRepository.exists(communityAllocation.resourceCreditId)).thenReturn(true);
		when(communityAllocationRepository.isUniqueName(communityAllocation.communityId, communityAllocation.name)).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateCreate(communityAllocation));
	}

	@Test
	void shouldNotPassCreateForNullAmount() {
		//given
		CommunityAllocation communityAllocation = CommunityAllocation.builder()
			.communityId("id")
			.resourceCreditId("id")
			.name("name")
			.build();

		when(communityRepository.exists(communityAllocation.communityId)).thenReturn(true);
		when(resourceCreditRepository.exists(communityAllocation.resourceCreditId)).thenReturn(true);
		when(communityAllocationRepository.isUniqueName(communityAllocation.communityId, communityAllocation.name)).thenReturn(true);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(communityAllocation));
	}

	@Test
	void shouldNotPassCreateForNonUniqueName() {
		//given
		CommunityAllocation communityAllocation = CommunityAllocation.builder()
			.communityId("id")
			.resourceCreditId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(communityRepository.exists(communityAllocation.communityId)).thenReturn(true);
		when(resourceCreditRepository.exists(communityAllocation.resourceCreditId)).thenReturn(true);
		when(communityAllocationRepository.isUniqueName(communityAllocation.communityId, communityAllocation.name)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(communityAllocation));
	}

	@Test
	void shouldNotPassCreateForNonExistingResourceCreditId() {
		//given
		CommunityAllocation communityAllocation = CommunityAllocation.builder()
			.communityId("id")
			.resourceCreditId("id")
			.name("name")
			.build();

		when(communityRepository.exists(communityAllocation.communityId)).thenReturn(true);
		when(resourceCreditRepository.exists(communityAllocation.resourceCreditId)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(communityAllocation));
	}

	@Test
	void shouldNotPassCreateForNullResourceCreditId() {
		//given
		CommunityAllocation communityAllocation = CommunityAllocation.builder()
			.name("name")
			.communityId("id")
			.build();

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(communityAllocation));
	}

	@Test
	void shouldPassUpdateForUniqueName() {
		//given
		final CommunityAllocation communityAllocation = CommunityAllocation.builder()
			.id("id")
			.communityId("id")
			.resourceCreditId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(communityRepository.exists(communityAllocation.communityId)).thenReturn(true);
		when(resourceCreditRepository.exists(communityAllocation.resourceCreditId)).thenReturn(true);
		when(communityAllocationRepository.exists(communityAllocation.id)).thenReturn(true);
		when(communityAllocationRepository.isUniqueName(communityAllocation.communityId, communityAllocation.name)).thenReturn(true);
		when(communityAllocationRepository.findById(any())).thenReturn(Optional.of(communityAllocation));

		//when+then
		assertDoesNotThrow(() -> validator.validateUpdate(communityAllocation));
	}

	@Test
	void shouldNotPassUpdateForNonExistingObject() {
		//given
		CommunityAllocation communityAllocation = CommunityAllocation.builder()
			.id("id")
			.communityId("id")
			.resourceCreditId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(communityAllocationRepository.exists(communityAllocation.id)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(communityAllocation));
	}

	@Test
	void shouldNotPassUpdateForNonUniqueName() {
		//given
		CommunityAllocation communityAllocation = CommunityAllocation.builder()
			.id("id")
			.communityId("id")
			.resourceCreditId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		CommunityAllocation communityAllocation1 = CommunityAllocation.builder()
			.id("id")
			.communityId("id")
			.resourceCreditId("id")
			.name("name2")
			.amount(new BigDecimal(2))
			.build();

		when(communityAllocationRepository.exists(any())).thenReturn(true);
		when(communityAllocationRepository.findById(any())).thenReturn(Optional.of(communityAllocation1));
		when(communityRepository.exists(any())).thenReturn(true);
		when(resourceCreditRepository.exists(any())).thenReturn(true);
		when(communityAllocationRepository.isUniqueName(communityAllocation.communityId, communityAllocation.name)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(communityAllocation));
	}

	@Test
	void shouldPassDeleteForExistingId() {
		//given
		String id = "id";

		when(communityAllocationRepository.exists(id)).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateDelete(id));
	}

	@Test
	void shouldNotPassDeleteForNonExistingId() {
		//given
		String id = "id";

		when(communityAllocationRepository.exists(id)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateDelete(id));
	}

}