/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.community_allocation;

import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationCreatedEvent;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.community_allocation.CommunityAllocationRemovedEvent;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
import io.imunity.furms.domain.community_allocation.CommunityAllocationUpdatedEvent;
import io.imunity.furms.domain.resource_credits.ResourceCreditId;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityAllocationServiceImplTest {
	@Mock
	private CommunityAllocationServiceValidator validator;
	@Mock
	private CommunityAllocationRepository communityAllocationRepository;
	@Mock
	private ApplicationEventPublisher publisher;
	@Mock
	private ProjectAllocationService projectAllocationService;
	@Mock
	private ResourceUsageRepository resourceUsageRepository;

	private CommunityAllocationServiceImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		service = new CommunityAllocationServiceImpl(communityAllocationRepository, validator,
				publisher, projectAllocationService, resourceUsageRepository);
		orderVerifier = inOrder(communityAllocationRepository, publisher);
	}

	@Test
	void shouldReturnCommunityAllocation() {
		//given
		CommunityAllocationId id = new CommunityAllocationId(UUID.randomUUID());
		when(communityAllocationRepository.findById(id)).thenReturn(Optional.of(CommunityAllocation.builder()
			.id(id)
			.name("name")
			.build())
		);

		//when
		Optional<CommunityAllocation> byId = service.findById(id);

		//then
		assertThat(byId).isPresent();
		assertThat(byId.get().id).isEqualTo(id);
	}

	@Test
	void shouldReturnCommunityAllocationByCommunityId() {
		//given
		CommunityId id = new CommunityId(UUID.randomUUID());
		when(communityAllocationRepository.findAllByCommunityId(id)).thenReturn(Set.of(CommunityAllocation.builder()
				.id(UUID.randomUUID().toString())
				.name("name")
				.build())
		);

		//when
		Set<CommunityAllocation> byId = service.findAllByCommunityId(id);

		//then
		assertThat(byId).hasSize(1);
	}

	@Test
	void shouldNotReturnNotExisting() {
		//when
		Optional<CommunityAllocation> otherId = service.findById(new CommunityAllocationId(UUID.randomUUID()));

		//then
		assertThat(otherId).isEmpty();
	}

	@Test
	void shouldReturnAllCommunityAllocationsExistingInRepository() {
		//given
		when(communityAllocationRepository.findAll()).thenReturn(Set.of(
			CommunityAllocation.builder().id(new CommunityAllocationId(UUID.randomUUID())).name("name").build(),
			CommunityAllocation.builder().id(new CommunityAllocationId(UUID.randomUUID())).name("name2").build()));

		//when
		Set<CommunityAllocation> allCommunityAllocations = service.findAll();

		//then
		assertThat(allCommunityAllocations).hasSize(2);
	}

	@Test
	void shouldReturnAllCommunityAllocationsIncludedFullyDistributed() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		CommunityAllocationId communityAllocationId = new CommunityAllocationId(UUID.randomUUID());
		CommunityAllocationId communityAllocationId1 = new CommunityAllocationId(UUID.randomUUID());
		CommunityAllocationId communityAllocationId2 = new CommunityAllocationId(UUID.randomUUID());
		when(communityAllocationRepository.findAllByCommunityIdAndNameOrSiteNameWithRelatedObjects(communityId, ""))
				.thenReturn(Set.of(
					CommunityAllocationResolved.builder().id(communityAllocationId).communityId(communityId).name("name").build(),
					CommunityAllocationResolved.builder().id(communityAllocationId1).communityId(communityId).name("name_fullyDistributed").build(),
					CommunityAllocationResolved.builder().id(communityAllocationId2).communityId(communityId).name("name2").build()));
		when(projectAllocationService.getAvailableAmount(communityId, communityAllocationId)).thenReturn(BigDecimal.ONE);
		when(projectAllocationService.getAvailableAmount(communityId, communityAllocationId1)).thenReturn(BigDecimal.ZERO);
		when(projectAllocationService.getAvailableAmount(communityId, communityAllocationId2)).thenReturn(BigDecimal.ONE);

		//when
		final Set<CommunityAllocationResolved> all = service.findAllWithRelatedObjects(communityId, "", true, true);

		//then
		assertThat(all).hasSize(3);
	}

	@Test
	void shouldReturnAllCommunityAllocationsNotIncludedFullyDistributed() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		CommunityAllocationId communityAllocationId = new CommunityAllocationId(UUID.randomUUID());
		CommunityAllocationId communityAllocationId1 = new CommunityAllocationId(UUID.randomUUID());
		CommunityAllocationId communityAllocationId2 = new CommunityAllocationId(UUID.randomUUID());
		when(communityAllocationRepository.findAllByCommunityIdAndNameOrSiteNameWithRelatedObjects(communityId, ""))
				.thenReturn(Set.of(
					CommunityAllocationResolved.builder().id(communityAllocationId).communityId(communityId).name("name").build(),
					CommunityAllocationResolved.builder().id(communityAllocationId1).communityId(communityId).name("name_fullyDistributed").build(),
					CommunityAllocationResolved.builder().id(communityAllocationId2).communityId(communityId).name("name2").build()));
		when(projectAllocationService.getAvailableAmount(communityId, communityAllocationId)).thenReturn(BigDecimal.ONE);
		when(projectAllocationService.getAvailableAmount(communityId, communityAllocationId1)).thenReturn(BigDecimal.ZERO);
		when(projectAllocationService.getAvailableAmount(communityId, communityAllocationId2)).thenReturn(BigDecimal.ONE);

		//when
		final Set<CommunityAllocationResolved> all = service.findAllWithRelatedObjects(communityId, "", false, true);

		//then
		assertThat(all).hasSize(2);
		assertThat(all.stream().noneMatch(credit -> credit.id.equals(communityAllocationId1))).isTrue();
	}

	@Test
	void shouldAllowToCreateCommunityAllocation() {
		//given
		CommunityAllocationId communityAllocationId = new CommunityAllocationId(UUID.randomUUID());
		CommunityAllocation request = CommunityAllocation.builder()
			.id(communityAllocationId)
			.communityId(UUID.randomUUID().toString())
			.resourceCreditId(new ResourceCreditId(UUID.randomUUID()))
			.name("name")
			.amount(new BigDecimal(1))
			.build();
		when(communityAllocationRepository.findById(communityAllocationId)).thenReturn(Optional.of(request));
		when(communityAllocationRepository.create(request)).thenReturn(communityAllocationId);

		//when
		service.create(request);

		orderVerifier.verify(communityAllocationRepository).create(eq(request));
		orderVerifier.verify(publisher).publishEvent(eq(new CommunityAllocationCreatedEvent(request)));
	}

	@Test
	void shouldAllowToUpdateCommunityAllocation() {
		//given
		CommunityAllocationId communityAllocationId = new CommunityAllocationId(UUID.randomUUID());
		CommunityAllocation request = CommunityAllocation.builder()
			.id(communityAllocationId)
			.communityId(UUID.randomUUID().toString())
			.resourceCreditId(UUID.randomUUID().toString())
			.name("name")
			.amount(new BigDecimal(1))
			.build();
		when(communityAllocationRepository.findById(communityAllocationId)).thenReturn(Optional.of(request));

		//when
		service.update(request);

		orderVerifier.verify(communityAllocationRepository).update(eq(request));
		orderVerifier.verify(publisher).publishEvent(eq(new CommunityAllocationUpdatedEvent(request, request)));
	}

	@Test
	void shouldAllowToDeleteCommunityAllocation() {
		//given
		CommunityAllocationId id = new CommunityAllocationId(UUID.randomUUID());
		CommunityAllocation request = CommunityAllocation.builder()
			.id(id)
			.communityId(UUID.randomUUID().toString())
			.resourceCreditId(UUID.randomUUID().toString())
			.name("name")
			.amount(new BigDecimal(1))
			.build();
		when(communityAllocationRepository.findById(id)).thenReturn(Optional.of(request));

		//when
		service.delete(id);

		orderVerifier.verify(communityAllocationRepository).delete(eq(id));
		orderVerifier.verify(publisher).publishEvent(eq(new CommunityAllocationRemovedEvent(request)));
	}
}