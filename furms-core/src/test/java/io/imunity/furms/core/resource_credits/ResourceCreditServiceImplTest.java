/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_credits;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.resource_types.ResourceTypeService;
import io.imunity.furms.core.community_allocation.CommunityAllocationServiceHelper;
import io.imunity.furms.domain.resource_credits.ResourceCreditCreatedEvent;
import io.imunity.furms.domain.resource_credits.ResourceCreditId;
import io.imunity.furms.domain.resource_credits.ResourceCreditRemovedEvent;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_credits.ResourceCreditWithAllocations;
import io.imunity.furms.domain.resource_credits.ResourceCreditUpdatedEvent;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.domain.resource_usage.ResourceUsageByCredit;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceCreditServiceImplTest {
	@Mock
	private ResourceCreditRepository resourceCreditRepository;
	@Mock
	private SiteRepository siteRepository;
	@Mock
	private ResourceTypeRepository resourceTypeRepository;
	@Mock
	private CommunityAllocationRepository communityAllocationRepository;
	@Mock
	private CommunityAllocationServiceHelper communityAllocationServiceHelper;
	@Mock
	private ApplicationEventPublisher publisher;
	@Mock
	private AuthzService authzService;
	@Mock
	private ResourceTypeService resourceTypeService;
	@Mock
	private ResourceUsageRepository resourceUsageRepository;

	private ResourceCreditServiceImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		ResourceCreditServiceValidator validator = new ResourceCreditServiceValidator(communityAllocationRepository,
				resourceCreditRepository, resourceTypeRepository, siteRepository);
		service = new ResourceCreditServiceImpl(resourceCreditRepository, validator, publisher,
			communityAllocationServiceHelper, authzService, resourceTypeService, resourceUsageRepository);
		orderVerifier = inOrder(resourceCreditRepository, publisher);
	}

	@Test
	void shouldReturnResourceCredit() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		ResourceCreditId resourceCreditId = new ResourceCreditId(UUID.randomUUID());
		when(resourceCreditRepository.findById(resourceCreditId)).thenReturn(Optional.of(ResourceCredit.builder()
			.id(resourceCreditId)
			.name("name")
			.build())
		);
		when(resourceUsageRepository.findResourceUsagesSumsBySiteId(siteId)).thenReturn(new ResourceUsageByCredit(Map.of()));
		when(resourceTypeService.findById(any(), any())).thenReturn(Optional.of(ResourceType.builder().build()));

		//when
		Optional<ResourceCreditWithAllocations> byId = service.findWithAllocationsByIdAndSiteId(resourceCreditId, siteId);

		//then
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(resourceCreditId);
	}

	@Test
	void shouldNotReturnResourceCredit() {
		//when
		SiteId siteId = new SiteId(UUID.randomUUID());
		ResourceCreditId resourceCreditId = new ResourceCreditId(UUID.randomUUID());
		Optional<ResourceCreditWithAllocations> otherId = service.findWithAllocationsByIdAndSiteId(resourceCreditId, siteId);

		//then
		assertThat(otherId).isEmpty();
	}

	@Test
	void shouldReturnAllResourceCreditsIfExistsInRepository() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		ResourceCreditId resourceCreditId = new ResourceCreditId(UUID.randomUUID());
		ResourceCreditId resourceCreditId1 = new ResourceCreditId(UUID.randomUUID());

		when(resourceCreditRepository.findAll(siteId)).thenReturn(Set.of(
			ResourceCredit.builder().id(resourceCreditId).name("name").build(),
			ResourceCredit.builder().id(resourceCreditId1).name("name2").build()));
		when(resourceUsageRepository.findResourceUsagesSumsBySiteId(siteId)).thenReturn(new ResourceUsageByCredit(Map.of()));
		when(resourceTypeService.findById(any(), any())).thenReturn(Optional.of(ResourceType.builder().build()));

		//when
		Set<ResourceCreditWithAllocations> allResourceCredits = service.findAllWithAllocations(siteId);

		//then
		assertThat(allResourceCredits).hasSize(2);
	}

	@Test
	void shouldReturnResourceCreditsIncludedFullyDistributed() {
		//given
		ResourceCreditId resourceCreditId = new ResourceCreditId(UUID.randomUUID());
		ResourceCreditId resourceCreditId1 = new ResourceCreditId(UUID.randomUUID());
		ResourceCreditId resourceCreditId2 = new ResourceCreditId(UUID.randomUUID());

		when(resourceCreditRepository.findAllNotExpiredByNameOrSiteName("")).thenReturn(Set.of(
				ResourceCredit.builder().id(resourceCreditId).name("name").build(),
				ResourceCredit.builder().id(resourceCreditId1).name("name_fullyDistributed").build(),
				ResourceCredit.builder().id(resourceCreditId2).name("name2").build()));
		when(communityAllocationServiceHelper.getAvailableAmountForNew(resourceCreditId)).thenReturn(BigDecimal.ONE);
		when(communityAllocationServiceHelper.getAvailableAmountForNew(resourceCreditId1)).thenReturn(BigDecimal.ZERO);
		when(communityAllocationServiceHelper.getAvailableAmountForNew(resourceCreditId2)).thenReturn(BigDecimal.ONE);
		when(resourceTypeService.findById(any(), any())).thenReturn(Optional.of(ResourceType.builder().build()));

		//when
		final Set<ResourceCreditWithAllocations> all = service.findAllWithAllocations("", true, false);

		//then
		assertThat(all).hasSize(3);
	}

	@Test
	void shouldReturnResourceCredits() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		ResourceTypeId resourceTypeId = new ResourceTypeId(UUID.randomUUID());
		ResourceCreditId resourceCreditId = new ResourceCreditId(UUID.randomUUID());
		when(resourceCreditRepository.findAll(siteId)).thenReturn(Set.of(
			ResourceCredit.builder()
				.id(resourceCreditId)
				.name("name")
				.siteId(siteId)
				.resourceTypeId(resourceTypeId)
				.build()
		));
		when(resourceUsageRepository.findResourceUsagesSumsBySiteId(siteId)).thenReturn(
			new ResourceUsageByCredit(Map.of(resourceCreditId, BigDecimal.TEN))
		);
		when(communityAllocationServiceHelper.getAvailableAmountForNew(resourceCreditId)).thenReturn(BigDecimal.ONE);
		when(resourceTypeService.findById(resourceTypeId, siteId)).thenReturn(Optional.of(ResourceType.builder().build()));

		//when
		Set<ResourceCreditWithAllocations> all = service.findAllWithAllocations(siteId);

		//then
		assertThat(all).hasSize(1);
		ResourceCreditWithAllocations credit = all.iterator().next();
		assertThat(credit.getId()).isEqualTo(resourceCreditId);
		assertThat(credit.getRemaining()).isEqualTo(BigDecimal.ONE);
		assertThat(credit.getConsumed()).isEqualTo(BigDecimal.TEN);

	}

	@Test
	void shouldReturnResourceCreditsNotIncludedFullyDistributed() {
		//given
		ResourceCreditId resourceCreditId = new ResourceCreditId(UUID.randomUUID());
		ResourceCreditId resourceCreditId1 = new ResourceCreditId(UUID.randomUUID());
		ResourceCreditId resourceCreditId2 = new ResourceCreditId(UUID.randomUUID());

		when(resourceCreditRepository.findAllNotExpiredByNameOrSiteName("")).thenReturn(Set.of(
				ResourceCredit.builder().id(resourceCreditId).name("name").build(),
				ResourceCredit.builder().id(resourceCreditId1).name("name_fullyDistributed").build(),
				ResourceCredit.builder().id(resourceCreditId2).name("name2").build()));
		when(communityAllocationServiceHelper.getAvailableAmountForNew(resourceCreditId)).thenReturn(BigDecimal.ONE);
		when(communityAllocationServiceHelper.getAvailableAmountForNew(resourceCreditId1)).thenReturn(BigDecimal.ZERO);
		when(communityAllocationServiceHelper.getAvailableAmountForNew(resourceCreditId2)).thenReturn(BigDecimal.ONE);
		when(resourceTypeService.findById(any(), any())).thenReturn(Optional.of(ResourceType.builder().build()));

		//when
		final Set<ResourceCreditWithAllocations> all = service.findAllWithAllocations("", false, false);

		//then
		assertThat(all).hasSize(2);
		assertThat(all.stream().noneMatch(credit -> credit.getId().equals(resourceCreditId1))).isTrue();
	}

	@Test
	void shouldAllowToCreateResourceCredit() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		ResourceTypeId resourceTypeId = new ResourceTypeId(UUID.randomUUID());
		ResourceCreditId resourceCreditId = new ResourceCreditId(UUID.randomUUID());
		ResourceCredit request = ResourceCredit.builder()
			.id(resourceCreditId)
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
			.name("name")
			.amount(new BigDecimal(1))
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();

		when(siteRepository.exists(request.siteId)).thenReturn(true);
		when(resourceTypeRepository.exists(request.resourceTypeId)).thenReturn(true);
		when(resourceCreditRepository.isNamePresent(request.name, request.siteId)).thenReturn(false);
		when(resourceCreditRepository.create(request)).thenReturn(resourceCreditId);
		when(resourceCreditRepository.findById(resourceCreditId)).thenReturn(Optional.of(request));

		//when
		service.create(request);

		orderVerifier.verify(resourceCreditRepository).create(eq(request));
		orderVerifier.verify(publisher).publishEvent(eq(new ResourceCreditCreatedEvent(new PersistentId("userId"), request)));
	}

	@Test
	void shouldNotAllowToCreateResourceCreditDueToNonUniqueName() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		ResourceTypeId resourceTypeId = new ResourceTypeId(UUID.randomUUID());
		ResourceCredit request = ResourceCredit.builder()
			.id(new ResourceCreditId(UUID.randomUUID()))
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
			.name("name")
			.build();
		when(siteRepository.exists(siteId)).thenReturn(true);
		when(resourceTypeRepository.exists(resourceTypeId)).thenReturn(true);
		when(resourceCreditRepository.isNamePresent(request.name, request.siteId)).thenReturn(true);

		//when
		assertThrows(IllegalArgumentException.class, () -> service.create(request));
		orderVerifier.verify(resourceCreditRepository, times(0)).create(eq(request));
		orderVerifier.verify(publisher, times(0)).publishEvent(eq(new ResourceCreditCreatedEvent(new PersistentId("use"), request)));
	}

	@Test
	void shouldAllowToUpdateResourceCredit() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		ResourceTypeId resourceTypeId = new ResourceTypeId(UUID.randomUUID());
		ResourceCreditId resourceCreditId = new ResourceCreditId(UUID.randomUUID());
		ResourceCredit request = ResourceCredit.builder()
			.id(resourceCreditId)
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
			.name("name")
			.amount(new BigDecimal(1))
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();

		when(siteRepository.exists(request.siteId)).thenReturn(true);
		when(communityAllocationRepository.getAvailableAmount(request.id)).thenReturn(BigDecimal.ZERO);
		when(resourceTypeRepository.exists(request.resourceTypeId)).thenReturn(true);
		when(resourceCreditRepository.findById(request.id)).thenReturn(Optional.of(request));

		//when
		service.update(request);

		orderVerifier.verify(resourceCreditRepository).update(eq(request));
		orderVerifier.verify(publisher).publishEvent(eq(new ResourceCreditUpdatedEvent(request, request)));
	}

	@Test
	void shouldAllowToDeleteResourceCredit() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		ResourceCreditId resourceCreditId = new ResourceCreditId(UUID.randomUUID());
		ResourceCredit mock = mock(ResourceCredit.class);
		when(resourceCreditRepository.findById(resourceCreditId)).thenReturn(Optional.of(mock));

		//when
		service.delete(resourceCreditId, siteId);

		orderVerifier.verify(resourceCreditRepository).delete(eq(resourceCreditId));
		orderVerifier.verify(publisher).publishEvent(eq(new ResourceCreditRemovedEvent(mock)));
	}

	@Test
	void shouldNotAllowToDeleteResourceCreditDueToResourceCreditNotExists() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		ResourceCreditId resourceCreditId = new ResourceCreditId(UUID.randomUUID());

		//when
		assertThrows(IllegalArgumentException.class, () -> service.delete(resourceCreditId, siteId));
		orderVerifier.verify(resourceCreditRepository, times(0)).delete(eq(resourceCreditId));
		orderVerifier.verify(publisher, times(0)).publishEvent(any());
	}

}