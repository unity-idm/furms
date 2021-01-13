/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.communities;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Optional;

import static io.imunity.furms.db.id.uuid.UUIDIdUtils.generateId;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommunityEntityRepositoryTest {

	@Autowired
	private CommunityEntityRepository communityEntityRepository;

	private byte[] imgTestFile;
	private byte[] imgTestFile2;

	@BeforeAll
	void init() throws IOException {
		imgTestFile = getClass().getClassLoader().getResourceAsStream("test.jpg").readAllBytes();
		imgTestFile2 = getClass().getClassLoader().getResourceAsStream("test2.jpg").readAllBytes();
	}

	@BeforeEach
	void setUp() {
		communityEntityRepository.deleteAll();
	}

	@Test
	void shouldCreateCommunityEntity() {
		//given
		CommunityEntity entityToSave = CommunityEntity.builder()
				.name("name")
				.description("description")
				.logo(imgTestFile, "jpg")
				.build();

		//when
		CommunityEntity saved = communityEntityRepository.save(entityToSave);

		//then
		assertThat(communityEntityRepository.findAll()).hasSize(1);
		assertThat(communityEntityRepository.findById(saved.getId())).isPresent();
	}

	@Test
	void shouldUpdateCommunityEntity() {
		//given
		CommunityEntity old = communityEntityRepository.save(CommunityEntity.builder()
				.name("name")
				.description("description")
				.logo(imgTestFile, "jpg")
				.build());
		CommunityEntity toUpdate = CommunityEntity.builder()
				.id(old.getId())
				.name("new_name")
				.description("new_description")
				.logo(imgTestFile2, "jpg")
				.build();

		//when
		communityEntityRepository.save(toUpdate);

		//then
		Optional<CommunityEntity> byId = communityEntityRepository.findById(toUpdate.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().getName()).isEqualTo("new_name");
		assertThat(byId.get().getDescription()).isEqualTo("new_description");
		assertThat(byId.get().getLogoImage()).isEqualTo(imgTestFile2);
	}

	@Test
	void shouldFindCreatedCommunity() {
		//given
		CommunityEntity toFind = communityEntityRepository.save(CommunityEntity.builder()
				.name("name1")
				.description("description")
				.logo(imgTestFile, "jpg")
				.build());

		//when
		Optional<CommunityEntity> byId = communityEntityRepository.findById(toFind.getId());

		//then
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindAllAvailableCommunities() {
		//given
		communityEntityRepository.save(CommunityEntity.builder()
				.name("name1")
				.description("description")
				.logo(imgTestFile, "jpg")
				.build());
		communityEntityRepository.save(CommunityEntity.builder()
				.name("name2")
				.description("description")
				.logo(imgTestFile, "jpg")
				.build());

		//when
		Iterable<CommunityEntity> all = communityEntityRepository.findAll();

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void savedCommunityExistsByCommunityId() {
		//given
		CommunityEntity site = communityEntityRepository.save(CommunityEntity.builder()
				.name("name")
				.description("description")
				.logo(imgTestFile, "jpg")
				.build());

		//when + then
		assertThat(communityEntityRepository.existsById(site.getId())).isTrue();
		assertThat(communityEntityRepository.existsById(generateId())).isFalse();
	}

	@Test
	void savedCommunityExistsByName() {
		//given
		CommunityEntity site = communityEntityRepository.save(CommunityEntity.builder()
				.name("name")
				.description("description")
				.logo(imgTestFile, "jpg")
				.build());

		//when
		boolean exists = communityEntityRepository.existsByName(site.getName());
		boolean nonExists = communityEntityRepository.existsByName("wrong_name");

		//then
		assertThat(exists).isTrue();
		assertThat(nonExists).isFalse();
	}

	@Test
	void shouldDeleteCommunity() {
		//given
		CommunityEntity entityToRemove = communityEntityRepository.save(CommunityEntity.builder()
				.name("name1")
				.description("description")
				.logo(imgTestFile, "jpg")
				.build());

		//when
		communityEntityRepository.deleteById(entityToRemove.getId());

		//then
		assertThat(communityEntityRepository.findAll()).hasSize(0);
	}

	@Test
	void shouldDeleteAllEntities() {
		//given
		communityEntityRepository.save(CommunityEntity.builder()
				.name("name1")
				.description("description")
				.logo(imgTestFile, "jpg")
				.build());
		communityEntityRepository.save(CommunityEntity.builder()
				.name("name2")
				.description("description2")
				.logo(imgTestFile2, "jpg")
				.build());

		//when
		communityEntityRepository.deleteAll();

		//then
		assertThat(communityEntityRepository.findAll()).hasSize(0);
	}

}