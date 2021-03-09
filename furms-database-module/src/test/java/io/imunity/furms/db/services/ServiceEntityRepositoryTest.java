/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.services;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.sites.Site;
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
class ServiceEntityRepositoryTest extends DBIntegrationTest {

	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private ServiceEntityRepository serviceRepository;

	private UUID siteId;
	private UUID siteId2;


	@BeforeEach
	void init() throws IOException {
		Site community = Site.builder()
			.name("name")
			.build();
		Site community2 = Site.builder()
			.name("name2")
			.build();
		siteId = UUID.fromString(siteRepository.create(community));
		siteId2 = UUID.fromString(siteRepository.create(community2));
	}

	@Test
	void shouldCreateService() {
		//given
		ServiceEntity entityToSave = ServiceEntity.builder()
			.name("name")
			.siteId(siteId)
			.description("description")
			.build();

		//when
		ServiceEntity saved = serviceRepository.save(entityToSave);

		//then
		assertThat(serviceRepository.findAll()).hasSize(1);
		Optional<ServiceEntity> byId = serviceRepository.findById(saved.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().siteId).isEqualTo(siteId);
		assertThat(byId.get().name).isEqualTo("name");
		assertThat(byId.get().description).isEqualTo("description");
	}

	@Test
	void shouldUpdateService() {
		//given
		ServiceEntity old = ServiceEntity.builder()
			.name("name")
			.siteId(siteId)
			.description("description")
			.build();
		serviceRepository.save(old);
		ServiceEntity toUpdate = ServiceEntity.builder()
			.name("new_name")
			.siteId(siteId2)
			.description("new_description")
			.build();

		//when
		serviceRepository.save(toUpdate);

		//then
		Optional<ServiceEntity> byId = serviceRepository.findById(toUpdate.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().siteId).isEqualTo(siteId2);
		assertThat(byId.get().name).isEqualTo("new_name");
		assertThat(byId.get().description).isEqualTo("new_description");
	}

	@Test
	void shouldFindCreatedServices() {
		//given
		ServiceEntity toFind = ServiceEntity.builder()
			.name("name")
			.siteId(siteId)
			.description("description")
			.build();
		serviceRepository.save(toFind);

		//when
		Optional<ServiceEntity> byId = serviceRepository.findById(toFind.getId());

		//then
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindAllAvailableServices() {
		//given
		serviceRepository.save(ServiceEntity.builder()
			.name("name")
			.siteId(siteId)
			.description("description")
			.build()
		);
		serviceRepository.save(ServiceEntity.builder()
			.name("new_name")
			.siteId(siteId2)
			.description("new_description")
			.build()
		);

		//when
		Iterable<ServiceEntity> all = serviceRepository.findAll();

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void savedServiceExistsByServiceId() {
		//given
		ServiceEntity service = serviceRepository.save(ServiceEntity.builder()
			.siteId(siteId)
			.name("name")
			.description("description")
			.build());

		//when + then
		assertThat(serviceRepository.existsById(service.getId())).isTrue();
		assertThat(serviceRepository.existsById(generateId())).isFalse();
	}

	@Test
	void savedServiceExistsByName() {
		//given
		ServiceEntity service = serviceRepository.save(ServiceEntity.builder()
			.name("name")
			.siteId(siteId)
			.description("description")
			.build());

		//when
		boolean exists = serviceRepository.existsByName(service.name);
		boolean nonExists = serviceRepository.existsByName("wrong_name");

		//then
		assertThat(exists).isTrue();
		assertThat(nonExists).isFalse();
	}

	@Test
	void shouldDeleteService() {
		//given
		ServiceEntity entityToRemove = serviceRepository.save(ServiceEntity.builder()
			.name("name")
			.siteId(siteId)
			.description("description")
			.build());

		//when
		serviceRepository.deleteById(entityToRemove.getId());

		//then
		assertThat(serviceRepository.findAll()).hasSize(0);
	}

	@Test
	void shouldDeleteAllServices() {
		//given
		serviceRepository.save(ServiceEntity.builder()
			.name("name")
			.siteId(siteId)
			.description("description")
			.build());
		serviceRepository.save(ServiceEntity.builder()
			.name("new_name")
			.siteId(siteId2)
			.description("new_description")
			.build());

		//when
		serviceRepository.deleteAll();

		//then
		assertThat(serviceRepository.findAll()).hasSize(0);
	}

}