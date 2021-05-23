/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_credits;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.community_allocation.CommunityAllocationService;
import io.imunity.furms.domain.resource_credits.CreateResourceCreditEvent;
import io.imunity.furms.domain.resource_credits.RemoveResourceCreditEvent;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_credits.ResourceCreditWithAllocations;
import io.imunity.furms.domain.resource_credits.UpdateResourceCreditEvent;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.sites.SiteRepository;

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
	private CommunityAllocationService communityAllocationService;
	@Mock
	private ApplicationEventPublisher publisher;
	@Mock
	private AuthzService authzService;

	private ResourceCreditServiceImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		ResourceCreditServiceValidator validator = new ResourceCreditServiceValidator(communityAllocationRepository, 
				resourceCreditRepository, resourceTypeRepository, siteRepository);
		service = new ResourceCreditServiceImpl(resourceCreditRepository, validator, publisher, 
				communityAllocationService, authzService);
		orderVerifier = inOrder(resourceCreditRepository, publisher);
	}

	@Test
	void shouldReturnResourceCredit() {
		//given
		String id = "id";
		when(resourceCreditRepository.findById(id)).thenReturn(Optional.of(ResourceCredit.builder()
			.id(id)
			.name("name")
			.build())
		);

		//when
		Optional<ResourceCredit> byId = service.findById(id, "");

		//then
		assertThat(byId).isPresent();
		assertThat(byId.get().id).isEqualTo(id);
	}

	@Test
	void shouldNotReturnResourceCredit() {
		//given
		String id = "id";
		when(resourceCreditRepository.findById(id)).thenReturn(Optional.of(ResourceCredit.builder()
			.id(id)
			.name("name")
			.build())
		);

		//when
		Optional<ResourceCredit> otherId = service.findById("otherId", "");

		//then
		assertThat(otherId).isEmpty();
	}

	@Test
	void shouldReturnAllResourceCreditsIfExistsInRepository() {
		//given
		when(resourceCreditRepository.findAll("1")).thenReturn(Set.of(
			ResourceCredit.builder().id("id1").name("name").build(),
			ResourceCredit.builder().id("id2").name("name2").build()));

		//when
		Set<ResourceCredit> allResourceCredits = service.findAll("1");

		//then
		assertThat(allResourceCredits).hasSize(2);
	}

	@Test
	void shouldReturnResourceCreditsIncludedFullyDistributed() {
		//given
		when(resourceCreditRepository.findAllByNameOrSiteNameWithoutExpired("")).thenReturn(Set.of(
				ResourceCredit.builder().id("id1").name("name").build(),
				ResourceCredit.builder().id("id2").name("name_fullyDistributed").build(),
				ResourceCredit.builder().id("id3").name("name2").build()));
		when(communityAllocationService.getAvailableAmount("id1")).thenReturn(BigDecimal.ONE);
		when(communityAllocationService.getAvailableAmount("id2")).thenReturn(BigDecimal.ZERO);
		when(communityAllocationService.getAvailableAmount("id3")).thenReturn(BigDecimal.ONE);

		//when
		final Set<ResourceCreditWithAllocations> all = service.findAllWithAllocations("", true, false);

		//then
		assertThat(all).hasSize(3);
	}

	@Test
	void shouldReturnResourceCreditsNotIncludedFullyDistributed() {
		//given
		when(resourceCreditRepository.findAllByNameOrSiteNameWithoutExpired("")).thenReturn(Set.of(
				ResourceCredit.builder().id("id1").name("name").build(),
				ResourceCredit.builder().id("id2").name("name_fullyDistributed").build(),
				ResourceCredit.builder().id("id3").name("name2").build()));
		when(communityAllocationService.getAvailableAmount("id1")).thenReturn(BigDecimal.ONE);
		when(communityAllocationService.getAvailableAmount("id2")).thenReturn(BigDecimal.ZERO);
		when(communityAllocationService.getAvailableAmount("id3")).thenReturn(BigDecimal.ONE);

		//when
		final Set<ResourceCreditWithAllocations> all = service.findAllWithAllocations("", false, false);

		//then
		assertThat(all).hasSize(2);
		assertThat(all.stream()
					.noneMatch(credit -> credit.getId().equals("id2")));
	}

	@Test
	void shouldAllowToCreateResourceCredit() {
		//given
		ResourceCredit request = ResourceCredit.builder()
			.id("id")
			.siteId("id")
			.resourceTypeId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();

		when(siteRepository.exists(request.siteId)).thenReturn(true);
		when(resourceTypeRepository.exists(request.resourceTypeId)).thenReturn(true);
		when(resourceCreditRepository.isNamePresent(request.name, request.siteId)).thenReturn(false);
		when(resourceCreditRepository.create(request)).thenReturn("id");

		//when
		service.create(request);

		orderVerifier.verify(resourceCreditRepository).create(eq(request));
		orderVerifier.verify(publisher).publishEvent(eq(new CreateResourceCreditEvent("id", new PersistentId("userId"))));
	}

	@Test
	void shouldNotAllowToCreateResourceCreditDueToNonUniqueName() {
		//given
		ResourceCredit request = ResourceCredit.builder()
			.id("id")
			.siteId("siteId")
			.name("name")
			.build();
		when(resourceCreditRepository.isNamePresent(request.name, request.siteId)).thenReturn(true);
		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("use"));

		//when
		assertThrows(IllegalArgumentException.class, () -> service.create(request));
		orderVerifier.verify(resourceCreditRepository, times(0)).create(eq(request));
		orderVerifier.verify(publisher, times(0)).publishEvent(eq(new CreateResourceCreditEvent("id", new PersistentId("use"))));
	}

	@Test
	void shouldAllowToUpdateResourceCredit() {
		//given
		ResourceCredit request = ResourceCredit.builder()
			.id("id")
			.siteId("id")
			.resourceTypeId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();

		when(siteRepository.exists(request.siteId)).thenReturn(true);
		when(resourceTypeRepository.exists(request.resourceTypeId)).thenReturn(true);
		when(resourceCreditRepository.exists(request.id)).thenReturn(true);
		when(resourceCreditRepository.isNamePresent(request.name, request.siteId)).thenReturn(true);
		when(resourceCreditRepository.findById(request.id)).thenReturn(Optional.of(request));

		//when
		service.update(request);

		orderVerifier.verify(resourceCreditRepository).update(eq(request));
		orderVerifier.verify(publisher).publishEvent(eq(new UpdateResourceCreditEvent("id")));
	}

	@Test
	void shouldAllowToDeleteResourceCredit() {
		//given
		String id = "id";
		when(resourceCreditRepository.findById(id)).thenReturn(Optional.of(mock(ResourceCredit.class)));

		//when
		service.delete(id, "");

		orderVerifier.verify(resourceCreditRepository).delete(eq(id));
		orderVerifier.verify(publisher).publishEvent(eq(new RemoveResourceCreditEvent("id")));
	}

	@Test
	void shouldNotAllowToDeleteResourceCreditDueToResourceCreditNotExists() {
		//given
		String id = "id";
		when(resourceCreditRepository.exists(id)).thenReturn(false);

		//when
		assertThrows(IllegalArgumentException.class, () -> service.delete(id, ""));
		orderVerifier.verify(resourceCreditRepository, times(0)).delete(eq(id));
		orderVerifier.verify(publisher, times(0)).publishEvent(eq(new UpdateResourceCreditEvent("id")));
	}

}