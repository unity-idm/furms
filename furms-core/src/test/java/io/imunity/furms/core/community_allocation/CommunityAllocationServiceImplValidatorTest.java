/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.community_allocation;

import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
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

	@InjectMocks
	private CommunityAllocationServiceValidator validator;

	@Test
	void shouldPassCreateForUniqueName() {
		//given
		CommunityAllocation service = CommunityAllocation.builder()
			.communityId("id")
			.resourceCreditId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(communityRepository.exists(service.communityId)).thenReturn(true);
		when(resourceCreditRepository.exists(service.resourceCreditId)).thenReturn(true);
		when(communityAllocationRepository.isUniqueName(any())).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNullAmount() {
		//given
		CommunityAllocation service = CommunityAllocation.builder()
			.communityId("id")
			.resourceCreditId("id")
			.name("name")
			.build();

		when(communityRepository.exists(service.communityId)).thenReturn(true);
		when(resourceCreditRepository.exists(service.resourceCreditId)).thenReturn(true);
		when(communityAllocationRepository.isUniqueName(any())).thenReturn(true);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNonUniqueName() {
		//given
		CommunityAllocation service = CommunityAllocation.builder()
			.communityId("id")
			.resourceCreditId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(communityRepository.exists(service.communityId)).thenReturn(true);
		when(resourceCreditRepository.exists(service.resourceCreditId)).thenReturn(true);
		when(communityAllocationRepository.isUniqueName(any())).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNonExistingResourceCreditId() {
		//given
		CommunityAllocation service = CommunityAllocation.builder()
			.communityId("id")
			.resourceCreditId("id")
			.name("name")
			.build();

		when(communityRepository.exists(service.communityId)).thenReturn(true);
		when(resourceCreditRepository.exists(service.resourceCreditId)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNullResourceCreditId() {
		//given
		CommunityAllocation service = CommunityAllocation.builder()
			.name("name")
			.communityId("id")
			.build();

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldPassUpdateForUniqueName() {
		//given
		final CommunityAllocation service = CommunityAllocation.builder()
			.id("id")
			.communityId("id")
			.resourceCreditId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(communityRepository.exists(service.communityId)).thenReturn(true);
		when(resourceCreditRepository.exists(service.resourceCreditId)).thenReturn(true);
		when(communityAllocationRepository.exists(service.id)).thenReturn(true);
		when(communityAllocationRepository.isUniqueName(any())).thenReturn(true);
		when(communityAllocationRepository.findById(any())).thenReturn(Optional.of(service));

		//when+then
		assertDoesNotThrow(() -> validator.validateUpdate(service));
	}

	@Test
	void shouldNotPassUpdateForNonExistingObject() {
		//given
		CommunityAllocation community = CommunityAllocation.builder()
			.id("id")
			.communityId("id")
			.resourceCreditId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(communityAllocationRepository.exists(community.id)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(community));
	}

	@Test
	void shouldNotPassUpdateForNonUniqueName() {
		//given
		CommunityAllocation resourceCredit = CommunityAllocation.builder()
			.id("id")
			.communityId("id")
			.resourceCreditId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		CommunityAllocation resourceCredit2 = CommunityAllocation.builder()
			.id("id")
			.communityId("id")
			.resourceCreditId("id")
			.name("name2")
			.amount(new BigDecimal(2))
			.build();

		when(communityAllocationRepository.exists(any())).thenReturn(true);
		when(communityAllocationRepository.findById(any())).thenReturn(Optional.of(resourceCredit2));
		when(communityRepository.exists(any())).thenReturn(true);
		when(resourceCreditRepository.exists(any())).thenReturn(true);
		when(communityAllocationRepository.isUniqueName(any())).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(resourceCredit));
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