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

import static io.imunity.furms.db.sites.SiteEntityUtils.generateSiteId;
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
		String id = generateSiteId();
		entityRepository.save(SiteEntity.builder()
				.siteId(id)
				.name("name")
				.build());

		//when
		Optional<Site> byId = repository.findById(id);

		//then
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(id);
	}

	@Test
	void shouldNotFindByIdIfNotExists() {
		//given
		String wrong_id = "wrong_id";
		entityRepository.save(SiteEntity.builder()
				.siteId(generateSiteId())
				.name("random_site")
				.build());

		//when
		Optional<Site> byId = repository.findById(wrong_id);

		//then
		assertThat(byId).isEmpty();
	}

	@Test
	void shouldFindAllSites() {
		//given
		entityRepository.save(SiteEntity.builder()
				.siteId(generateSiteId())
				.name("name1")
				.build());
		entityRepository.save(SiteEntity.builder()
				.siteId(generateSiteId())
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
				.siteId(generateSiteId())
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
				.siteId(generateSiteId())
				.name("name")
				.build());
		Site requestToUpdate = Site.builder()
				.id(old.getSiteId())
				.name("new_name")
				.build();

		//when
		repository.update(requestToUpdate);

		//then
		Optional<Site> byId = repository.findById(old.getSiteId());
		assertThat(byId).isPresent();
		assertThat(byId.get().getName()).isEqualTo("new_name");
	}

	@Test
	void shouldExistsById() {
		//given
		String existedId = generateSiteId();
		entityRepository.save(SiteEntity.builder()
				.siteId(existedId)
				.name("name")
				.build());

		//when + then
		assertThat(repository.exists(existedId)).isTrue();
	}

	@Test
	void shouldNotExistsDueToEmptyOrWrongId() {
		//given
		String nonExistedId = generateSiteId();

		//when + then
		assertThat(repository.exists(nonExistedId)).isFalse();
		assertThat(repository.exists(null)).isFalse();
		assertThat(repository.exists("")).isFalse();
	}

	@Test
	void shouldReturnTrueForUniqueName() {
		//given
		entityRepository.save(SiteEntity.builder()
				.siteId(generateSiteId())
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
				.siteId(generateSiteId())
				.name("name")
				.build());

		//when + then
		assertThat(repository.isUniqueName(existedSite.getName())).isFalse();
	}



}