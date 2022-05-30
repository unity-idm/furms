/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_types;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.resource_types.ResourceMeasureType;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.services.InfraServiceId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.spi.services.InfraServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.db.id.uuid.UUIDIdUtils.generateId;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ResourceTypeDatabaseRepositoryTest extends DBIntegrationTest {

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private InfraServiceRepository infraServiceRepository;

	@Autowired
	private ResourceTypeDatabaseRepository repository;

	@Autowired
	private ResourceTypeEntityRepository entityRepository;

	private SiteId siteId;
	private InfraServiceId serviceId;

	@BeforeEach
	void init() {
		Site site = Site.builder()
			.name("name")
			.build();
		Site site2 = Site.builder()
			.name("name2")
			.build();
		siteId = siteRepository.create(site, new SiteExternalId("id"));
		SiteId siteId2 = siteRepository.create(site2, new SiteExternalId("id2"));

		InfraService service = InfraService.builder()
			.siteId(siteId)
			.name("name")
			.build();
		InfraService service1 = InfraService.builder()
			.siteId(siteId2)
			.name("name1")
			.build();
		serviceId = infraServiceRepository.create(service);
		infraServiceRepository.create(service1);
	}

	@Test
	void shouldFindCreatedInfraService() {
		//given
		ResourceTypeEntity entity = entityRepository.save(ResourceTypeEntity.builder()
			.siteId(siteId.id)
			.serviceId(serviceId.id)
			.name("name")
			.type(ResourceMeasureType.FLOATING_POINT)
			.unit(ResourceMeasureUnit.GIGA)
			.build()
		);

		//when
		Optional<ResourceType> byId = repository.findById(new ResourceTypeId(entity.getId()));

		//then
		assertThat(byId).isPresent();
		ResourceType project = byId.get();
		assertThat(project.id.id).isEqualTo(entity.getId());
		assertThat(project.name).isEqualTo(entity.name);
		assertThat(byId.get().type).isEqualTo(ResourceMeasureType.FLOATING_POINT);
		assertThat(byId.get().unit).isEqualTo(ResourceMeasureUnit.GIGA);
	}

	@Test
	void shouldNotFindByIdIfDoesntExist() {
		//given
		ResourceTypeId wrongId = new ResourceTypeId(generateId());
		entityRepository.save(ResourceTypeEntity.builder()
			.siteId(siteId.id)
			.serviceId(serviceId.id)
			.name("name")
			.type(ResourceMeasureType.FLOATING_POINT)
			.unit(ResourceMeasureUnit.GIGA)
			.build()
		);

		//when
		Optional<ResourceType> byId = repository.findById(wrongId);

		//then
		assertThat(byId).isEmpty();
	}

	@Test
	void shouldFindAllResourceTypes() {
		//given
		entityRepository.save(ResourceTypeEntity.builder()
			.siteId(siteId.id)
			.serviceId(serviceId.id)
			.name("name")
			.type(ResourceMeasureType.FLOATING_POINT)
			.unit(ResourceMeasureUnit.GIGA)
			.build()
		);
		entityRepository.save(ResourceTypeEntity.builder()
			.siteId(siteId.id)
			.serviceId(serviceId.id)
			.name("name2")
			.type(ResourceMeasureType.FLOATING_POINT)
			.unit(ResourceMeasureUnit.TERA)
			.build()
		);

		//when
		Set<ResourceType> all = repository.findAllBySiteId(siteId);

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void shouldFindAllResourceTypesByServiceId() {
		//given
		entityRepository.save(ResourceTypeEntity.builder()
			.siteId(siteId.id)
			.serviceId(serviceId.id)
			.name("name")
			.type(ResourceMeasureType.FLOATING_POINT)
			.unit(ResourceMeasureUnit.GIGA)
			.build()
		);
		entityRepository.save(ResourceTypeEntity.builder()
			.siteId(siteId.id)
			.serviceId(serviceId.id)
			.name("name2")
			.type(ResourceMeasureType.FLOATING_POINT)
			.unit(ResourceMeasureUnit.TERA)
			.build()
		);

		//when
		Set<ResourceType> all = repository.findAllByInfraServiceId(serviceId);

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void shouldCreateResourceType() {
		//given
		ResourceType request = ResourceType.builder()
			.siteId(siteId)
			.serviceId(serviceId)
			.name("name")
			.type(ResourceMeasureType.FLOATING_POINT)
			.unit(ResourceMeasureUnit.GIGA)
			.build();

		//when
		ResourceTypeId newResourceTypeId = repository.create(request);

		//then
		Optional<ResourceType> byId = repository.findById(newResourceTypeId);
		assertThat(byId).isPresent();
		assertThat(byId.get().id).isNotNull();
		assertThat(byId.get().name).isEqualTo("name");
		assertThat(byId.get().type).isEqualTo(ResourceMeasureType.FLOATING_POINT);
		assertThat(byId.get().unit).isEqualTo(ResourceMeasureUnit.GIGA);
	}

	@Test
	void shouldUpdateResourceType() {
		//given
		ResourceTypeEntity old = entityRepository.save(ResourceTypeEntity.builder()
			.siteId(siteId.id)
			.serviceId(serviceId.id)
			.name("name")
			.type(ResourceMeasureType.FLOATING_POINT)
			.unit(ResourceMeasureUnit.GIGA)
			.build()
		);
		ResourceType requestToUpdate = ResourceType.builder()
			.id(old.getId().toString())
			.siteId(siteId)
			.serviceId(serviceId)
			.name("new_name")
			.type(ResourceMeasureType.DATA)
			.unit(ResourceMeasureUnit.MB)
			.build();

		//when
		repository.update(requestToUpdate);

		//then
		Optional<ResourceType> byId = repository.findById(new ResourceTypeId(old.getId()));
		assertThat(byId).isPresent();
		assertThat(byId.get().name).isEqualTo("new_name");
		assertThat(byId.get().type).isEqualTo(ResourceMeasureType.DATA);
		assertThat(byId.get().unit).isEqualTo(ResourceMeasureUnit.MB);
	}

	@Test
	void savedResourceTypeExists() {
		//given
		ResourceTypeEntity entity = entityRepository.save(ResourceTypeEntity.builder()
			.siteId(siteId.id)
			.serviceId(serviceId.id)
			.name("new_name")
			.type(ResourceMeasureType.DATA)
			.unit(ResourceMeasureUnit.MB)
			.build()
		);

		//when + then
		assertThat(repository.exists(new ResourceTypeId(entity.getId()))).isTrue();
	}

	@Test
	void shouldNotExistsDueToEmptyOrWrongId() {
		//given
		ResourceTypeId nonExistedId = new ResourceTypeId(generateId());

		//when + then
		assertThat(repository.exists(nonExistedId)).isFalse();
		assertThat(repository.exists(null)).isFalse();
		assertThat(repository.exists(new ResourceTypeId((UUID) null))).isFalse();
	}

	@Test
	void shouldReturnFalseForMissingName() {
		//given
		entityRepository.save(ResourceTypeEntity.builder()
			.siteId(siteId.id)
			.serviceId(serviceId.id)
			.name("new_name")
			.type(ResourceMeasureType.DATA)
			.unit(ResourceMeasureUnit.MB)
			.build()
		);
		String uniqueName = "unique_name";

		//when + then
		assertThat(repository.isNamePresent(uniqueName, siteId)).isFalse();
	}

	@Test
	void shouldReturnTrueForPresentName() {
		//given
		ResourceTypeEntity existingResourceType = entityRepository.save(ResourceTypeEntity.builder()
			.siteId(siteId.id)
			.serviceId(serviceId.id)
			.name("new_name")
			.type(ResourceMeasureType.DATA)
			.unit(ResourceMeasureUnit.MB)
			.build());

		//when + then
		assertThat(repository.isNamePresent(existingResourceType.name, siteId)).isTrue();
	}

	@Test
	void shouldReturnFalseForPresentNameInOtherSite() {
		//given
		ResourceTypeEntity existingResourceType = entityRepository.save(ResourceTypeEntity.builder()
			.siteId(siteId.id)
			.serviceId(serviceId.id)
			.name("new_name")
			.type(ResourceMeasureType.DATA)
			.unit(ResourceMeasureUnit.MB)
			.build());

		//when + then
		assertThat(repository.isNamePresent(existingResourceType.name, new SiteId(UUID.randomUUID()))).isFalse();
	}
	
	@Test
	void shouldRemoveResourceTypeWhenSiteHasRemoved() {
		//given
		ResourceTypeEntity entity = entityRepository.save(ResourceTypeEntity.builder()
			.siteId(siteId.id)
			.serviceId(serviceId.id)
			.name("name")
			.type(ResourceMeasureType.FLOATING_POINT)
			.unit(ResourceMeasureUnit.GIGA)
			.build()
		);

		//when
		siteRepository.delete(siteId);

		//then
		assertThat(repository.findById(new ResourceTypeId(entity.getId()))).isEmpty();
	}

	@Test
	void shouldRemoveResourceTypeWhenInfraServiceHasRemoved() {
		//given
		ResourceTypeEntity entity = entityRepository.save(ResourceTypeEntity.builder()
			.siteId(siteId.id)
			.serviceId(serviceId.id)
			.name("name")
			.type(ResourceMeasureType.FLOATING_POINT)
			.unit(ResourceMeasureUnit.GIGA)
			.build()
		);

		//when
		infraServiceRepository.delete(serviceId);

		//then
		assertThat(repository.findById(new ResourceTypeId(entity.getId()))).isEmpty();
	}
}