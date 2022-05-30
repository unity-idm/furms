/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.services;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.services.InfraServiceId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
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

	private SiteId siteId;

	@BeforeEach
	void init() {
		Site site = Site.builder()
			.name("name")
			.build();
		Site site2 = Site.builder()
			.name("name2")
			.build();
		siteId = siteRepository.create(site, new SiteExternalId("id"));
		siteRepository.create(site2, new SiteExternalId("id2"));
	}

	@Test
	void shouldFindCreatedInfraService() {
		//given
		InfraServiceEntity entity = entityRepository.save(InfraServiceEntity.builder()
			.name("name")
			.description("new_description")
			.siteId(siteId.id)
			.build()
		);

		//when
		Optional<InfraService> byId = repository.findById(new InfraServiceId(entity.getId()));

		//then
		assertThat(byId).isPresent();
		InfraService project = byId.get();
		assertThat(project.id.id).isEqualTo(entity.getId());
		assertThat(project.name).isEqualTo(entity.name);
		assertThat(project.description).isEqualTo(entity.description);
	}

	@Test
	void shouldNotFindByIdIfDoesntExist() {
		//given
		InfraServiceId wrongId = new InfraServiceId(generateId());
		entityRepository.save(InfraServiceEntity.builder()
				.siteId(siteId.id)
				.name("name")
				.description("new_description")
				.build()
		);

		//when
		Optional<InfraService> byId = repository.findById(wrongId);

		//then
		assertThat(byId).isEmpty();
	}

	@Test
	void shouldFindAllInfraServices() {
		//given
		entityRepository.save(InfraServiceEntity.builder()
			.siteId(siteId.id)
			.name("name")
			.description("new_description")
			.build()
		);
		entityRepository.save(InfraServiceEntity.builder()
			.siteId(siteId.id)
			.name("name2")
			.description("new_description")
			.build()
		);

		//when
		Set<InfraService> all = repository.findAll(siteId);

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void shouldCreateInfraService() {
		//given
		InfraService request = InfraService.builder()
			.siteId(siteId)
			.name("name")
			.description("new_description")
			.build();

		//when
		InfraServiceId newServiceId = repository.create(request);

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
			.siteId(siteId.id)
			.name("name")
			.description("description")
			.build()
		);
		InfraService requestToUpdate = InfraService.builder()
			.id(new InfraServiceId(old.getId()))
			.siteId(siteId)
			.name("new_name")
			.description("new_description")
			.build();

		//when
		repository.update(requestToUpdate);

		//then
		Optional<InfraService> byId = repository.findById(new InfraServiceId(old.getId()));
		assertThat(byId).isPresent();
		assertThat(byId.get().name).isEqualTo("new_name");
		assertThat(byId.get().description).isEqualTo("new_description");
	}

	@Test
	void savedInfraServiceExists() {
		//given
		InfraServiceEntity entity = entityRepository.save(InfraServiceEntity.builder()
			.siteId(siteId.id)
			.name("name")
			.description("new_description")
			.build()
		);

		//when + then
		assertThat(repository.exists(new InfraServiceId(entity.getId()))).isTrue();
	}

	@Test
	void shouldNotExistsDueToEmptyOrWrongId() {
		//given
		InfraServiceId nonExistedId = new InfraServiceId(generateId());

		//when + then
		assertThat(repository.exists(nonExistedId)).isFalse();
		assertThat(repository.exists(null)).isFalse();
		assertThat(repository.exists(new InfraServiceId((UUID) null))).isFalse();
	}

	@Test
	void shouldReturnTrueForPresentName() {
		//given
		InfraServiceEntity existingService = entityRepository.save(InfraServiceEntity.builder()
			.siteId(siteId.id)
			.name("name")
			.description("new_description")
			.build()
		);

		//when + then
		assertThat(repository.isNamePresent(existingService.name, siteId)).isTrue();
	}

	@Test
	void shouldReturnFalseForNotPresentName() {
		//given
		InfraServiceEntity existingService = entityRepository.save(InfraServiceEntity.builder()
			.siteId(siteId.id)
			.name("name")
			.description("new_description")
			.build());

		//when + then
		assertThat(repository.isNamePresent(existingService.name + "foo", siteId)).isFalse();
	}

	@Test
	void shouldReturnFalseForPresentNameInOtherSite() {
		//given
		InfraServiceEntity existingService = entityRepository.save(InfraServiceEntity.builder()
			.siteId(siteId.id)
			.name("name")
			.description("new_description")
			.build());

		//when + then
		assertThat(repository.isNamePresent(existingService.name, new SiteId(UUID.randomUUID()))).isFalse();
	}

	
	@Test
	void shouldRemoveInfraServiceWhenAssociatedSiteHasRemoved() {
		//given
		InfraServiceEntity entity = entityRepository.save(InfraServiceEntity.builder()
			.name("name")
			.description("new_description")
			.siteId(siteId.id)
			.build()
		);

		//when
		siteRepository.delete(siteId);

		//then
		assertThat(repository.findById(new InfraServiceId(entity.getId()))).isEmpty();
	}

}