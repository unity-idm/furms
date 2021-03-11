/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_credits;

import io.imunity.furms.domain.resource_credits.ResourceCredit;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceCreditServiceImplValidatorTest {
	@Mock
	private ResourceTypeRepository resourceTypeRepository;
	@Mock
	private SiteRepository siteRepository;
	@Mock
	private ResourceCreditRepository resourceCreditRepository;

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
			.startTime(LocalDateTime.now())
			.endTime(LocalDateTime.now())
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(resourceTypeRepository.exists(service.resourceTypeId)).thenReturn(true);
		when(resourceCreditRepository.isUniqueName(any())).thenReturn(true);

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
			.startTime(LocalDateTime.now())
			.endTime(LocalDateTime.now())
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(resourceTypeRepository.exists(service.resourceTypeId)).thenReturn(true);
		when(resourceCreditRepository.isUniqueName(any())).thenReturn(true);

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
			.startTime(LocalDateTime.now())
			.endTime(LocalDateTime.now().minusDays(10))
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(resourceTypeRepository.exists(service.resourceTypeId)).thenReturn(true);
		when(resourceCreditRepository.isUniqueName(any())).thenReturn(true);

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
			.startTime(LocalDateTime.now())
			.endTime(LocalDateTime.now())
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(resourceTypeRepository.exists(service.resourceTypeId)).thenReturn(true);
		when(resourceCreditRepository.isUniqueName(any())).thenReturn(false);

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
		final ResourceCredit service = ResourceCredit.builder()
			.id("id")
			.siteId("id")
			.resourceTypeId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.startTime(LocalDateTime.now())
			.endTime(LocalDateTime.now())
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(resourceTypeRepository.exists(service.resourceTypeId)).thenReturn(true);
		when(resourceCreditRepository.exists(service.id)).thenReturn(true);
		when(resourceCreditRepository.isUniqueName(any())).thenReturn(true);
		when(resourceCreditRepository.findById(any())).thenReturn(Optional.of(service));

		//when+then
		assertDoesNotThrow(() -> validator.validateUpdate(service));
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
			.startTime(LocalDateTime.now())
			.endTime(LocalDateTime.now())
			.build();

		when(resourceCreditRepository.exists(community.id)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(community));
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
			.startTime(LocalDateTime.now())
			.endTime(LocalDateTime.now())
			.build();

		ResourceCredit resourceCredit2 = ResourceCredit.builder()
			.id("id")
			.siteId("id")
			.resourceTypeId("id")
			.name("name2")
			.amount(new BigDecimal(2))
			.startTime(LocalDateTime.now())
			.endTime(LocalDateTime.now())
			.build();

		when(resourceCreditRepository.exists(any())).thenReturn(true);
		when(siteRepository.exists(any())).thenReturn(true);
		when(resourceCreditRepository.findById(any())).thenReturn(Optional.of(resourceCredit2));
		when(resourceTypeRepository.exists(any())).thenReturn(true);
		when(resourceCreditRepository.isUniqueName(any())).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(resourceCredit));
	}

	@Test
	void shouldPassDeleteForExistingId() {
		//given
		String id = "id";

		when(resourceCreditRepository.exists(id)).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateDelete(id));
	}

	@Test
	void shouldNotPassDeleteForNonExistingId() {
		//given
		String id = "id";

		when(resourceCreditRepository.exists(id)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateDelete(id));
	}

}