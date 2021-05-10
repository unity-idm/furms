/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import io.imunity.furms.domain.resource_types.CreateResourceTypeEvent;
import io.imunity.furms.domain.resource_types.RemoveResourceTypeEvent;
import io.imunity.furms.domain.resource_types.ResourceMeasureType;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.resource_types.UpdateResourceTypeEvent;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.services.InfraServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;

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
		MockitoAnnotations.initMocks(this);
		ResourceTypeServiceValidator validator = new ResourceTypeServiceValidator(resourceTypeRepository, resourceCreditRepository, infraServiceRepository, siteRepository);
		service = new ResourceTypeServiceImpl(resourceTypeRepository, validator, publisher);
		orderVerifier = inOrder(resourceTypeRepository, publisher);
	}

	@Test
	void shouldReturnResourceType() {
		//given
		String id = "id";
		when(resourceTypeRepository.findById(id)).thenReturn(Optional.of(ResourceType.builder()
			.id(id)
			.name("name")
			.build())
		);

		//when
		Optional<ResourceType> byId = service.findById(id, "");

		//then
		assertThat(byId).isPresent();
		assertThat(byId.get().id).isEqualTo(id);
	}

	@Test
	void shouldNotReturnResourceType() {
		//given
		String id = "id";
		when(resourceTypeRepository.findById(id)).thenReturn(Optional.of(ResourceType.builder()
			.id(id)
			.name("name")
			.build())
		);

		//when
		Optional<ResourceType> otherId = service.findById("otherId", "");

		//then
		assertThat(otherId).isEmpty();
	}

	@Test
	void shouldReturnAllResourceTypesIfExistsInRepository() {
		//given
		when(resourceTypeRepository.findAllBySiteId("1")).thenReturn(Set.of(
			ResourceType.builder().id("id1").name("name").build(),
			ResourceType.builder().id("id2").name("name2").build()));

		//when
		Set<ResourceType> allResourceTypes = service.findAll("1");

		//then
		assertThat(allResourceTypes).hasSize(2);
	}

	@Test
	void shouldAllowToCreateResourceType() {
		//given
		ResourceType request = ResourceType.builder()
			.id("id")
			.siteId("id")
			.serviceId("id")
			.name("name")
			.type(ResourceMeasureType.DATA)
			.unit(ResourceMeasureUnit.DataUnit.GB)
			.build();

		when(siteRepository.exists(request.siteId)).thenReturn(true);
		when(infraServiceRepository.exists(request.serviceId)).thenReturn(true);
		when(resourceTypeRepository.isNamePresent(request.name, request.siteId)).thenReturn(false);
		when(resourceTypeRepository.create(request)).thenReturn("id");

		//when
		service.create(request);

		orderVerifier.verify(resourceTypeRepository).create(eq(request));
		orderVerifier.verify(publisher).publishEvent(eq(new CreateResourceTypeEvent("id")));
	}

	@Test
	void shouldNotAllowToCreateResourceTypeDueToNonUniqueName() {
		//given
		ResourceType request = ResourceType.builder()
			.id("id")
			.name("name")
			.build();
		when(resourceTypeRepository.isNamePresent(request.name, request.siteId)).thenReturn(true);

		//when
		assertThrows(IllegalArgumentException.class, () -> service.create(request));
		orderVerifier.verify(resourceTypeRepository, times(0)).create(eq(request));
		orderVerifier.verify(publisher, times(0)).publishEvent(eq(new CreateResourceTypeEvent("id")));
	}

	@Test
	void shouldAllowToUpdateResourceType() {
		//given
		ResourceType request = ResourceType.builder()
			.id("id")
			.siteId("id")
			.serviceId("id")
			.name("name")
			.type(ResourceMeasureType.DATA)
			.unit(ResourceMeasureUnit.DataUnit.GB)
			.build();

		when(siteRepository.exists(request.siteId)).thenReturn(true);
		when(infraServiceRepository.exists(request.serviceId)).thenReturn(true);
		when(resourceTypeRepository.exists(request.id)).thenReturn(true);
		when(resourceTypeRepository.isNamePresent(request.name, request.siteId)).thenReturn(false);
		when(resourceTypeRepository.findById(request.id)).thenReturn(Optional.of(request));

		//when
		service.update(request);

		orderVerifier.verify(resourceTypeRepository).update(eq(request));
		orderVerifier.verify(publisher).publishEvent(eq(new UpdateResourceTypeEvent("id")));
	}

	@Test
	void shouldAllowToDeleteResourceType() {
		//given
		String id = "id";
		when(resourceTypeRepository.exists(id)).thenReturn(true);

		//when
		service.delete(id, "");

		orderVerifier.verify(resourceTypeRepository).delete(eq(id));
		orderVerifier.verify(publisher).publishEvent(eq(new RemoveResourceTypeEvent("id")));
	}

	@Test
	void shouldNotAllowToDeleteResourceTypeDueToResourceTypeNotExists() {
		//given
		String id = "id";
		when(resourceTypeRepository.exists(id)).thenReturn(false);

		//when
		assertThrows(IllegalArgumentException.class, () -> service.delete(id, ""));
		orderVerifier.verify(resourceTypeRepository, times(0)).delete(eq(id));
		orderVerifier.verify(publisher, times(0)).publishEvent(eq(new UpdateResourceTypeEvent("id")));
	}

}