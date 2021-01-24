/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.sites;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.imunity.furms.db.DBIntegrationTest;

import java.util.Optional;

import static io.imunity.furms.db.id.uuid.UUIDIdUtils.generateId;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SiteEntityRepositoryTest extends DBIntegrationTest {

	@Autowired
	private SiteEntityRepository siteEntityRepository;

	@BeforeEach
	void setUp() {
		siteEntityRepository.deleteAll();
	}

	@Test
	void shouldCreateSiteEntity() {
		//given
		SiteEntity entityToSave = SiteEntity.builder()
				.name("name")
				.build();

		//when
		SiteEntity saved = siteEntityRepository.save(entityToSave);

		//then
		assertThat(siteEntityRepository.findAll()).hasSize(1);
		assertThat(siteEntityRepository.findById(saved.getId())).isPresent();
	}

	@Test
	void shouldUpdateSiteEntity() {
		//given
		SiteEntity old = siteEntityRepository.save(SiteEntity.builder()
				.name("name")
				.build());
		SiteEntity toUpdate = SiteEntity.builder()
				.id(old.getId())
				.name("new_name")
				.build();

		//when
		siteEntityRepository.save(toUpdate);

		//then
		Optional<SiteEntity> byId = siteEntityRepository.findById(toUpdate.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().getName()).isEqualTo("new_name");
	}

	@Test
	void shouldFindSiteById() {
		//given
		SiteEntity toFind = siteEntityRepository.save(SiteEntity.builder()
				.name("name1")
				.build());

		//when
		Optional<SiteEntity> byId = siteEntityRepository.findById(toFind.getId());

		//then
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindSiteBySiteId() {
		//given
		SiteEntity toFind = siteEntityRepository.save(SiteEntity.builder()
				.name("name1")
				.build());

		//when
		Optional<SiteEntity> bySiteId = siteEntityRepository.findById(toFind.getId());

		//then
		assertThat(bySiteId).isPresent();
	}

	@Test
	void shouldFindAllAvailableSites() {
		//given
		siteEntityRepository.save(SiteEntity.builder()
				.name("name1")
				.build());
		siteEntityRepository.save(SiteEntity.builder()
				.name("name2")
				.build());

		//when
		Iterable<SiteEntity> all = siteEntityRepository.findAll();

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void shouldCheckIfExistsBySiteId() {
		//given
		SiteEntity site = siteEntityRepository.save(SiteEntity.builder()
				.name("name")
				.build());

		//when + then
		assertThat(siteEntityRepository.existsById(site.getId())).isTrue();
		assertThat(siteEntityRepository.existsById(generateId())).isFalse();
	}

	@Test
	void shouldCheckIfExistsByName() {
		//given
		SiteEntity site = siteEntityRepository.save(SiteEntity.builder()
				.name("name")
				.build());

		//when
		boolean exists = siteEntityRepository.existsByName(site.getName());
		boolean nonExists = siteEntityRepository.existsByName("wrong_name");

		//then
		assertThat(exists).isTrue();
		assertThat(nonExists).isFalse();
	}

	@Test
	void shouldDeleteEntity() {
		//given
		SiteEntity entityToRemove = siteEntityRepository.save(SiteEntity.builder()
				.name("name1")
				.build());

		//when
		siteEntityRepository.deleteById(entityToRemove.getId());

		//then
		assertThat(siteEntityRepository.findAll()).hasSize(0);
	}

	@Test
	void shouldDeleteEntityBySiteId() {
		//given
		SiteEntity entityToRemove = siteEntityRepository.save(SiteEntity.builder()
				.name("name1")
				.build());

		//when
		siteEntityRepository.deleteById(entityToRemove.getId());

		//then
		assertThat(siteEntityRepository.findAll()).hasSize(0);
	}

	@Test
	void shouldDeleteAllEntities() {
		//given
		siteEntityRepository.save(SiteEntity.builder()
				.name("name1")
				.build());
		siteEntityRepository.save(SiteEntity.builder()
				.name("name2")
				.build());

		//when
		siteEntityRepository.deleteAll();

		//then
		assertThat(siteEntityRepository.findAll()).hasSize(0);
	}

}