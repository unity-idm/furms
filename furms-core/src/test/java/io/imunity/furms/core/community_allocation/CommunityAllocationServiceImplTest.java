/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.community_allocation;

import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
import io.imunity.furms.domain.community_allocation.CreateCommunityAllocationEvent;
import io.imunity.furms.domain.community_allocation.RemoveCommunityAllocationEvent;
import io.imunity.furms.domain.community_allocation.UpdateCommunityAllocationEvent;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

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
		MockitoAnnotations.initMocks(this);
		service = new CommunityAllocationServiceImpl(communityAllocationRepository, validator,
				publisher, projectAllocationService, resourceUsageRepository);
		orderVerifier = inOrder(communityAllocationRepository, publisher);
	}

	@Test
	void shouldReturnCommunityAllocation() {
		//given
		String id = "id";
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
	void shouldNotReturnNotExisting() {
		//when
		Optional<CommunityAllocation> otherId = service.findById("otherId");

		//then
		assertThat(otherId).isEmpty();
	}

	@Test
	void shouldReturnAllCommunityAllocationsExistingInRepository() {
		//given
		when(communityAllocationRepository.findAll()).thenReturn(Set.of(
			CommunityAllocation.builder().id("id1").name("name").build(),
			CommunityAllocation.builder().id("id2").name("name2").build()));

		//when
		Set<CommunityAllocation> allCommunityAllocations = service.findAll();

		//then
		assertThat(allCommunityAllocations).hasSize(2);
	}

	@Test
	void shouldReturnAllCommunityAllocationsIncludedFullyDistributed() {
		//given
		when(communityAllocationRepository.findAllByCommunityIdAndNameOrSiteNameWithRelatedObjects("id1", ""))
				.thenReturn(Set.of(
					CommunityAllocationResolved.builder().id("id1").communityId("id1").name("name").build(),
					CommunityAllocationResolved.builder().id("id2").communityId("id1").name("name_fullyDistributed").build(),
					CommunityAllocationResolved.builder().id("id3").communityId("id1").name("name2").build()));
		when(projectAllocationService.getAvailableAmount("id1", "id1")).thenReturn(BigDecimal.ONE);
		when(projectAllocationService.getAvailableAmount("id1", "id2")).thenReturn(BigDecimal.ZERO);
		when(projectAllocationService.getAvailableAmount("id1", "id3")).thenReturn(BigDecimal.ONE);

		//when
		final Set<CommunityAllocationResolved> all = service.findAllWithRelatedObjects("id1", "", true, true);

		//then
		assertThat(all).hasSize(3);
	}

	@Test
	void shouldReturnAllCommunityAllocationsNotIncludedFullyDistributed() {
		//given
		when(communityAllocationRepository.findAllByCommunityIdAndNameOrSiteNameWithRelatedObjects("id1", ""))
				.thenReturn(Set.of(
					CommunityAllocationResolved.builder().id("id1").communityId("id1").name("name").build(),
					CommunityAllocationResolved.builder().id("id2").communityId("id1").name("name_fullyDistributed").build(),
					CommunityAllocationResolved.builder().id("id3").communityId("id1").name("name2").build()));
		when(projectAllocationService.getAvailableAmount("id1", "id1")).thenReturn(BigDecimal.ONE);
		when(projectAllocationService.getAvailableAmount("id1", "id2")).thenReturn(BigDecimal.ZERO);
		when(projectAllocationService.getAvailableAmount("id1", "id3")).thenReturn(BigDecimal.ONE);

		//when
		final Set<CommunityAllocationResolved> all = service.findAllWithRelatedObjects("id1", "", false, true);

		//then
		assertThat(all).hasSize(2);
		assertThat(all.stream()
				.noneMatch(credit -> credit.id.equals("id2")));
	}

	@Test
	void shouldAllowToCreateCommunityAllocation() {
		//given
		CommunityAllocation request = CommunityAllocation.builder()
			.id("id")
			.communityId("id")
			.resourceCreditId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		//when
		service.create(request);

		orderVerifier.verify(communityAllocationRepository).create(eq(request));
		orderVerifier.verify(publisher).publishEvent(eq(new CreateCommunityAllocationEvent("id")));
	}

	@Test
	void shouldAllowToUpdateCommunityAllocation() {
		//given
		CommunityAllocation request = CommunityAllocation.builder()
			.id("id")
			.communityId("id")
			.resourceCreditId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		//when
		service.update(request);

		orderVerifier.verify(communityAllocationRepository).update(eq(request));
		orderVerifier.verify(publisher).publishEvent(eq(new UpdateCommunityAllocationEvent("id")));
	}

	@Test
	void shouldAllowToDeleteCommunityAllocation() {
		//given
		String id = "id";
		when(communityAllocationRepository.exists(id)).thenReturn(true);

		//when
		service.delete(id);

		orderVerifier.verify(communityAllocationRepository).delete(eq(id));
		orderVerifier.verify(publisher).publishEvent(eq(new RemoveCommunityAllocationEvent("id")));
	}
}