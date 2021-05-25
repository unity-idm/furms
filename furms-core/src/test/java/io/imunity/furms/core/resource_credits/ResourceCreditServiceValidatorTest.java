/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_credits;

import io.imunity.furms.api.validation.exceptions.CreditUpdateBelowDistributedAmountException;
import io.imunity.furms.api.validation.exceptions.ResourceCreditHasAllocationException;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceCreditServiceValidatorTest {
	@Mock
	private ResourceTypeRepository resourceTypeRepository;
	@Mock
	private SiteRepository siteRepository;
	@Mock
	private ResourceCreditRepository resourceCreditRepository;
	@Mock
	private CommunityAllocationRepository communityAllocationRepository;

	@InjectMocks
	private ResourceCreditServiceValidator validator;

	@Test
	void shouldPassCreateForUniqueName() {
		//given
		ResourceCredit service = ResourceCredit.builder()
			.siteId("id")
			.resourceTypeId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(resourceTypeRepository.exists(service.resourceTypeId)).thenReturn(true);
		when(resourceCreditRepository.isNamePresent(any(), any())).thenReturn(false);

		//when+then
		assertDoesNotThrow(() -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNullAmount() {
		//given
		ResourceCredit service = ResourceCredit.builder()
			.siteId("id")
			.resourceTypeId("id")
			.name("name")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(resourceTypeRepository.exists(service.resourceTypeId)).thenReturn(true);
		when(resourceCreditRepository.isNamePresent(any(), any())).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForWrongDates() {
		//given
		ResourceCredit service = ResourceCredit.builder()
			.siteId("id")
			.resourceTypeId("id")
			.name("name")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now().minusDays(10))
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(resourceTypeRepository.exists(service.resourceTypeId)).thenReturn(true);
		when(resourceCreditRepository.isNamePresent(any(), any())).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNonUniqueName() {
		//given
		ResourceCredit service = ResourceCredit.builder()
			.siteId("id")
			.resourceTypeId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(resourceTypeRepository.exists(service.resourceTypeId)).thenReturn(true);
		when(resourceCreditRepository.isNamePresent(any(), any())).thenReturn(true);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNonExistingSiteId() {
		//given
		ResourceCredit service = ResourceCredit.builder()
			.siteId("id")
			.name("name")
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNonExistingResourceTypeId() {
		//given
		ResourceCredit service = ResourceCredit.builder()
			.siteId("id")
			.resourceTypeId("id")
			.name("name")
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(resourceTypeRepository.exists(service.siteId)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNullSiteId() {
		//given
		ResourceCredit service = ResourceCredit.builder()
			.name("name")
			.build();

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNullResourceTypeId() {
		//given
		ResourceCredit service = ResourceCredit.builder()
			.siteId("id")
			.name("name")
			.build();

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldPassUpdateForUniqueName() {
		//given
		final ResourceCredit credit = ResourceCredit.builder()
			.id("id")
			.siteId("id")
			.resourceTypeId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();

		when(siteRepository.exists(credit.siteId)).thenReturn(true);
		when(resourceTypeRepository.exists(credit.resourceTypeId)).thenReturn(true);
		when(resourceCreditRepository.findById(any())).thenReturn(Optional.of(credit));
		when(communityAllocationRepository.getAvailableAmount(credit.id)).thenReturn(BigDecimal.valueOf(1));

		//when+then
		assertDoesNotThrow(() -> validator.validateUpdate(credit));
	}

	@Test
	void shouldNotPassUpdateForNonExistingObject() {
		//given
		ResourceCredit community = ResourceCredit.builder()
			.id("id")
			.siteId("id")
			.resourceTypeId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();

		when(resourceCreditRepository.findById(community.id)).thenReturn(Optional.empty());

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(community));
	}

	@Test
	void shouldForbidToDecreaseAmountBelowAlreadyDistributed() {
		//given
		ResourceCredit original = ResourceCredit.builder()
				.id("id")
				.siteId("sid")
				.resourceTypeId("rid")
				.name("name")
				.amount(new BigDecimal(10))
				.utcStartTime(LocalDateTime.now())
				.utcEndTime(LocalDateTime.now())
				.build();
		ResourceCredit updated = ResourceCredit.builder()
				.id("id")
				.siteId("sid")
				.resourceTypeId("rid")
				.name("name")
				.amount(new BigDecimal(5))
				.utcStartTime(LocalDateTime.now())
				.utcEndTime(LocalDateTime.now())
				.build();

		when(communityAllocationRepository.getAvailableAmount(original.id)).thenReturn(BigDecimal.valueOf(4));
		when(siteRepository.exists(any())).thenReturn(true);
		when(resourceCreditRepository.findById(any())).thenReturn(Optional.of(original));
		when(resourceTypeRepository.exists(any())).thenReturn(true);

		//when+then
		assertThrows(CreditUpdateBelowDistributedAmountException.class, () -> validator.validateUpdate(updated));
	}
	
	@Test
	void shouldNotPassUpdateForNonUniqueName() {
		//given
		ResourceCredit resourceCredit = ResourceCredit.builder()
			.id("id")
			.siteId("id")
			.resourceTypeId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();

		ResourceCredit resourceCredit2 = ResourceCredit.builder()
			.id("id")
			.siteId("id")
			.resourceTypeId("id")
			.name("name2")
			.amount(new BigDecimal(2))
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();

		when(siteRepository.exists(any())).thenReturn(true);
		when(resourceCreditRepository.findById(any())).thenReturn(Optional.of(resourceCredit2));
		when(resourceTypeRepository.exists(any())).thenReturn(true);
		when(resourceCreditRepository.isNamePresent(any(), any())).thenReturn(true);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(resourceCredit));
	}

	@Test
	void shouldPassDeleteForExistingId() {
		//given
		String id = "id";

		when(resourceCreditRepository.findById(id)).thenReturn(Optional.of(mock(ResourceCredit.class)));

		//when+then
		assertDoesNotThrow(() -> validator.validateDelete(id));
	}

	@Test
	void shouldNotPassDeleteForExistingResourceCredits() {
		//given
		String id = "id";

		when(resourceCreditRepository.findById(id)).thenReturn(Optional.of(mock(ResourceCredit.class)));
		when(communityAllocationRepository.existsByResourceCreditId(id)).thenReturn(true);

		//when+then
		assertThrows(ResourceCreditHasAllocationException.class, () -> validator.validateDelete(id));
	}

	@Test
	void shouldNotPassDeleteForNonExistingId() {
		//given
		String id = "id";

		when(resourceCreditRepository.findById(id)).thenReturn(Optional.empty());

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateDelete(id));
	}

}