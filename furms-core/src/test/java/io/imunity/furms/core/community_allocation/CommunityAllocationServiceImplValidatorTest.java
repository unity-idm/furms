/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.community_allocation;

import io.imunity.furms.api.validation.exceptions.CommunityAllocationAmountNotEnoughException;
import io.imunity.furms.api.validation.exceptions.CommunityAllocationUpdateAboveCreditAmountException;
import io.imunity.furms.api.validation.exceptions.CommunityAllocationUpdateAboveCreditAvailableAmountException;
import io.imunity.furms.api.validation.exceptions.CommunityAllocationUpdateBelowDistributedAmountException;
import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_credits.ResourceCreditId;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
	private static final ResourceCredit CREDIT_OF_TEN = ResourceCredit.builder()
			.amount(BigDecimal.TEN)
			.build();

	@Test
	void shouldPassCreateForUniqueName() {
		//given
		CommunityAllocation communityAllocation = CommunityAllocation.builder()
			.communityId(UUID.randomUUID().toString())
			.resourceCreditId(UUID.randomUUID().toString())
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(communityRepository.exists(communityAllocation.communityId)).thenReturn(true);
		when(resourceCreditRepository.exists(communityAllocation.resourceCreditId)).thenReturn(true);
		when(communityAllocationRepository.isUniqueName(any())).thenReturn(true);
		when(communityAllocationRepository.getAvailableAmount(communityAllocation.resourceCreditId)).thenReturn(BigDecimal.ONE);

		//when+then
		assertDoesNotThrow(() -> validator.validateCreate(communityAllocation));
	}

	@Test
	void shouldNotPassCreateForNullAmount() {
		//given
		CommunityAllocation communityAllocation = CommunityAllocation.builder()
			.communityId(UUID.randomUUID().toString())
			.resourceCreditId(UUID.randomUUID().toString())
			.name("name")
			.build();

		when(communityRepository.exists(communityAllocation.communityId)).thenReturn(true);
		when(resourceCreditRepository.exists(communityAllocation.resourceCreditId)).thenReturn(true);
		when(communityAllocationRepository.isUniqueName(any())).thenReturn(true);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(communityAllocation));
	}

	@Test
	void shouldNotPassCreateForNonUniqueName() {
		//given
		CommunityAllocation communityAllocation = CommunityAllocation.builder()
			.communityId(UUID.randomUUID().toString())
			.resourceCreditId(UUID.randomUUID().toString())
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(communityRepository.exists(communityAllocation.communityId)).thenReturn(true);
		when(resourceCreditRepository.exists(communityAllocation.resourceCreditId)).thenReturn(true);
		when(communityAllocationRepository.isUniqueName(any())).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(communityAllocation));
	}

	@Test
	void shouldNotPassCreateForNonExistingResourceCreditId() {
		//given
		CommunityAllocation communityAllocation = CommunityAllocation.builder()
			.communityId(UUID.randomUUID().toString())
			.resourceCreditId(UUID.randomUUID().toString())
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
			.communityId(UUID.randomUUID().toString())
			.build();

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(communityAllocation));
	}

	@Test
	void shouldNotPassCreateForExpiredResourceCredit() {
		//given
		CommunityAllocation communityAllocation = CommunityAllocation.builder()
				.communityId(UUID.randomUUID().toString())
				.resourceCreditId(UUID.randomUUID().toString())
				.name("name")
				.build();

		when(communityRepository.exists(communityAllocation.communityId)).thenReturn(true);
		when(resourceCreditRepository.exists(communityAllocation.resourceCreditId)).thenReturn(true);
		when(resourceCreditRepository.findById(communityAllocation.resourceCreditId))
				.thenReturn(Optional.of(ResourceCredit.builder()
					.utcEndTime(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).minusMinutes(1L))
					.build()));

		//when+then
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> validator.validateCreate(communityAllocation));
		assertThat(ex.getMessage()).isEqualTo("Cannot use expired Resource credit");
	}


	@Test
	void shouldNotPassCreateForAmountGreaterThanResourceCreditAmount() {
		//given
		CommunityAllocation communityAllocation = CommunityAllocation.builder()
				.communityId(UUID.randomUUID().toString())
				.resourceCreditId(UUID.randomUUID().toString())
				.name("name")
				.amount(BigDecimal.TEN)
				.build();

		when(communityRepository.exists(communityAllocation.communityId)).thenReturn(true);
		when(resourceCreditRepository.exists(communityAllocation.resourceCreditId)).thenReturn(true);
		when(communityAllocationRepository.isUniqueName(any())).thenReturn(true);
		when(resourceCreditRepository.findById(communityAllocation.resourceCreditId))
				.thenReturn(Optional.of(ResourceCredit.builder()
						.utcEndTime(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).plusMinutes(1L))
						.build()));
		when(communityAllocationRepository.getAvailableAmount(communityAllocation.resourceCreditId)).thenReturn(BigDecimal.ONE);

		//when+then
		final CommunityAllocationAmountNotEnoughException ex = assertThrows(CommunityAllocationAmountNotEnoughException.class,
				() -> validator.validateCreate(communityAllocation));
		assertThat(ex.getMessage()).isEqualTo("There is no available Resource Credit amount to cover " +
				"requested amount.");
	}

	@Test
	void shouldPassUpdateForUniqueName() {
		//given
		CommunityAllocation communityAllocation = CommunityAllocation.builder()
			.id(UUID.randomUUID().toString())
			.communityId(UUID.randomUUID().toString())
			.resourceCreditId(UUID.randomUUID().toString())
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(projectAllocationRepository.getAvailableAmount(communityAllocation.id)).thenReturn(BigDecimal.valueOf(1));
		when(communityAllocationRepository.getAvailableAmount(communityAllocation.resourceCreditId)).thenReturn(BigDecimal.valueOf(2));
		when(resourceCreditRepository.findById(communityAllocation.resourceCreditId)).thenReturn(Optional.of(CREDIT_OF_TEN));
		when(communityAllocationRepository.isUniqueName(any())).thenReturn(true);
		when(communityAllocationRepository.findById(any())).thenReturn(Optional.of(communityAllocation));

		//when+then
		assertDoesNotThrow(() -> validator.validateUpdate(communityAllocation));
	}

	@Test
	void shouldForbidToUpdateBelowDistributedAmount() {
		CommunityAllocationId id = new CommunityAllocationId(UUID.randomUUID());
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ResourceCreditId resourceCreditId = new ResourceCreditId(UUID.randomUUID());

		CommunityAllocation originalAllocation = CommunityAllocation.builder()
				.id(id)
				.communityId(communityId)
				.resourceCreditId(resourceCreditId)
				.name("name")
				.amount(new BigDecimal(10))
				.build();

		CommunityAllocation updatedAllocation = CommunityAllocation.builder()
				.id(id)
				.communityId(communityId)
				.resourceCreditId(resourceCreditId)
				.name("name")
				.amount(new BigDecimal(4))
				.build();

		when(projectAllocationRepository.getAvailableAmount(updatedAllocation.id)).thenReturn(BigDecimal.valueOf(5));
		when(communityAllocationRepository.findById(any())).thenReturn(Optional.of(originalAllocation));

		Throwable error = catchThrowable(() -> validator.validateUpdate(updatedAllocation));
		
		assertThat(error).isInstanceOf(CommunityAllocationUpdateBelowDistributedAmountException.class);
	}
	
	@Test
	void shouldForbidToUpdateAboveCreditAmount() {
		CommunityAllocationId id = new CommunityAllocationId(UUID.randomUUID());
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ResourceCreditId resourceCreditId = new ResourceCreditId(UUID.randomUUID());

		CommunityAllocation originalAllocation = CommunityAllocation.builder()
				.id(id)
				.communityId(communityId)
				.resourceCreditId(resourceCreditId)
				.name("name")
				.amount(new BigDecimal(10))
				.build();

		CommunityAllocation updatedAllocation = CommunityAllocation.builder()
				.id(id)
				.communityId(communityId)
				.resourceCreditId(resourceCreditId)
				.name("name")
				.amount(new BigDecimal(11))
				.build();
		
		when(projectAllocationRepository.getAvailableAmount(updatedAllocation.id)).thenReturn(BigDecimal.valueOf(10));
		when(resourceCreditRepository.findById(updatedAllocation.resourceCreditId)).thenReturn(Optional.of(CREDIT_OF_TEN));
		when(communityAllocationRepository.findById(any())).thenReturn(Optional.of(originalAllocation));


		Throwable error = catchThrowable(() -> validator.validateUpdate(updatedAllocation));
		
		assertThat(error).isInstanceOf(CommunityAllocationUpdateAboveCreditAmountException.class);
	}

	@Test
	void shouldForbidToUpdateAboveCreditAvailableAmount() {
		CommunityAllocationId id = new CommunityAllocationId(UUID.randomUUID());
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ResourceCreditId resourceCreditId = new ResourceCreditId(UUID.randomUUID());
		CommunityAllocation originalAllocation = CommunityAllocation.builder()
				.id(id)
				.communityId(communityId)
				.resourceCreditId(resourceCreditId)
				.name("name")
				.amount(new BigDecimal(3))
				.build();

		CommunityAllocation updatedAllocation = CommunityAllocation.builder()
				.id(id)
				.communityId(communityId)
				.resourceCreditId(resourceCreditId)
				.name("name")
				.amount(new BigDecimal(6))
				.build();
		
		when(projectAllocationRepository.getAvailableAmount(updatedAllocation.id)).thenReturn(BigDecimal.valueOf(10));
		when(communityAllocationRepository.getAvailableAmount(updatedAllocation.resourceCreditId)).thenReturn(BigDecimal.valueOf(2));
		when(resourceCreditRepository.findById(updatedAllocation.resourceCreditId)).thenReturn(Optional.of(CREDIT_OF_TEN));
		when(communityAllocationRepository.findById(any())).thenReturn(Optional.of(originalAllocation));


		Throwable error = catchThrowable(() -> validator.validateUpdate(updatedAllocation));
		
		assertThat(error).isInstanceOf(CommunityAllocationUpdateAboveCreditAvailableAmountException.class);
	}
	
	@Test
	void shouldNotPassUpdateForNonExistingObject() {
		//given
		CommunityAllocation communityAllocation = CommunityAllocation.builder()
			.id(UUID.randomUUID().toString())
			.communityId(UUID.randomUUID().toString())
			.resourceCreditId(UUID.randomUUID().toString())
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(communityAllocationRepository.findById(communityAllocation.id)).thenReturn(Optional.empty());

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(communityAllocation));
	}

	//FIXME this test is completely broken
	@Test
	void shouldNotPassUpdateForNonUniqueName() {
		//given
		CommunityAllocationId id = new CommunityAllocationId(UUID.randomUUID());
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ResourceCreditId resourceCreditId = new ResourceCreditId(UUID.randomUUID());

		CommunityAllocation communityAllocation = CommunityAllocation.builder()
			.id(id)
			.communityId(communityId)
			.resourceCreditId(resourceCreditId)
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		CommunityAllocation communityAllocation1 = CommunityAllocation.builder()
			.id(id)
			.communityId(communityId)
			.resourceCreditId(resourceCreditId)
			.name("name2")
			.amount(new BigDecimal(2))
			.build();

		when(projectAllocationRepository.getAvailableAmount(communityAllocation.id)).thenReturn(BigDecimal.valueOf(2));
		when(resourceCreditRepository.findById(communityAllocation.resourceCreditId)).thenReturn(Optional.of(CREDIT_OF_TEN));
		when(communityAllocationRepository.getAvailableAmount(communityAllocation.resourceCreditId)).thenReturn(BigDecimal.valueOf(2));
		when(communityAllocationRepository.findById(communityAllocation.id)).thenReturn(Optional.of(communityAllocation1));
		

		//when+then
		assertThrows(DuplicatedNameValidationError.class, () -> validator.validateUpdate(communityAllocation));
	}

	@Test
	void shouldPassDeleteForExistingId() {
		//given
		CommunityAllocationId id = new CommunityAllocationId(UUID.randomUUID());

		when(communityAllocationRepository.findById(id)).thenReturn(Optional.of(mock(CommunityAllocation.class)));

		//when+then
		assertDoesNotThrow(() -> validator.validateDelete(id));
	}

	@Test
	void shouldNotPassDeleteForNonExistingId() {
		//given
		CommunityAllocationId id = new CommunityAllocationId(UUID.randomUUID());

		when(communityAllocationRepository.findById(id)).thenReturn(Optional.empty());

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateDelete(id));
	}

}