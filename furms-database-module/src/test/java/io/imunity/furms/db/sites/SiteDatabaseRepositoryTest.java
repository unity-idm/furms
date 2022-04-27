/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.sites;

import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.db.id.uuid.UUIDIdUtils.generateId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class SiteDatabaseRepositoryTest extends DBIntegrationTest {

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
				.externalId("id")
				.build());

		//when
		Optional<Site> byId = repository.findById(new SiteId(entity.getId()));

		//then
		assertThat(byId).isPresent();
		assertThat(byId.get().getId().id).isEqualTo(entity.getId());
	}

	@Test
	void shouldNotFindByIdIfDoesntExist() {
		//given
		SiteId wrongId = new SiteId(generateId());
		entityRepository.save(SiteEntity.builder()
				.name("random_site")
				.externalId("id")
				.build());

		//when
		Optional<Site> byId = repository.findById(wrongId);

		//then
		assertThat(byId).isEmpty();
	}

	@Test
	void shouldFindAllSites() {
		//given
		entityRepository.save(SiteEntity.builder()
				.name("name1")
				.externalId("id")
				.build());
		entityRepository.save(SiteEntity.builder()
				.name("name2")
				.externalId("id2")
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
		SiteId newSiteId = repository.create(request, new SiteExternalId("id"));

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

		//when + then
		assertThrows(IllegalArgumentException.class, () -> repository.create(requestWithEmptyName, new SiteExternalId("id2")));
		assertThrows(IllegalArgumentException.class, () -> repository.create(nullRequest, new SiteExternalId("id3")));
	}

	@Test
	void shouldUpdateSite() {
		//given
		SiteEntity old = entityRepository.save(SiteEntity.builder()
			.name("name")
			.externalId("id")
			.build());
		Site requestToUpdate = Site.builder()
			.id(new SiteId(old.getId().toString(), new SiteExternalId("id")))
			.name("new_name")
			.build();

		//when
		repository.update(requestToUpdate);

		//then
		Optional<Site> byId = repository.findById(new SiteId(old.getId()));
		assertThat(byId).isPresent();
		assertThat(byId.get().getName()).isEqualTo("new_name");
	}
	
	

	@Test
	void shouldExistsById() {
		//given
		SiteEntity entity = entityRepository.save(SiteEntity.builder()
				.name("name")
				.externalId("id")
				.build());

		//when + then
		assertThat(repository.exists(new SiteId(entity.getId()))).isTrue();
	}

	@Test
	void shouldNotExistsDueToEmptyOrWrongId() {
		//given
		SiteId nonExistedId = new SiteId(generateId());

		//when + then
		assertThat(repository.exists(nonExistedId)).isFalse();
		assertThat(repository.exists(null)).isFalse();
		assertThat(repository.exists(new SiteId((UUID) null))).isFalse();
	}

	@Test
	void shouldReturnFalseForNonPresentName() {
		//given
		entityRepository.save(SiteEntity.builder()
				.name("name")
				.externalId("id")
				.build());
		String uniqueName = "unique_name";

		//when + then
		assertThat(repository.isNamePresent(uniqueName)).isFalse();
	}

	@Test
	void shouldReturnTrueIfNamePresent() {
		//given
		SiteEntity existedSite = entityRepository.save(SiteEntity.builder()
				.name("name")
				.externalId("id")
				.build());

		//when + then
		assertThat(repository.isNamePresent(existedSite.getName())).isTrue();
	}

	@Test
	void shouldReturnTrueIfNameIsPresentOutOfSpecificRecord() {
		//given
		SiteEntity existedSite = entityRepository.save(SiteEntity.builder()
				.name("name")
				.externalId("id")
				.build());
		SiteEntity existedSite2 = entityRepository.save(SiteEntity.builder()
				.name("name2")
				.externalId("id2")
				.build());

		//when + then
		assertThat(repository.isNamePresentIgnoringRecord(existedSite.getName(), new SiteId(existedSite2.getId()))).isTrue();
	}

	@Test
	void shouldReturnFalseIfNameIsPresentOnlyInSpecificRecord() {
		//given
		SiteEntity existedSite = entityRepository.save(SiteEntity.builder()
				.name("name")
				.externalId("id")
				.build());

		//when + then
		assertThat(repository.isNamePresentIgnoringRecord(existedSite.getName(), new SiteId(existedSite.getId()))).isFalse();
	}


}