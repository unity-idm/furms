/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_credit_allocation;

import io.imunity.furms.domain.resource_credit_allocation.ResourceCreditAllocation;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.resource_credit_allocation.ResourceCreditAllocationRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.sites.SiteRepository;
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
class ResourceCreditAllocationServiceImplValidatorTest {
	@Mock
	private ResourceTypeRepository resourceTypeRepository;
	@Mock
	private SiteRepository siteRepository;
	@Mock
	private CommunityRepository communityRepository;
	@Mock
	private ResourceCreditRepository resourceCreditRepository;
	@Mock
	private ResourceCreditAllocationRepository resourceCreditAllocationRepository;

	@InjectMocks
	private ResourceCreditAllocationServiceValidator validator;

	@Test
	void shouldPassCreateForUniqueName() {
		//given
		ResourceCreditAllocation service = ResourceCreditAllocation.builder()
			.siteId("id")
			.communityId("id")
			.resourceTypeId("id")
			.resourceCreditId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(communityRepository.exists(service.communityId)).thenReturn(true);
		when(resourceTypeRepository.exists(service.resourceTypeId)).thenReturn(true);
		when(resourceCreditRepository.exists(service.resourceCreditId)).thenReturn(true);
		when(resourceCreditAllocationRepository.isUniqueName(any())).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNullAmount() {
		//given
		ResourceCreditAllocation service = ResourceCreditAllocation.builder()
			.siteId("id")
			.communityId("id")
			.resourceTypeId("id")
			.resourceCreditId("id")
			.name("name")
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(communityRepository.exists(service.communityId)).thenReturn(true);
		when(resourceTypeRepository.exists(service.resourceTypeId)).thenReturn(true);
		when(resourceCreditRepository.exists(service.resourceCreditId)).thenReturn(true);
		when(resourceCreditAllocationRepository.isUniqueName(any())).thenReturn(true);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNonUniqueName() {
		//given
		ResourceCreditAllocation service = ResourceCreditAllocation.builder()
			.siteId("id")
			.communityId("id")
			.resourceTypeId("id")
			.resourceCreditId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(communityRepository.exists(service.communityId)).thenReturn(true);
		when(resourceTypeRepository.exists(service.resourceTypeId)).thenReturn(true);
		when(resourceCreditRepository.exists(service.resourceCreditId)).thenReturn(true);
		when(resourceCreditAllocationRepository.isUniqueName(any())).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNonExistingSiteId() {
		//given
		ResourceCreditAllocation service = ResourceCreditAllocation.builder()
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
		ResourceCreditAllocation service = ResourceCreditAllocation.builder()
			.siteId("id")
			.communityId("id")
			.resourceTypeId("id")
			.name("name")
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(communityRepository.exists(service.communityId)).thenReturn(true);
		when(resourceTypeRepository.exists(service.resourceTypeId)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNullSiteId() {
		//given
		ResourceCreditAllocation service = ResourceCreditAllocation.builder()
			.name("name")
			.build();

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNullResourceTypeId() {
		//given
		ResourceCreditAllocation service = ResourceCreditAllocation.builder()
			.siteId("id")
			.name("name")
			.build();

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldPassUpdateForUniqueName() {
		//given
		final ResourceCreditAllocation service = ResourceCreditAllocation.builder()
			.id("id")
			.siteId("id")
			.communityId("id")
			.resourceTypeId("id")
			.resourceCreditId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(communityRepository.exists(service.communityId)).thenReturn(true);
		when(resourceTypeRepository.exists(service.resourceTypeId)).thenReturn(true);
		when(resourceCreditRepository.exists(service.resourceCreditId)).thenReturn(true);
		when(resourceCreditAllocationRepository.exists(service.id)).thenReturn(true);
		when(resourceCreditAllocationRepository.isUniqueName(any())).thenReturn(true);
		when(resourceCreditAllocationRepository.findById(any())).thenReturn(Optional.of(service));

		//when+then
		assertDoesNotThrow(() -> validator.validateUpdate(service));
	}

	@Test
	void shouldNotPassUpdateForNonExistingObject() {
		//given
		ResourceCreditAllocation community = ResourceCreditAllocation.builder()
			.id("id")
			.siteId("id")
			.communityId("id")
			.resourceTypeId("id")
			.resourceCreditId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(resourceCreditAllocationRepository.exists(community.id)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(community));
	}

	@Test
	void shouldNotPassUpdateForNonUniqueName() {
		//given
		ResourceCreditAllocation resourceCredit = ResourceCreditAllocation.builder()
			.id("id")
			.siteId("id")
			.communityId("id")
			.resourceTypeId("id")
			.resourceCreditId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		ResourceCreditAllocation resourceCredit2 = ResourceCreditAllocation.builder()
			.id("id")
			.siteId("id")
			.communityId("id")
			.resourceTypeId("id")
			.resourceCreditId("id")
			.name("name2")
			.amount(new BigDecimal(2))
			.build();

		when(resourceCreditAllocationRepository.exists(any())).thenReturn(true);
		when(resourceCreditAllocationRepository.findById(any())).thenReturn(Optional.of(resourceCredit2));
		when(siteRepository.exists(any())).thenReturn(true);
		when(communityRepository.exists(any())).thenReturn(true);
		when(resourceTypeRepository.exists(any())).thenReturn(true);
		when(resourceCreditRepository.exists(any())).thenReturn(true);
		when(resourceCreditAllocationRepository.isUniqueName(any())).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(resourceCredit));
	}

	@Test
	void shouldPassDeleteForExistingId() {
		//given
		String id = "id";

		when(resourceCreditAllocationRepository.exists(id)).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateDelete(id));
	}

	@Test
	void shouldNotPassDeleteForNonExistingId() {
		//given
		String id = "id";

		when(resourceCreditAllocationRepository.exists(id)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateDelete(id));
	}

}