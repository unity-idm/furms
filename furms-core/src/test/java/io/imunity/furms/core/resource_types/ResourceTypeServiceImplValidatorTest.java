/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_types;

import io.imunity.furms.api.validation.exceptions.ResourceTypeHasResourceCreditsRemoveValidationError;
import io.imunity.furms.api.validation.exceptions.TypeAndUnitAreInconsistentValidationError;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.domain.services.InfraServiceId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.domain.resource_types.ResourceMeasureType;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.services.InfraServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

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
		SiteId siteId = new SiteId(UUID.randomUUID());
		InfraServiceId infraServiceId = new InfraServiceId(UUID.randomUUID());
		ResourceType service = ResourceType.builder()
			.siteId(siteId)
			.serviceId(infraServiceId)
			.name("name")
			.type(ResourceMeasureType.DATA)
			.unit(ResourceMeasureUnit.GB)
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(serviceRepository.exists(service.serviceId)).thenReturn(true);
		when(resourceTypeRepository.isNamePresent(any(), any())).thenReturn(false);

		//when+then
		assertDoesNotThrow(() -> validator.validateCreate(service));
	}


	@Test
	void shouldNotPassCreateForNullType() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		InfraServiceId infraServiceId = new InfraServiceId(UUID.randomUUID());
		ResourceType service = ResourceType.builder()
			.siteId(siteId)
			.serviceId(infraServiceId)
			.name("name")
			.unit(ResourceMeasureUnit.MEGA)
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(serviceRepository.exists(service.serviceId)).thenReturn(true);
		when(resourceTypeRepository.isNamePresent(any(), any())).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNullUnit() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		InfraServiceId infraServiceId = new InfraServiceId(UUID.randomUUID());
		ResourceType service = ResourceType.builder()
			.siteId(siteId)
			.serviceId(infraServiceId)
			.name("name")
			.unit(ResourceMeasureUnit.MEGA)
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(serviceRepository.exists(service.serviceId)).thenReturn(true);
		when(resourceTypeRepository.isNamePresent(any(), any())).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForInconsistentTypeAndUnit() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		InfraServiceId infraServiceId = new InfraServiceId(UUID.randomUUID());
		ResourceType service = ResourceType.builder()
			.siteId(siteId)
			.serviceId(infraServiceId)
			.name("name")
			.type(ResourceMeasureType.DATA)
			.unit(ResourceMeasureUnit.MEGA)
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(serviceRepository.exists(service.serviceId)).thenReturn(true);
		when(resourceTypeRepository.isNamePresent(any(), any())).thenReturn(false);

		//when+then
		assertThrows(TypeAndUnitAreInconsistentValidationError.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNonUniqueName() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		InfraServiceId infraServiceId = new InfraServiceId(UUID.randomUUID());
		ResourceType service = ResourceType.builder()
			.siteId(siteId)
			.serviceId(infraServiceId)
			.name("name")
			.type(ResourceMeasureType.DATA)
			.unit(ResourceMeasureUnit.GB)
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(serviceRepository.exists(service.serviceId)).thenReturn(true);
		when(resourceTypeRepository.isNamePresent(any(), any())).thenReturn(true);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNonExistingSiteId() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		ResourceType service = ResourceType.builder()
			.siteId(siteId)
			.name("name")
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldNotPassCreateForNonExistingServiceId() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		InfraServiceId infraServiceId = new InfraServiceId(UUID.randomUUID());
		ResourceType service = ResourceType.builder()
			.siteId(siteId)
			.serviceId(infraServiceId)
			.name("name")
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(serviceRepository.exists(service.serviceId)).thenReturn(false);

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
		SiteId siteId = new SiteId(UUID.randomUUID());
		ResourceType service = ResourceType.builder()
			.siteId(siteId)
			.name("name")
			.build();

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(service));
	}

	@Test
	void shouldPassUpdateWithSameName() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		InfraServiceId infraServiceId = new InfraServiceId(UUID.randomUUID());
		ResourceTypeId resourceTypeId = new ResourceTypeId(UUID.randomUUID());
		final ResourceType service = ResourceType.builder()
			.id(resourceTypeId)
			.siteId(siteId)
			.serviceId(infraServiceId)
			.name("name")
			.type(ResourceMeasureType.DATA)
			.unit(ResourceMeasureUnit.GB)
			.build();

		when(siteRepository.exists(service.siteId)).thenReturn(true);
		when(serviceRepository.exists(service.serviceId)).thenReturn(true);
		when(resourceTypeRepository.exists(service.id)).thenReturn(true);
		when(resourceTypeRepository.findById(any())).thenReturn(Optional.of(service));

		//when+then
		assertDoesNotThrow(() -> validator.validateUpdate(service));
	}

	@Test
	void shouldNotPassUpdateForNonExistingObject() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		InfraServiceId infraServiceId = new InfraServiceId(UUID.randomUUID());
		ResourceTypeId resourceTypeId = new ResourceTypeId(UUID.randomUUID());
		ResourceType community = ResourceType.builder()
			.id(resourceTypeId)
			.siteId(siteId)
			.serviceId(infraServiceId)
			.name("name")
			.type(ResourceMeasureType.DATA)
			.unit(ResourceMeasureUnit.GB)
			.build();

		when(resourceTypeRepository.exists(community.id)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(community));
	}

	@Test
	void shouldNotPassUpdateForNonUniqueName() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		InfraServiceId infraServiceId = new InfraServiceId(UUID.randomUUID());
		ResourceTypeId resourceTypeId = new ResourceTypeId(UUID.randomUUID());
		ResourceType resourceType = ResourceType.builder()
			.id(resourceTypeId)
			.siteId(siteId)
			.serviceId(infraServiceId)
			.name("name")
			.type(ResourceMeasureType.DATA)
			.unit(ResourceMeasureUnit.GB)
			.build();

		ResourceType resourceType2 = ResourceType.builder()
			.id(resourceTypeId)
			.siteId(siteId)
			.serviceId(infraServiceId)
			.name("name2")
			.build();

		when(resourceTypeRepository.exists(any())).thenReturn(true);
		when(siteRepository.exists(any())).thenReturn(true);
		when(resourceTypeRepository.findById(any())).thenReturn(Optional.of(resourceType2));
		when(serviceRepository.exists(any())).thenReturn(true);
		when(resourceTypeRepository.isNamePresent(any(), any())).thenReturn(true);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(resourceType));
	}

	@Test
	void shouldPassDeleteForExistingId() {
		//given
		ResourceTypeId resourceTypeId = new ResourceTypeId(UUID.randomUUID());

		when(resourceTypeRepository.exists(resourceTypeId)).thenReturn(true);

		//when+then
		assertDoesNotThrow(() -> validator.validateDelete(resourceTypeId));
	}

	@Test
	void shouldNotPassDeleteForExistingResourceCredits() {
		//given
		ResourceTypeId resourceTypeId = new ResourceTypeId(UUID.randomUUID());

		when(resourceTypeRepository.exists(resourceTypeId)).thenReturn(true);
		when(resourceCreditRepository.existsByResourceTypeId(resourceTypeId)).thenReturn(true);

		//when+then
		assertThrows(ResourceTypeHasResourceCreditsRemoveValidationError.class, () -> validator.validateDelete(resourceTypeId));
	}

	@Test
	void shouldNotPassDeleteForNonExistingId() {
		//given
		ResourceTypeId resourceTypeId = new ResourceTypeId(UUID.randomUUID());

		when(resourceTypeRepository.exists(resourceTypeId)).thenReturn(false);

		//when+then
		assertThrows(IllegalArgumentException.class, () -> validator.validateDelete(resourceTypeId));
	}

}