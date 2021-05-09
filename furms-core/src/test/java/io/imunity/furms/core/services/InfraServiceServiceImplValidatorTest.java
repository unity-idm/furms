/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.services;

import io.imunity.furms.api.validation.exceptions.InfraServiceHasIndirectlyResourceCreditsRemoveValidationError;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.services.InfraServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InfraServiceServiceImplValidatorTest {
	@Mock
	private InfraServiceRepository infraServiceRepository;
	@Mock
	private SiteRepository siteRepository;
	@Mock
	private ResourceTypeRepository resourceTypeRepository;
	@Mock
	private ResourceCreditRepository resourceCreditRepository;

	@InjectMocks
	private InfraServiceServiceValidator validator;

	@Test
	void shouldPassCreateForUniqueName() {
		//given
		InfraService infraService = InfraService.builder()
			.siteId("id")
			.name("name")
			.build();

		when(siteRepository.exists(infraService.siteId)).thenReturn(true);
		when(infraServiceRepository.isNameUsed(any(), any())).thenReturn(false);

		//when+then
		assertDoesNotThrow(() -> validator.validateCreate(infraService));
	}

	@Test
	void shouldNotPassCreateForNonUniqueName() {
		//given
		InfraService infraService = InfraService.builder()
			.siteId("id")
			.name("name")
			.description("description")
			.build();

		when(infraServiceRepository.isNameUsed(any(), any())).thenReturn(true);
		when(siteRepository.exists(infraService.siteId)).thenReturn(true);
		InfraService secondInfraService = InfraService.builder().name("a").build();
		when(infraServiceRepository.findById(any())).thenReturn(Optional.of(secondInfraService));

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(infraService));
	}

	@Test
	void shouldNotPassCreateForNonExistingSiteId() {
		//given
		InfraService infraService = InfraService.builder()
			.siteId("id")
			.name("name")
			.description("description")
			.build();

		when(siteRepository.exists(infraService.siteId)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(infraService));
	}

	@Test
	void shouldNotPassCreateForNullSiteId() {
		//given
		InfraService infraService = InfraService.builder()
			.name("name")
			.description("description")
			.build();

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(infraService));
	}

	@Test
	void shouldPassUpdateForUniqueName() {
		//given
		final InfraService infraService = InfraService.builder()
			.id("id")
			.siteId("id")
			.name("name")
			.description("description")
			.build();

		when(siteRepository.exists(infraService.siteId)).thenReturn(true);
		when(infraServiceRepository.exists(infraService.id)).thenReturn(true);
		when(infraServiceRepository.findById(any())).thenReturn(Optional.of(infraService));

		//when+then
		assertDoesNotThrow(() -> validator.validateUpdate(infraService));
	}

	@Test
	void shouldNotPassUpdateForNonExistingObject() {
		//given
		InfraService community = InfraService.builder()
			.id("id")
			.siteId("id")
			.name("name")
			.description("description")
			.build();

		when(infraServiceRepository.exists(community.id)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(community));
	}

	@Test
	void shouldNotPassUpdateForNonUniqueName() {
		//given
		InfraService community = InfraService.builder()
			.id("id")
			.siteId("id")
			.name("name")
			.description("description")
			.build();
		InfraService secondInfraService = InfraService.builder()
			.siteId("id")
			.name("a")
			.build();

		when(infraServiceRepository.exists(community.id)).thenReturn(true);
		when(infraServiceRepository.isNameUsed(any(), any())).thenReturn(true);
		when(siteRepository.exists(any())).thenReturn(true);
		when(infraServiceRepository.findById(any())).thenReturn(Optional.of(secondInfraService));
		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(community));
	}

	@Test
	void shouldPassDeleteForExistingId() {
		//given
		String id = "id";
		ResourceType resourceType = ResourceType.builder()
			.id("id")
			.siteId("id")
			.serviceId("id")
			.build();

		when(infraServiceRepository.exists(id)).thenReturn(true);
		when(resourceTypeRepository.findAllByInfraServiceId("id")).thenReturn(Set.of(resourceType));
		when(resourceCreditRepository.existsByResourceTypeIdIn(List.of("id"))).thenReturn(false);

		//when+then
		assertDoesNotThrow(() -> validator.validateDelete(id));
	}

	@Test
	void shouldNotPassDeleteForNonExistingId() {
		//given
		final String id = "id";

		when(infraServiceRepository.exists(id)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateDelete(id));
	}

	@Test
	void shouldNotPassDeleteForExistingResourceCredit() {
		//given
		String id = "id";
		ResourceType resourceType = ResourceType.builder()
			.id("id")
			.siteId("id")
			.serviceId("id")
			.build();

		when(infraServiceRepository.exists(id)).thenReturn(true);
		when(resourceTypeRepository.findAllByInfraServiceId("id")).thenReturn(Set.of(resourceType));
		when(resourceCreditRepository.existsByResourceTypeIdIn(List.of("id"))).thenReturn(true);

		//when+then
		assertThrows(InfraServiceHasIndirectlyResourceCreditsRemoveValidationError.class, () -> validator.validateDelete(id));
	}

}