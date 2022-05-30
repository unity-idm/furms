/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_types;

import io.imunity.furms.domain.resource_types.ResourceTypeCreatedEvent;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.domain.resource_types.ResourceTypeRemovedEvent;
import io.imunity.furms.domain.resource_types.ResourceMeasureType;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.resource_types.ResourceTypeUpdatedEvent;
import io.imunity.furms.domain.services.InfraServiceId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.services.InfraServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceTypeServiceImplTest {
	@Mock
	private ResourceTypeRepository resourceTypeRepository;
	@Mock
	private SiteRepository siteRepository;
	@Mock
	private InfraServiceRepository infraServiceRepository;
	@Mock
	private ResourceCreditRepository resourceCreditRepository;
	@Mock
	private ApplicationEventPublisher publisher;

	private ResourceTypeServiceImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		ResourceTypeServiceValidator validator = new ResourceTypeServiceValidator(resourceTypeRepository, resourceCreditRepository, infraServiceRepository, siteRepository);
		service = new ResourceTypeServiceImpl(resourceTypeRepository, validator, publisher);
		orderVerifier = inOrder(resourceTypeRepository, publisher);
	}

	@Test
	void shouldReturnResourceType() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		ResourceTypeId id = new ResourceTypeId(UUID.randomUUID());
		when(resourceTypeRepository.findById(id)).thenReturn(Optional.of(ResourceType.builder()
			.id(id)
			.name("name")
			.build())
		);

		//when
		Optional<ResourceType> byId = service.findById(id, siteId);

		//then
		assertThat(byId).isPresent();
		assertThat(byId.get().id).isEqualTo(id);
	}

	@Test
	void shouldNotReturnResourceType() {
		//when
		SiteId siteId = new SiteId(UUID.randomUUID());
		ResourceTypeId id = new ResourceTypeId(UUID.randomUUID());
		Optional<ResourceType> otherId = service.findById(id, siteId);

		//then
		assertThat(otherId).isEmpty();
	}

	@Test
	void shouldReturnAllResourceTypesIfExistsInRepository() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		ResourceTypeId resourceTypeId = new ResourceTypeId(UUID.randomUUID());
		ResourceTypeId resourceTypeId1 = new ResourceTypeId(UUID.randomUUID());
		when(resourceTypeRepository.findAllBySiteId(siteId)).thenReturn(Set.of(
			ResourceType.builder().id(resourceTypeId).name("name").build(),
			ResourceType.builder().id(resourceTypeId1).name("name2").build()));

		//when
		Set<ResourceType> allResourceTypes = service.findAll(siteId);

		//then
		assertThat(allResourceTypes).hasSize(2);
	}

	@Test
	void shouldAllowToCreateResourceType() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		ResourceTypeId resourceTypeId = new ResourceTypeId(UUID.randomUUID());
		InfraServiceId infraServiceId = new InfraServiceId(UUID.randomUUID());
		ResourceType request = ResourceType.builder()
			.id(resourceTypeId)
			.siteId(siteId)
			.serviceId(infraServiceId)
			.name("name")
			.type(ResourceMeasureType.DATA)
			.unit(ResourceMeasureUnit.GB)
			.build();

		when(siteRepository.exists(request.siteId)).thenReturn(true);
		when(infraServiceRepository.exists(request.serviceId)).thenReturn(true);
		when(resourceTypeRepository.findById(resourceTypeId)).thenReturn(Optional.of(request));
		when(resourceTypeRepository.create(request)).thenReturn(resourceTypeId);

		//when
		service.create(request);

		orderVerifier.verify(resourceTypeRepository).create(eq(request));
		orderVerifier.verify(publisher).publishEvent(eq(new ResourceTypeCreatedEvent(request)));
	}

	@Test
	void shouldNotAllowToCreateResourceTypeDueToNonUniqueName() {
		//given
		ResourceTypeId resourceTypeId = new ResourceTypeId(UUID.randomUUID());
		ResourceType request = ResourceType.builder()
			.id(resourceTypeId)
			.name("name")
			.build();

		//when
		assertThrows(IllegalArgumentException.class, () -> service.create(request));
		orderVerifier.verify(resourceTypeRepository, times(0)).create(eq(request));
		orderVerifier.verify(publisher, times(0)).publishEvent(any());
	}

	@Test
	void shouldAllowToUpdateResourceType() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		ResourceTypeId resourceTypeId = new ResourceTypeId(UUID.randomUUID());
		InfraServiceId infraServiceId = new InfraServiceId(UUID.randomUUID());
		ResourceType request = ResourceType.builder()
			.id(resourceTypeId)
			.siteId(siteId)
			.serviceId(infraServiceId)
			.name("name")
			.type(ResourceMeasureType.DATA)
			.unit(ResourceMeasureUnit.GB)
			.build();

		when(siteRepository.exists(request.siteId)).thenReturn(true);
		when(infraServiceRepository.exists(request.serviceId)).thenReturn(true);
		when(resourceTypeRepository.exists(request.id)).thenReturn(true);
		when(resourceTypeRepository.findById(request.id)).thenReturn(Optional.of(request));

		//when
		service.update(request);

		orderVerifier.verify(resourceTypeRepository).update(eq(request));
		orderVerifier.verify(publisher).publishEvent(eq(new ResourceTypeUpdatedEvent(request, request)));
	}

	@Test
	void shouldAllowToDeleteResourceType() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		ResourceTypeId resourceTypeId = new ResourceTypeId(UUID.randomUUID());
		when(resourceTypeRepository.exists(resourceTypeId)).thenReturn(true);
		ResourceType resourceType = ResourceType.builder().build();
		when(resourceTypeRepository.findById(resourceTypeId)).thenReturn(Optional.of(resourceType));

		//when
		service.delete(resourceTypeId, siteId);

		orderVerifier.verify(resourceTypeRepository).delete(eq(resourceTypeId));
		orderVerifier.verify(publisher).publishEvent(eq(new ResourceTypeRemovedEvent(resourceType)));
	}

	@Test
	void shouldNotAllowToDeleteResourceTypeDueToResourceTypeNotExists() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		ResourceTypeId resourceTypeId = new ResourceTypeId(UUID.randomUUID());
		when(resourceTypeRepository.exists(resourceTypeId)).thenReturn(false);

		//when
		assertThrows(IllegalArgumentException.class, () -> service.delete(resourceTypeId, siteId));
		orderVerifier.verify(resourceTypeRepository, times(0)).delete(eq(resourceTypeId));
		orderVerifier.verify(publisher, times(0)).publishEvent(any());
	}

}