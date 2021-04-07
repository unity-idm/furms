/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.services;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.services.InfraService;
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
class InfraServiceDatabaseRepositoryTest extends DBIntegrationTest {

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private InfraServiceDatabaseRepository repository;

	@Autowired
	private InfraServiceEntityRepository entityRepository;

	private UUID siteId;

	@BeforeEach
	void init() {
		Site site = Site.builder()
			.name("name")
			.build();
		Site site2 = Site.builder()
			.name("name2")
			.build();
		siteId = UUID.fromString(siteRepository.create(site, "id"));
		siteRepository.create(site2, "id2");
	}

	@Test
	void shouldFindCreatedInfraService() {
		//given
		InfraServiceEntity entity = entityRepository.save(InfraServiceEntity.builder()
			.name("name")
			.description("new_description")
			.siteId(siteId)
			.build()
		);

		//when
		Optional<InfraService> byId = repository.findById(entity.getId().toString());

		//then
		assertThat(byId).isPresent();
		InfraService project = byId.get();
		assertThat(project.id).isEqualTo(entity.getId().toString());
		assertThat(project.name).isEqualTo(entity.name);
		assertThat(project.description).isEqualTo(entity.description);
	}

	@Test
	void shouldNotFindByIdIfDoesntExist() {
		//given
		UUID wrongId = generateId();
		entityRepository.save(InfraServiceEntity.builder()
				.siteId(siteId)
				.name("name")
				.description("new_description")
				.build()
		);

		//when
		Optional<InfraService> byId = repository.findById(wrongId.toString());

		//then
		assertThat(byId).isEmpty();
	}

	@Test
	void shouldFindAllInfraServices() {
		//given
		entityRepository.save(InfraServiceEntity.builder()
			.siteId(siteId)
			.name("name")
			.description("new_description")
			.build()
		);
		entityRepository.save(InfraServiceEntity.builder()
			.siteId(siteId)
			.name("name2")
			.description("new_description")
			.build()
		);

		//when
		Set<InfraService> all = repository.findAll(siteId.toString());

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void shouldCreateInfraService() {
		//given
		InfraService request = InfraService.builder()
			.siteId(siteId.toString())
			.name("name")
			.description("new_description")
			.build();

		//when
		String newServiceId = repository.create(request);

		//then
		Optional<InfraService> byId = repository.findById(newServiceId);
		assertThat(byId).isPresent();
		assertThat(byId.get().id).isNotNull();
		assertThat(byId.get().name).isEqualTo("name");
	}

	@Test
	void shouldUpdateInfraService() {
		//given
		InfraServiceEntity old = entityRepository.save(InfraServiceEntity.builder()
			.siteId(siteId)
			.name("name")
			.description("description")
			.build()
		);
		InfraService requestToUpdate = InfraService.builder()
			.id(old.getId().toString())
			.siteId(siteId.toString())
			.name("new_name")
			.description("new_description")
			.build();

		//when
		repository.update(requestToUpdate);

		//then
		Optional<InfraService> byId = repository.findById(old.getId().toString());
		assertThat(byId).isPresent();
		assertThat(byId.get().name).isEqualTo("new_name");
		assertThat(byId.get().description).isEqualTo("new_description");
	}

	@Test
	void savedInfraServiceExists() {
		//given
		InfraServiceEntity entity = entityRepository.save(InfraServiceEntity.builder()
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
		entityRepository.save(InfraServiceEntity.builder()
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
		InfraServiceEntity existedService = entityRepository.save(InfraServiceEntity.builder()
			.siteId(siteId)
			.name("name")
			.description("new_description")
			.build());

		//when + then
		assertThat(repository.isUniqueName(existedService.name)).isFalse();
	}

	@Test
	void shouldRemoveInfraServiceWhenAssociatedSiteHasRemoved() {
		//given
		InfraServiceEntity entity = entityRepository.save(InfraServiceEntity.builder()
			.name("name")
			.description("new_description")
			.siteId(siteId)
			.build()
		);

		//when
		siteRepository.delete(siteId.toString());

		//then
		assertThat(repository.findById(entity.getId().toString())).isEmpty();
	}

}