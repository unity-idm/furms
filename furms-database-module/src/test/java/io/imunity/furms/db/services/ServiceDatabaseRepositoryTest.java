/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.services;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.services.Service;
import io.imunity.furms.domain.sites.Site;
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
class ServiceDatabaseRepositoryTest extends DBIntegrationTest {

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private ServiceDatabaseRepository repository;

	@Autowired
	private ServiceEntityRepository entityRepository;

	private UUID siteId;

	@BeforeEach
	void init() {
		Site site = Site.builder()
			.name("name")
			.build();
		Site site2 = Site.builder()
			.name("name2")
			.build();
		siteId = UUID.fromString(siteRepository.create(site));
		siteRepository.create(site2);
	}

	@Test
	void shouldFindCreatedService() {
		//given
		ServiceEntity entity = entityRepository.save(ServiceEntity.builder()
			.name("name")
			.description("new_description")
			.siteId(siteId)
			.build()
		);

		//when
		Optional<Service> byId = repository.findById(entity.getId().toString());

		//then
		assertThat(byId).isPresent();
		Service project = byId.get();
		assertThat(project.id).isEqualTo(entity.getId().toString());
		assertThat(project.name).isEqualTo(entity.name);
		assertThat(project.description).isEqualTo(entity.description);
	}

	@Test
	void shouldNotFindByIdIfDoesntExist() {
		//given
		UUID wrongId = generateId();
		entityRepository.save(ServiceEntity.builder()
				.siteId(siteId)
				.name("name")
				.description("new_description")
				.build()
		);

		//when
		Optional<Service> byId = repository.findById(wrongId.toString());

		//then
		assertThat(byId).isEmpty();
	}

	@Test
	void shouldFindAllServices() {
		//given
		entityRepository.save(ServiceEntity.builder()
			.siteId(siteId)
			.name("name")
			.description("new_description")
			.build()
		);
		entityRepository.save(ServiceEntity.builder()
			.siteId(siteId)
			.name("name2")
			.description("new_description")
			.build()
		);

		//when
		Set<Service> all = repository.findAll(siteId.toString());

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void shouldCreateService() {
		//given
		Service request = Service.builder()
			.siteId(siteId.toString())
			.name("name")
			.description("new_description")
			.build();

		//when
		String newServiceId = repository.create(request);

		//then
		Optional<Service> byId = repository.findById(newServiceId);
		assertThat(byId).isPresent();
		assertThat(byId.get().id).isNotNull();
		assertThat(byId.get().name).isEqualTo("name");
	}

	@Test
	void shouldUpdateService() {
		//given
		ServiceEntity old = entityRepository.save(ServiceEntity.builder()
			.siteId(siteId)
			.name("name")
			.description("description")
			.build()
		);
		Service requestToUpdate = Service.builder()
			.id(old.getId().toString())
			.siteId(siteId.toString())
			.name("new_name")
			.description("new_description")
			.build();

		//when
		repository.update(requestToUpdate);

		//then
		Optional<Service> byId = repository.findById(old.getId().toString());
		assertThat(byId).isPresent();
		assertThat(byId.get().name).isEqualTo("new_name");
		assertThat(byId.get().description).isEqualTo("new_description");
	}

	@Test
	void savedServiceExists() {
		//given
		ServiceEntity entity = entityRepository.save(ServiceEntity.builder()
			.siteId(siteId)
			.name("name")
			.description("new_description")
			.build()
		);

		//when + then
		assertThat(repository.exists(entity.getId().toString())).isTrue();
	}

	@Test
	void shouldNotExistsDueToEmptyOrWrongId() {
		//given
		String nonExistedId = generateId().toString();

		//when + then
		assertThat(repository.exists(nonExistedId)).isFalse();
		assertThat(repository.exists(null)).isFalse();
		assertThat(repository.exists("")).isFalse();
	}

	@Test
	void shouldReturnTrueForUniqueName() {
		//given
		entityRepository.save(ServiceEntity.builder()
			.siteId(siteId)
			.name("name")
			.description("new_description")
			.build()
		);
		String uniqueName = "unique_name";

		//when + then
		assertThat(repository.isUniqueName(uniqueName)).isTrue();
	}

	@Test
	void shouldReturnFalseForNonUniqueName() {
		//given
		ServiceEntity existedService = entityRepository.save(ServiceEntity.builder()
			.siteId(siteId)
			.name("name")
			.description("new_description")
			.build());

		//when + then
		assertThat(repository.isUniqueName(existedService.name)).isFalse();
	}



}