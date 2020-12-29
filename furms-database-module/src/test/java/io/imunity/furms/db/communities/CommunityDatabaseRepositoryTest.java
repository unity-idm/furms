/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.communities;


import io.imunity.furms.domain.communities.Community;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.db.id.uuid.UUIDIdUtils.generateId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommunityDatabaseRepositoryTest {

	@Autowired
	private CommunityDatabaseRepository repository;

	@Autowired
	private CommunityEntityRepository entityRepository;

	private byte[] imgTestFile;
	private byte[] imgTestFile2;

	@BeforeAll
	void init() throws IOException {
		imgTestFile = getClass().getClassLoader().getResourceAsStream("test.jpg").readAllBytes();
		imgTestFile2 = getClass().getClassLoader().getResourceAsStream("test2.jpg").readAllBytes();
	}

	@BeforeEach
	void setUp() {
		entityRepository.deleteAll();
	}

	@Test
	void shouldFindById() throws IOException {
		//given
		CommunityEntity entity = entityRepository.save(CommunityEntity.builder()
				.name("name")
				.description("description")
				.logoImage(imgTestFile)
				.build());

		//when
		Optional<Community> byId = repository.findById(entity.getId().toString());

		//then
		assertThat(byId).isPresent();
		Community community = byId.get();
		assertThat(community.getId()).isEqualTo(entity.getId().toString());
		assertThat(community.getName()).isEqualTo(entity.getName());
		assertThat(community.getDescription()).isEqualTo(entity.getDescription());
		assertThat(community.getLogoImage()).isEqualTo(entity.getLogoImage());
	}

	@Test
	void shouldNotFindByIdIfNotExists() {
		//given
		UUID wrongId = generateId();
		entityRepository.save(CommunityEntity.builder()
				.name("random_site")
				.description("description")
				.logoImage(imgTestFile)
				.build());

		//when
		Optional<Community> byId = repository.findById(wrongId.toString());

		//then
		assertThat(byId).isEmpty();
	}

	@Test
	void shouldFindAllCommunitys() {
		//given
		entityRepository.save(CommunityEntity.builder()
				.name("name1")
				.description("description")
				.logoImage(imgTestFile)
				.build());
		entityRepository.save(CommunityEntity.builder()
				.name("name2")
				.description("description")
				.logoImage(imgTestFile2)
				.build());

		//when
		Set<Community> all = repository.findAll();

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void shouldCreateCommunity() {
		//given
		Community request = Community.builder()
				.name("name")
				.description("description")
				.logoImage(imgTestFile)
				.build();

		//when
		String newCommunityId = repository.create(request);

		//then
		Optional<Community> byId = repository.findById(newCommunityId);
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isNotNull();
		assertThat(byId.get().getName()).isEqualTo("name");
	}

	@Test
	void shouldNotCreatCommunityDueToWrongRequest() {
		//given
		Community requestWithEmptyName = Community.builder()
				.name("")
				.build();
		Community nullRequest = null;
		entityRepository.save(CommunityEntity.builder()
				.name("non_unique_name")
				.description("description")
				.logoImage(imgTestFile)
				.build());
		Community nonUniqueNameRequest = Community.builder()
				.name("non_unique_name")
				.description("description")
				.logoImage(imgTestFile2)
				.build();

		//when + then
		assertThrows(IllegalArgumentException.class, () -> repository.create(requestWithEmptyName));
		assertThrows(IllegalArgumentException.class, () -> repository.create(nullRequest));
		assertThrows(IllegalArgumentException.class, () -> repository.create(nonUniqueNameRequest));
	}

	@Test
	void shouldUpdateCommunity() {
		//given
		CommunityEntity old = entityRepository.save(CommunityEntity.builder()
				.name("name")
				.description("description")
				.logoImage(imgTestFile)
				.build());
		Community requestToUpdate = Community.builder()
				.id(old.getId().toString())
				.name("new_name")
				.description("new_description")
				.logoImage(imgTestFile2)
				.build();

		//when
		repository.update(requestToUpdate);

		//then
		Optional<Community> byId = repository.findById(old.getId().toString());
		assertThat(byId).isPresent();
		assertThat(byId.get().getName()).isEqualTo("new_name");
		assertThat(byId.get().getDescription()).isEqualTo("new_description");
		assertThat(byId.get().getLogoImage()).isEqualTo(imgTestFile2);
	}

	@Test
	void shouldExistsById() {
		//given
		CommunityEntity entity = entityRepository.save(CommunityEntity.builder()
				.name("name")
				.description("new_description")
				.logoImage(imgTestFile)
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
		entityRepository.save(CommunityEntity.builder()
				.name("name")
				.description("new_description")
				.logoImage(imgTestFile)
				.build());
		String uniqueName = "unique_name";

		//when + then
		assertThat(repository.isUniqueName(uniqueName)).isTrue();
	}

	@Test
	void shouldReturnFalseForNonUniqueName() {
		//given
		CommunityEntity existedCommunity = entityRepository.save(CommunityEntity.builder()
				.name("name")
				.description("new_description")
				.logoImage(imgTestFile)
				.build());

		//when + then
		assertThat(repository.isUniqueName(existedCommunity.getName())).isFalse();
	}



}