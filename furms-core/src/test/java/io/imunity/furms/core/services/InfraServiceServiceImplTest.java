/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.services;

import io.imunity.furms.domain.services.CreateServiceEvent;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.services.RemoveServiceEvent;
import io.imunity.furms.domain.services.UpdateServiceEvent;
import io.imunity.furms.spi.services.InfraServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class InfraServiceServiceImplTest {
	@Mock
	private InfraServiceRepository infraServiceRepository;
	@Mock
	private SiteRepository siteRepository;
	@Mock
	private ApplicationEventPublisher publisher;

	private InfraServiceServiceImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		InfraServiceServiceValidator validator = new InfraServiceServiceValidator(infraServiceRepository, siteRepository);
		service = new InfraServiceServiceImpl(infraServiceRepository, validator, publisher);
		orderVerifier = inOrder(infraServiceRepository, publisher);
	}

	@Test
	void shouldReturnInfraService() {
		//given
		String id = "id";
		when(infraServiceRepository.findById(id)).thenReturn(Optional.of(InfraService.builder()
			.id(id)
			.name("userFacingName")
			.build())
		);

		//when
		Optional<InfraService> byId = service.findById(id);

		//then
		assertThat(byId).isPresent();
		assertThat(byId.get().id).isEqualTo(id);
	}

	@Test
	void shouldNotReturnInfraService() {
		//given
		String id = "id";
		when(infraServiceRepository.findById(id)).thenReturn(Optional.of(InfraService.builder()
			.id(id)
			.name("userFacingName")
			.build())
		);

		//when
		Optional<InfraService> otherId = service.findById("otherId");

		//then
		assertThat(otherId).isEmpty();
	}

	@Test
	void shouldReturnAllInfraServicesIfExistsInRepository() {
		//given
		when(infraServiceRepository.findAll("1")).thenReturn(Set.of(
			InfraService.builder().id("id1").name("userFacingName").build(),
			InfraService.builder().id("id2").name("userFacingName2").build()));

		//when
		Set<InfraService> allInfraServices = service.findAll("1");

		//then
		assertThat(allInfraServices).hasSize(2);
	}

	@Test
	void shouldAllowToCreateInfraService() {
		//given
		InfraService request = InfraService.builder()
			.id("id")
			.siteId("id")
			.name("userFacingName")
			.build();

		when(siteRepository.exists(request.id)).thenReturn(true);
		when(infraServiceRepository.isUniqueName(request.name)).thenReturn(true);
		when(infraServiceRepository.create(request)).thenReturn("id");

		//when
		service.create(request);

		orderVerifier.verify(infraServiceRepository).create(eq(request));
		orderVerifier.verify(publisher).publishEvent(eq(new CreateServiceEvent("id")));
	}

	@Test
	void shouldNotAllowToCreateInfraServiceDueToNonUniqueName() {
		//given
		InfraService request = InfraService.builder()
			.id("id")
			.name("name")
			.build();
		when(infraServiceRepository.isUniqueName(request.name)).thenReturn(false);

		//when
		assertThrows(IllegalArgumentException.class, () -> service.create(request));
		orderVerifier.verify(infraServiceRepository, times(0)).create(eq(request));
		orderVerifier.verify(publisher, times(0)).publishEvent(eq(new CreateServiceEvent("id")));
	}

	@Test
	void shouldAllowToUpdateInfraService() {
		//given
		InfraService request = InfraService.builder()
			.id("id")
			.siteId("id")
			.name("userFacingName")
			.build();

		when(siteRepository.exists(request.id)).thenReturn(true);
		when(infraServiceRepository.exists(request.id)).thenReturn(true);
		when(infraServiceRepository.isUniqueName(request.name)).thenReturn(true);
		when(infraServiceRepository.findById(request.id)).thenReturn(Optional.of(request));

		//when
		service.update(request);

		orderVerifier.verify(infraServiceRepository).update(eq(request));
		orderVerifier.verify(publisher).publishEvent(eq(new UpdateServiceEvent("id")));
	}

	@Test
	void shouldAllowToDeleteInfraService() {
		//given
		String id = "id";
		when(infraServiceRepository.exists(id)).thenReturn(true);

		//when
		service.delete(id);

		orderVerifier.verify(infraServiceRepository).delete(eq(id));
		orderVerifier.verify(publisher).publishEvent(eq(new RemoveServiceEvent("id")));
	}

	@Test
	void shouldNotAllowToDeleteInfraServiceDueToInfraServiceNotExists() {
		//given
		String id = "id";
		when(infraServiceRepository.exists(id)).thenReturn(false);

		//when
		assertThrows(IllegalArgumentException.class, () -> service.delete(id));
		orderVerifier.verify(infraServiceRepository, times(0)).delete(eq(id));
		orderVerifier.verify(publisher, times(0)).publishEvent(eq(new UpdateServiceEvent("id")));
	}

}