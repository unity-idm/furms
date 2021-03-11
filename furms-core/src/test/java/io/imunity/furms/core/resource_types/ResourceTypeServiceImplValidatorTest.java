/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_types;

import io.imunity.furms.api.validation.exceptions.ResourceTypeHasResourceCreditsRemoveValidationError;
import io.imunity.furms.api.validation.exceptions.TypeAndUnitAreInconsistentValidationError;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.resource_types.Type;
import io.imunity.furms.domain.resource_types.Unit;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.services.InfraServiceRepository;
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
class ResourceTypeServiceImplValidatorTest {
	@Mock
	private InfraServiceRepository serviceRepository;
	@Mock
	private SiteRepository siteRepository;
	@Mock
	private ResourceTypeRepository resourceTypeRepository;
	@Mock
	private ResourceCreditRepository resourceCreditRepository;

	@InjectMocks
	private ResourceTypeServiceValidator validator;

	@Test
	void shouldPassCreateForUniqueName() {
		//given
		ResourceType service = ResourceType.builder()
			.siteId("id")
			.serviceId("id")
			.name("name")
			.type(Type.DATA)
			.unit(Unit.DataUnit.GB)
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(serviceRepository.exists(service.serviceId)).thenReturn(true);
		when(resourceTypeRepository.isUniqueName(any())).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateCreate(service));
	}


	@Test
	void shouldNotPassCreateForNullType() {
		//given
		ResourceType service = ResourceType.builder()
			.siteId("id")
			.serviceId("id")
			.name("name")
			.unit(Unit.SiUnit.m)
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(serviceRepository.exists(service.serviceId)).thenReturn(true);
		when(resourceTypeRepository.isUniqueName(any())).thenReturn(true);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNullUnit() {
		//given
		ResourceType service = ResourceType.builder()
			.siteId("id")
			.serviceId("id")
			.name("name")
			.unit(Unit.SiUnit.m)
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(serviceRepository.exists(service.serviceId)).thenReturn(true);
		when(resourceTypeRepository.isUniqueName(any())).thenReturn(true);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForInconsistentTypeAndUnit() {
		//given
		ResourceType service = ResourceType.builder()
			.siteId("id")
			.serviceId("id")
			.name("name")
			.type(Type.DATA)
			.unit(Unit.SiUnit.m)
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(serviceRepository.exists(service.serviceId)).thenReturn(true);
		when(resourceTypeRepository.isUniqueName(any())).thenReturn(true);

		//when+then
		assertThrows(TypeAndUnitAreInconsistentValidationError.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNonUniqueName() {
		//given
		ResourceType service = ResourceType.builder()
			.siteId("id")
			.serviceId("id")
			.name("name")
			.type(Type.DATA)
			.unit(Unit.DataUnit.GB)
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(serviceRepository.exists(service.serviceId)).thenReturn(true);
		when(resourceTypeRepository.isUniqueName(any())).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNonExistingSiteId() {
		//given
		ResourceType service = ResourceType.builder()
			.siteId("id")
			.name("name")
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNonExistingServiceId() {
		//given
		ResourceType service = ResourceType.builder()
			.siteId("id")
			.serviceId("id")
			.name("name")
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(serviceRepository.exists(service.siteId)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNullSiteId() {
		//given
		ResourceType service = ResourceType.builder()
			.name("name")
			.build();

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNullServiceId() {
		//given
		ResourceType service = ResourceType.builder()
			.siteId("id")
			.name("name")
			.build();

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldPassUpdateForUniqueName() {
		//given
		final ResourceType service = ResourceType.builder()
			.id("id")
			.siteId("id")
			.serviceId("id")
			.name("name")
			.type(Type.DATA)
			.unit(Unit.DataUnit.GB)
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(serviceRepository.exists(service.serviceId)).thenReturn(true);
		when(resourceTypeRepository.exists(service.id)).thenReturn(true);
		when(resourceTypeRepository.isUniqueName(any())).thenReturn(true);
		when(resourceTypeRepository.findById(any())).thenReturn(Optional.of(service));

		//when+then
		assertDoesNotThrow(() -> validator.validateUpdate(service));
	}

	@Test
	void shouldNotPassUpdateForNonExistingObject() {
		//given
		ResourceType community = ResourceType.builder()
			.id("id")
			.siteId("id")
			.serviceId("id")
			.name("name")
			.type(Type.DATA)
			.unit(Unit.DataUnit.GB)
			.build();

		when(resourceTypeRepository.exists(community.id)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(community));
	}

	@Test
	void shouldNotPassUpdateForNonUniqueName() {
		//given
		ResourceType resourceType = ResourceType.builder()
			.id("id")
			.siteId("id")
			.serviceId("id")
			.name("name")
			.type(Type.DATA)
			.unit(Unit.DataUnit.GB)
			.build();

		ResourceType resourceType2 = ResourceType.builder()
			.id("id")
			.siteId("id")
			.serviceId("id")
			.name("name2")
			.build();

		when(resourceTypeRepository.exists(any())).thenReturn(true);
		when(siteRepository.exists(any())).thenReturn(true);
		when(resourceTypeRepository.findById(any())).thenReturn(Optional.of(resourceType2));
		when(serviceRepository.exists(any())).thenReturn(true);
		when(resourceTypeRepository.isUniqueName(any())).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(resourceType));
	}

	@Test
	void shouldPassDeleteForExistingId() {
		//given
		String id = "id";

		when(resourceTypeRepository.exists(id)).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateDelete(id));
	}

	@Test
	void shouldNotPassDeleteForExistingResourceCredits() {
		//given
		String id = "id";

		when(resourceTypeRepository.exists(id)).thenReturn(true);
		when(resourceCreditRepository.existsByResourceTypeId(id)).thenReturn(true);

		//when+then
		assertThrows(ResourceTypeHasResourceCreditsRemoveValidationError.class, () -> validator.validateDelete(id));
	}

	@Test
	void shouldNotPassDeleteForNonExistingId() {
		//given
		String id = "id";

		when(resourceTypeRepository.exists(id)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateDelete(id));
	}

}