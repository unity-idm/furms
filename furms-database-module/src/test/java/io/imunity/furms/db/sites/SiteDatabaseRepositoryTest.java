/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.sites;

import io.imunity.furms.domain.sites.Site;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.db.sites.SiteEntityUtils.generateId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class SiteDatabaseRepositoryTest {

	@Autowired
	private SiteDatabaseRepository repository;

	@Autowired
	private SiteEntityRepository entityRepository;

	@BeforeEach
	void setUp() {
		entityRepository.deleteAll();
	}

	@Test
	void shouldFindById() {
		//given
		SiteEntity entity = entityRepository.save(SiteEntity.builder()
				.name("name")
				.build());

		//when
		Optional<Site> byId = repository.findById(entity.getId().toString());

		//then
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(entity.getId().toString());
	}

	@Test
	void shouldNotFindByIdIfNotExists() {
		//given
		UUID wrongId = generateId();
		entityRepository.save(SiteEntity.builder()
				.name("random_site")
				.build());

		//when
		Optional<Site> byId = repository.findById(wrongId.toString());

		//then
		assertThat(byId).isEmpty();
	}

	@Test
	void shouldFindAllSites() {
		//given
		entityRepository.save(SiteEntity.builder()
				.name("name1")
				.build());
		entityRepository.save(SiteEntity.builder()
				.name("name2")
				.build());

		//when
		Set<Site> all = repository.findAll();

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void shouldCreateSite() {
		//given
		Site request = Site.builder()
				.name("name")
				.build();

		//when
		String newSiteId = repository.create(request);

		//then
		Optional<Site> byId = repository.findById(newSiteId);
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isNotNull();
		assertThat(byId.get().getName()).isEqualTo("name");
	}

	@Test
	void shouldNotCreatSiteDueToWrongRequest() {
		//given
		Site requestWithEmptyName = Site.builder()
				.name("")
				.build();
		Site nullRequest = null;
		entityRepository.save(SiteEntity.builder()
				.name("non_unique_name")
				.build());
		Site nonUniqueNameRequest = Site.builder()
				.name("non_unique_name")
				.build();

		//when + then
		assertThrows(IllegalArgumentException.class, () -> repository.create(requestWithEmptyName));
		assertThrows(IllegalArgumentException.class, () -> repository.create(nullRequest));
		assertThrows(IllegalArgumentException.class, () -> repository.create(nonUniqueNameRequest));
	}

	@Test
	void shouldUpdateSite() {
		//given
		SiteEntity old = entityRepository.save(SiteEntity.builder()
				.name("name")
				.build());
		Site requestToUpdate = Site.builder()
				.id(old.getId().toString())
				.name("new_name")
				.build();

		//when
		repository.update(requestToUpdate);

		//then
		Optional<Site> byId = repository.findById(old.getId().toString());
		assertThat(byId).isPresent();
		assertThat(byId.get().getName()).isEqualTo("new_name");
	}

	@Test
	void shouldExistsById() {
		//given
		SiteEntity entity = entityRepository.save(SiteEntity.builder()
				.name("name")
				.build());

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
		entityRepository.save(SiteEntity.builder()
				.name("name")
				.build());
		String uniqueName = "unique_name";

		//when + then
		assertThat(repository.isUniqueName(uniqueName)).isTrue();
	}

	@Test
	void shouldReturnFalseForNonUniqueName() {
		//given
		SiteEntity existedSite = entityRepository.save(SiteEntity.builder()
				.name("name")
				.build());

		//when + then
		assertThat(repository.isUniqueName(existedSite.getName())).isFalse();
	}



}