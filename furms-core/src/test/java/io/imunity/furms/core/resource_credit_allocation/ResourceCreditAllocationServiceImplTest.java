/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_credit_allocation;

import io.imunity.furms.domain.resource_credit_allocation.CreateResourceCreditAllocationEvent;
import io.imunity.furms.domain.resource_credit_allocation.RemoveResourceCreditAllocationEvent;
import io.imunity.furms.domain.resource_credit_allocation.ResourceCreditAllocation;
import io.imunity.furms.domain.resource_credit_allocation.UpdateResourceCreditAllocationEvent;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.resource_credit_allocation.ResourceCreditAllocationRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ResourceCreditAllocationServiceImplTest {
	@Mock
	private ResourceCreditRepository resourceCreditRepository;
	@Mock
	private SiteRepository siteRepository;
	@Mock
	private CommunityRepository communityRepository;
	@Mock
	private ResourceTypeRepository resourceTypeRepository;
	@Mock
	private ResourceCreditAllocationRepository resourceCreditAllocationRepository;
	@Mock
	private ApplicationEventPublisher publisher;

	private ResourceCreditAllocationServiceImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		ResourceCreditAllocationServiceValidator validator = new ResourceCreditAllocationServiceValidator(resourceCreditAllocationRepository, resourceCreditRepository, resourceTypeRepository, communityRepository, siteRepository);
		service = new ResourceCreditAllocationServiceImpl(resourceCreditAllocationRepository, validator, publisher);
		orderVerifier = inOrder(resourceCreditAllocationRepository, publisher);
	}

	@Test
	void shouldReturnResourceCredit() {
		//given
		String id = "id";
		when(resourceCreditAllocationRepository.findById(id)).thenReturn(Optional.of(ResourceCreditAllocation.builder()
			.id(id)
			.name("name")
			.build())
		);

		//when
		Optional<ResourceCreditAllocation> byId = service.findById(id);

		//then
		assertThat(byId).isPresent();
		assertThat(byId.get().id).isEqualTo(id);
	}

	@Test
	void shouldNotReturnResourceCreditAllocation() {
		//given
		String id = "id";
		when(resourceCreditAllocationRepository.findById(id)).thenReturn(Optional.of(ResourceCreditAllocation.builder()
			.id(id)
			.name("name")
			.build())
		);

		//when
		Optional<ResourceCreditAllocation> otherId = service.findById("otherId");

		//then
		assertThat(otherId).isEmpty();
	}

	@Test
	void shouldReturnAllResourceCreditAllocationsIfExistsInRepository() {
		//given
		when(resourceCreditAllocationRepository.findAll()).thenReturn(Set.of(
			ResourceCreditAllocation.builder().id("id1").name("name").build(),
			ResourceCreditAllocation.builder().id("id2").name("name2").build()));

		//when
		Set<ResourceCreditAllocation> allResourceCreditAllocations = service.findAll();

		//then
		assertThat(allResourceCreditAllocations).hasSize(2);
	}

	@Test
	void shouldAllowToCreateResourceCreditAllocation() {
		//given
		ResourceCreditAllocation request = ResourceCreditAllocation.builder()
			.id("id")
			.siteId("id")
			.communityId("id")
			.resourceTypeId("id")
			.resourceCreditId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(siteRepository.exists(request.siteId)).thenReturn(true);
		when(communityRepository.exists(request.communityId)).thenReturn(true);
		when(resourceTypeRepository.exists(request.resourceTypeId)).thenReturn(true);
		when(resourceCreditRepository.exists(request.resourceCreditId)).thenReturn(true);
		when(resourceCreditAllocationRepository.isUniqueName(request.name)).thenReturn(true);
		when(resourceCreditAllocationRepository.create(request)).thenReturn("id");

		//when
		service.create(request);

		orderVerifier.verify(resourceCreditAllocationRepository).create(eq(request));
		orderVerifier.verify(publisher).publishEvent(eq(new CreateResourceCreditAllocationEvent("id")));
	}

	@Test
	void shouldNotAllowToCreateResourceCreditAllocationDueToNonUniqueName() {
		//given
		ResourceCreditAllocation request = ResourceCreditAllocation.builder()
			.id("id")
			.name("name")
			.build();
		when(resourceCreditAllocationRepository.isUniqueName(request.name)).thenReturn(false);

		//when
		assertThrows(IllegalArgumentException.class, () -> service.create(request));
		orderVerifier.verify(resourceCreditAllocationRepository, times(0)).create(eq(request));
		orderVerifier.verify(publisher, times(0)).publishEvent(eq(new CreateResourceCreditAllocationEvent("id")));
	}

	@Test
	void shouldAllowToUpdateResourceCreditAllocation() {
		//given
		ResourceCreditAllocation request = ResourceCreditAllocation.builder()
			.id("id")
			.siteId("id")
			.communityId("id")
			.resourceTypeId("id")
			.resourceCreditId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(siteRepository.exists(request.siteId)).thenReturn(true);
		when(communityRepository.exists(request.communityId)).thenReturn(true);
		when(resourceTypeRepository.exists(request.resourceTypeId)).thenReturn(true);
		when(resourceCreditRepository.exists(request.resourceCreditId)).thenReturn(true);
		when(resourceCreditAllocationRepository.exists(request.id)).thenReturn(true);
		when(resourceCreditAllocationRepository.isUniqueName(request.name)).thenReturn(true);
		when(resourceCreditAllocationRepository.findById(request.id)).thenReturn(Optional.of(request));

		//when
		service.update(request);

		orderVerifier.verify(resourceCreditAllocationRepository).update(eq(request));
		orderVerifier.verify(publisher).publishEvent(eq(new UpdateResourceCreditAllocationEvent("id")));
	}

	@Test
	void shouldAllowToDeleteResourceCreditAllocation() {
		//given
		String id = "id";
		when(resourceCreditAllocationRepository.exists(id)).thenReturn(true);

		//when
		service.delete(id);

		orderVerifier.verify(resourceCreditAllocationRepository).delete(eq(id));
		orderVerifier.verify(publisher).publishEvent(eq(new RemoveResourceCreditAllocationEvent("id")));
	}

	@Test
	void shouldNotAllowToDeleteResourceCreditAllocationDueToResourceCreditAllocationNotExists() {
		//given
		String id = "id";
		when(resourceCreditAllocationRepository.exists(id)).thenReturn(false);

		//when
		assertThrows(IllegalArgumentException.class, () -> service.delete(id));
		orderVerifier.verify(resourceCreditAllocationRepository, times(0)).delete(eq(id));
		orderVerifier.verify(publisher, times(0)).publishEvent(eq(new UpdateResourceCreditAllocationEvent("id")));
	}

}