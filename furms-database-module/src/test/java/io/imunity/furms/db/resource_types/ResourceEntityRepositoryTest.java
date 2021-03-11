/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_types;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.resource_types.Type;
import io.imunity.furms.domain.resource_types.Unit;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.spi.services.InfraServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static io.imunity.furms.db.id.uuid.UUIDIdUtils.generateId;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ResourceEntityRepositoryTest extends DBIntegrationTest {

	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private InfraServiceRepository infraServiceRepository;
	@Autowired
	private ResourceTypeEntityRepository resourceTypeRepository;

	private UUID siteId;
	private UUID siteId2;

	private UUID serviceId;
	private UUID serviceId2;

	@BeforeEach
	void init() throws IOException {
		Site site = Site.builder()
			.name("name")
			.build();
		Site site1 = Site.builder()
			.name("name2")
			.build();
		siteId = UUID.fromString(siteRepository.create(site));
		siteId2 = UUID.fromString(siteRepository.create(site1));

		InfraService service = InfraService.builder()
			.siteId(siteId.toString())
			.name("name")
			.build();
		InfraService service1 = InfraService.builder()
			.siteId(siteId2.toString())
			.name("name1")
			.build();
		serviceId = UUID.fromString(infraServiceRepository.create(service));
		serviceId2 = UUID.fromString(infraServiceRepository.create(service1));
	}

	@Test
	void shouldCreateResourceType() {
		//given
		ResourceTypeEntity entityToSave = ResourceTypeEntity.builder()
			.siteId(siteId)
			.serviceId(serviceId)
			.name("name")
			.type(Type.FLOATING_POINT)
			.unit(Unit.SiUnit.A)
			.build();

		//when
		ResourceTypeEntity saved = resourceTypeRepository.save(entityToSave);

		//then
		assertThat(resourceTypeRepository.findAll()).hasSize(1);
		Optional<ResourceTypeEntity> byId = resourceTypeRepository.findById(saved.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().siteId).isEqualTo(siteId);
		assertThat(byId.get().serviceId).isEqualTo(serviceId);
		assertThat(byId.get().name).isEqualTo("name");
		assertThat(byId.get().type).isEqualTo(Type.FLOATING_POINT);
		assertThat(byId.get().unit).isEqualTo(Unit.SiUnit.A);
	}

	@Test
	void shouldUpdateResourceType() {
		//given
		ResourceTypeEntity old = ResourceTypeEntity.builder()
			.siteId(siteId)
			.serviceId(serviceId)
			.name("name")
			.type(Type.FLOATING_POINT)
			.unit(Unit.SiUnit.A)
			.build();
		resourceTypeRepository.save(old);
		ResourceTypeEntity toUpdate = ResourceTypeEntity.builder()
			.siteId(siteId)
			.serviceId(serviceId2)
			.name("name2")
			.type(Type.DATA)
			.unit(Unit.DataUnit.GB)
			.build();

		//when
		resourceTypeRepository.save(toUpdate);

		//then
		Optional<ResourceTypeEntity> byId = resourceTypeRepository.findById(toUpdate.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().siteId).isEqualTo(siteId);
		assertThat(byId.get().serviceId).isEqualTo(serviceId2);
		assertThat(byId.get().name).isEqualTo("name2");
		assertThat(byId.get().type).isEqualTo(Type.DATA);
		assertThat(byId.get().unit).isEqualTo(Unit.DataUnit.GB);
	}

	@Test
	void shouldFindCreatedResourceTypes() {
		//given
		ResourceTypeEntity toFind = ResourceTypeEntity.builder()
			.siteId(siteId)
			.serviceId(serviceId)
			.name("name")
			.type(Type.FLOATING_POINT)
			.unit(Unit.SiUnit.A)
			.build();
		resourceTypeRepository.save(toFind);

		//when
		Optional<ResourceTypeEntity> byId = resourceTypeRepository.findById(toFind.getId());

		//then
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindAllAvailableResourceTypes() {
		//given
		resourceTypeRepository.save(ResourceTypeEntity.builder()
			.siteId(siteId)
			.serviceId(serviceId)
			.name("name")
			.type(Type.FLOATING_POINT)
			.unit(Unit.SiUnit.A)
			.build()
		);
		resourceTypeRepository.save(ResourceTypeEntity.builder()
			.siteId(siteId)
			.serviceId(serviceId)
			.name("name1")
			.type(Type.DATA)
			.unit(Unit.DataUnit.GB)
			.build()
		);

		//when
		Iterable<ResourceTypeEntity> all = resourceTypeRepository.findAll();

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void savedServiceExistsByResourceTypeId() {
		//given
		ResourceTypeEntity service = resourceTypeRepository.save(ResourceTypeEntity.builder()
			.siteId(siteId)
			.serviceId(serviceId)
			.name("name")
			.type(Type.DATA)
			.unit(Unit.DataUnit.GB)
			.build());

		//when + then
		assertThat(resourceTypeRepository.existsById(service.getId())).isTrue();
		assertThat(resourceTypeRepository.existsById(generateId())).isFalse();
	}

	@Test
	void savedResourceTypeExistsByName() {
		//given
		ResourceTypeEntity service = resourceTypeRepository.save(ResourceTypeEntity.builder()
			.siteId(siteId)
			.serviceId(serviceId)
			.name("name")
			.type(Type.DATA)
			.unit(Unit.DataUnit.GB)
			.build());

		//when
		boolean exists = resourceTypeRepository.existsByName(service.name);

		//then
		assertThat(exists).isTrue();
	}

	@Test
	void savedResourceTypeDoesNotExistByName() {
		//given
		resourceTypeRepository.save(ResourceTypeEntity.builder()
			.siteId(siteId)
			.serviceId(serviceId)
			.name("name")
			.type(Type.DATA)
			.unit(Unit.DataUnit.GB)
			.build());

		//when
		boolean nonExists = resourceTypeRepository.existsByName("wrong_name");

		//then
		assertThat(nonExists).isFalse();
	}

	@Test
	void shouldDeleteService() {
		//given
		ResourceTypeEntity entityToRemove = resourceTypeRepository.save(ResourceTypeEntity.builder()
			.siteId(siteId)
			.serviceId(serviceId)
			.name("name")
			.type(Type.DATA)
			.unit(Unit.DataUnit.GB)
			.build());

		//when
		resourceTypeRepository.deleteById(entityToRemove.getId());

		//then
		assertThat(resourceTypeRepository.findAll()).hasSize(0);
	}

	@Test
	void shouldDeleteAllServices() {
		//given
		resourceTypeRepository.save(ResourceTypeEntity.builder()
			.siteId(siteId)
			.serviceId(serviceId)
			.name("name")
			.type(Type.DATA)
			.unit(Unit.DataUnit.GB)
			.build());
		resourceTypeRepository.save(ResourceTypeEntity.builder()
			.siteId(siteId)
			.serviceId(serviceId)
			.name("name1")
			.type(Type.FLOATING_POINT)
			.unit(Unit.SiUnit.A)
			.build());

		//when
		resourceTypeRepository.deleteAll();

		//then
		assertThat(resourceTypeRepository.findAll()).hasSize(0);
	}

}