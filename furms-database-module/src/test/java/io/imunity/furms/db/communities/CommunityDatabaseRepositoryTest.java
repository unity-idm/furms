/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.communities;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.images.FurmsImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.db.id.uuid.UUIDIdUtils.generateId;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CommunityDatabaseRepositoryTest extends DBIntegrationTest {

	@Autowired
	private CommunityDatabaseRepository repository;

	@Autowired
	private CommunityEntityRepository entityRepository;

	private byte[] imgTestFile;
	private byte[] imgTestFile2;

	@BeforeEach
	void init() throws IOException {
		imgTestFile = getClass().getClassLoader().getResourceAsStream("test.jpg").readAllBytes();
		imgTestFile2 = getClass().getClassLoader().getResourceAsStream("test2.jpg").readAllBytes();
	}

	@Test
	void shouldFindCreatedCommunity() {
		//given
		CommunityEntity entity = entityRepository.save(CommunityEntity.builder()
				.name("name")
				.description("description")
				.logo(imgTestFile, "jpg")
				.build());

		//when
		Optional<Community> byId = repository.findById(new CommunityId(entity.getId()));

		//then
		assertThat(byId).isPresent();
		Community community = byId.get();
		assertThat(community.getId().id).isEqualTo(entity.getId());
		assertThat(community.getName()).isEqualTo(entity.getName());
		assertThat(community.getDescription()).isEqualTo(entity.getDescription());
		assertThat(community.getLogo()).isEqualTo(new FurmsImage(entity.getLogoImage(), entity.getLogoType()));
	}

	@Test
	void shouldNotFindByIdIfDoesntExist() {
		//given
		UUID wrongId = generateId();
		entityRepository.save(CommunityEntity.builder()
				.name("random_site")
				.description("description")
				.logo(imgTestFile, "jpg")
				.build());

		//when
		Optional<Community> byId = repository.findById(new CommunityId(wrongId));

		//then
		assertThat(byId).isEmpty();
	}

	@Test
	void shouldFindAllCommunitys() {
		//given
		entityRepository.save(CommunityEntity.builder()
				.name("name1")
				.description("description")
				.logo(imgTestFile, "jpg")
				.build());
		entityRepository.save(CommunityEntity.builder()
				.name("name2")
				.description("description")
				.logo(imgTestFile2, "jpg")
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
				.logo(imgTestFile, "jpg")
				.build();

		//when
		CommunityId newCommunityId = repository.create(request);

		//then
		Optional<Community> byId = repository.findById(newCommunityId);
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isNotNull();
		assertThat(byId.get().getName()).isEqualTo("name");
	}

	@Test
	void shouldUpdateCommunity() {
		//given
		CommunityEntity old = entityRepository.save(CommunityEntity.builder()
				.name("name")
				.description("description")
				.logo(imgTestFile, "jpg")
				.build());
		Community requestToUpdate = Community.builder()
				.id(old.getId().toString())
				.name("new_name")
				.description("new_description")
				.logo(imgTestFile2, "jpg")
				.build();

		//when
		repository.update(requestToUpdate);

		//then
		Optional<Community> byId = repository.findById(new CommunityId(old.getId()));
		assertThat(byId).isPresent();
		assertThat(byId.get().getName()).isEqualTo("new_name");
		assertThat(byId.get().getDescription()).isEqualTo("new_description");
		assertThat(byId.get().getLogo().getImage()).isEqualTo(imgTestFile2);
		assertThat(byId.get().getLogo().getType()).isEqualTo("jpg");
	}

	@Test
	void savedCommunityExists() {
		//given
		CommunityEntity entity = entityRepository.save(CommunityEntity.builder()
				.name("name")
				.description("new_description")
				.logo(imgTestFile, "jpg")
				.build());

		//when + then
		assertThat(repository.exists(new CommunityId(entity.getId()))).isTrue();
	}

	@Test
	void shouldNotExistsDueToEmptyOrWrongId() {
		//given
		CommunityId nonExistedId = new CommunityId(generateId());

		//when + then
		assertThat(repository.exists(nonExistedId)).isFalse();
		assertThat(repository.exists(null)).isFalse();
		assertThat(repository.exists(new CommunityId((UUID) null))).isFalse();
	}

	@Test
	void shouldReturnTrueForUniqueName() {
		//given
		entityRepository.save(CommunityEntity.builder()
				.name("name")
				.description("new_description")
				.logo(imgTestFile, "jpg")
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
				.logo(imgTestFile, "jpg")
				.build());

		//when + then
		assertThat(repository.isUniqueName(existedCommunity.getName())).isFalse();
	}

}