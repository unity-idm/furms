/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.services;

import io.imunity.furms.domain.services.CreateServiceEvent;
import io.imunity.furms.domain.services.RemoveServiceEvent;
import io.imunity.furms.domain.services.Service;
import io.imunity.furms.domain.services.UpdateServiceEvent;
import io.imunity.furms.spi.services.ServiceRepository;
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

class ServiceServiceImplTest {
	@Mock
	private ServiceRepository serviceRepository;
	@Mock
	private SiteRepository siteRepository;
	@Mock
	private ApplicationEventPublisher publisher;

	private ServiceServiceImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		ServiceServiceValidator validator = new ServiceServiceValidator(serviceRepository, siteRepository);
		service = new ServiceServiceImpl(serviceRepository, validator, publisher);
		orderVerifier = inOrder(serviceRepository, publisher);
	}

	@Test
	void shouldReturnServiceIfExistsInRepository() {
		//given
		String id = "id";
		when(serviceRepository.findById(id)).thenReturn(Optional.of(Service.builder()
			.id(id)
			.name("userFacingName")
			.build())
		);

		//when
		Optional<Service> byId = service.findById(id);
		Optional<Service> otherId = service.findById("otherId");

		//then
		assertThat(byId).isPresent();
		assertThat(byId.get().id).isEqualTo(id);
		assertThat(otherId).isEmpty();
	}

	@Test
	void shouldReturnAllServicesIfExistsInRepository() {
		//given
		when(serviceRepository.findAll("1")).thenReturn(Set.of(
			Service.builder().id("id1").name("userFacingName").build(),
			Service.builder().id("id2").name("userFacingName2").build()));

		//when
		Set<Service> allServices = service.findAll("1");

		//then
		assertThat(allServices).hasSize(2);
	}

	@Test
	void shouldAllowToCreateService() {
		//given
		Service request = Service.builder()
			.id("id")
			.siteId("id")
			.name("userFacingName")
			.build();

		when(siteRepository.exists(request.id)).thenReturn(true);
		when(serviceRepository.isUniqueName(request.name)).thenReturn(true);
		when(serviceRepository.create(request)).thenReturn("id");

		//when
		service.create(request);

		orderVerifier.verify(serviceRepository).create(eq(request));
		orderVerifier.verify(publisher).publishEvent(eq(new CreateServiceEvent("id")));
	}

	@Test
	void shouldNotAllowToCreateServiceDueToNonUniqueName() {
		//given
		Service request = Service.builder()
			.id("id")
			.name("name")
			.build();
		when(serviceRepository.isUniqueName(request.name)).thenReturn(false);

		//when
		assertThrows(IllegalArgumentException.class, () -> service.create(request));
		orderVerifier.verify(serviceRepository, times(0)).create(eq(request));
		orderVerifier.verify(publisher, times(0)).publishEvent(eq(new CreateServiceEvent("id")));
	}

	@Test
	void shouldAllowToUpdateService() {
		//given
		Service request = Service.builder()
			.id("id")
			.siteId("id")
			.name("userFacingName")
			.build();

		when(siteRepository.exists(request.id)).thenReturn(true);
		when(serviceRepository.exists(request.id)).thenReturn(true);
		when(serviceRepository.isUniqueName(request.name)).thenReturn(true);
		when(serviceRepository.findById(request.id)).thenReturn(Optional.of(request));

		//when
		service.update(request);

		orderVerifier.verify(serviceRepository).update(eq(request));
		orderVerifier.verify(publisher).publishEvent(eq(new UpdateServiceEvent("id")));
	}

	@Test
	void shouldAllowToDeleteService() {
		//given
		String id = "id";
		when(serviceRepository.exists(id)).thenReturn(true);

		//when
		service.delete(id);

		orderVerifier.verify(serviceRepository).delete(eq(id));
		orderVerifier.verify(publisher).publishEvent(eq(new RemoveServiceEvent("id")));
	}

	@Test
	void shouldNotAllowToDeleteServiceDueToServiceNotExists() {
		//given
		String id = "id";
		when(serviceRepository.exists(id)).thenReturn(false);

		//when
		assertThrows(IllegalArgumentException.class, () -> service.delete(id));
		orderVerifier.verify(serviceRepository, times(0)).delete(eq(id));
		orderVerifier.verify(publisher, times(0)).publishEvent(eq(new UpdateServiceEvent("id")));
	}

}