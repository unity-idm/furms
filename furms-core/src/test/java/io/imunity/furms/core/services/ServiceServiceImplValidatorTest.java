/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.services;

import io.imunity.furms.domain.services.Service;
import io.imunity.furms.spi.services.ServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceServiceImplValidatorTest {
	@Mock
	private ServiceRepository serviceRepository;
	@Mock
	private SiteRepository siteRepository;

	@InjectMocks
	private ServiceServiceValidator validator;

	@Test
	void shouldPassCreateForUniqueName() {
		//given
		Service service = Service.builder()
			.siteId("id")
			.name("name")
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(serviceRepository.isUniqueName(any())).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNonUniqueName() {
		//given
		Service service = Service.builder()
			.siteId("id")
			.name("name")
			.description("description")
			.build();

		when(serviceRepository.isUniqueName(any())).thenReturn(false);
		when(siteRepository.exists(service.siteId)).thenReturn(true);
		Service secondService = Service.builder().name("a").build();
		when(serviceRepository.findById(any())).thenReturn(Optional.of(secondService));

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNonExistingSiteId() {
		//given
		Service service = Service.builder()
			.siteId("id")
			.name("name")
			.description("description")
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNullSiteId() {
		//given
		Service service = Service.builder()
			.name("name")
			.description("description")
			.build();

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldPassUpdateForUniqueName() {
		//given
		final Service service = Service.builder()
			.id("id")
			.siteId("id")
			.name("name")
			.description("description")
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(serviceRepository.exists(service.id)).thenReturn(true);
		when(serviceRepository.isUniqueName(any())).thenReturn(true);
		when(serviceRepository.findById(any())).thenReturn(Optional.of(service));

		//when+then
		assertDoesNotThrow(() -> validator.validateUpdate(service));
	}

	@Test
	void shouldNotPassUpdateForNonExistingObject() {
		//given
		Service community = Service.builder()
			.id("id")
			.siteId("id")
			.name("name")
			.description("description")
			.build();

		when(serviceRepository.exists(community.id)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(community));
	}

	@Test
	void shouldNotPassUpdateForNonUniqueName() {
		//given
		Service community = Service.builder()
			.id("id")
			.siteId("id")
			.name("name")
			.description("description")
			.build();
		Service secondService = Service.builder()
			.siteId("id")
			.name("a")
			.build();

		when(serviceRepository.exists(community.id)).thenReturn(true);
		when(serviceRepository.isUniqueName(any())).thenReturn(false);
		when(siteRepository.exists(any())).thenReturn(true);
		when(serviceRepository.findById(any())).thenReturn(Optional.of(secondService));
		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(community));
	}

	@Test
	void shouldPassDeleteForExistingId() {
		//given
		final String id = "id";

		when(serviceRepository.exists(id)).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateDelete(id));
	}

	@Test
	void shouldNotPassDeleteForNonExistingId() {
		//given
		final String id = "id";

		when(serviceRepository.exists(id)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateDelete(id));
	}

}