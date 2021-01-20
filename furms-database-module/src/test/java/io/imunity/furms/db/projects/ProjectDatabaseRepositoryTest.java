/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.projects;


import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.projects.Project;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static io.imunity.furms.db.id.uuid.UUIDIdUtils.generateId;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProjectDatabaseRepositoryTest {

	@Autowired
	private ProjectDatabaseRepository repository;

	@Autowired
	private ProjectEntityRepository entityRepository;

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
	void shouldFindCreatedProject() {
		//given
		ProjectEntity entity = entityRepository.save(ProjectEntity.builder()
				.name("name")
				.description("description")
				.logo(imgTestFile, "jpg")
				.build());

		//when
		Optional<Project> byId = repository.findById(entity.getId().toString());

		//then
		assertThat(byId).isPresent();
		Project project = byId.get();
		assertThat(project.getId()).isEqualTo(entity.getId().toString());
		assertThat(project.getName()).isEqualTo(entity.getName());
		assertThat(project.getDescription()).isEqualTo(entity.getDescription());
		assertThat(project.getLogo()).isEqualTo(new FurmsImage(entity.getLogoImage(), entity.getLogoType()));
	}

	@Test
	void shouldNotFindByIdIfDoesntExist() {
		//given
		UUID wrongId = generateId();
		entityRepository.save(ProjectEntity.builder()
				.name("random_site")
				.description("description")
				.logo(imgTestFile, "jpg")
				.build());

		//when
		Optional<Project> byId = repository.findById(wrongId.toString());

		//then
		assertThat(byId).isEmpty();
	}

	@Test
	void shouldFindAllProjects() {
		//given
		entityRepository.save(ProjectEntity.builder()
				.name("name1")
				.description("description")
				.logo(imgTestFile, "jpg")
				.build());
		entityRepository.save(ProjectEntity.builder()
				.name("name2")
				.description("description")
				.logo(imgTestFile2, "jpg")
				.build());

//		//when
//		Set<Project> all = repository.findAll();
//
//		//then
//		assertThat(all).hasSize(2);
	}

	@Test
	void shouldCreateProject() {
		//given
		Project request = Project.builder()
				.name("name")
				.description("description")
				.logo(imgTestFile, "jpg")
				.build();

		//when
		String newProjectId = repository.create(request);

		//then
		Optional<Project> byId = repository.findById(newProjectId);
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isNotNull();
		assertThat(byId.get().getName()).isEqualTo("name");
	}

	@Test
	void shouldUpdateProject() {
		//given
		ProjectEntity old = entityRepository.save(ProjectEntity.builder()
				.name("name")
				.description("description")
				.logo(imgTestFile, "jpg")
				.build());
		Project requestToUpdate = Project.builder()
				.id(old.getId().toString())
				.name("new_name")
				.description("new_description")
				.logo(imgTestFile2, "jpg")
				.build();

		//when
		repository.update(requestToUpdate);

		//then
		Optional<Project> byId = repository.findById(old.getId().toString());
		assertThat(byId).isPresent();
		assertThat(byId.get().getName()).isEqualTo("new_name");
		assertThat(byId.get().getDescription()).isEqualTo("new_description");
		assertThat(byId.get().getLogo().getImage()).isEqualTo(imgTestFile2);
		assertThat(byId.get().getLogo().getType()).isEqualTo("jpg");
	}

	@Test
	void savedProjectExists() {
		//given
		ProjectEntity entity = entityRepository.save(ProjectEntity.builder()
				.name("name")
				.description("new_description")
				.logo(imgTestFile, "jpg")
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
		entityRepository.save(ProjectEntity.builder()
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
		ProjectEntity existedProject = entityRepository.save(ProjectEntity.builder()
				.name("name")
				.description("new_description")
				.logo(imgTestFile, "jpg")
				.build());

		//when + then
		assertThat(repository.isUniqueName(existedProject.getName())).isFalse();
	}



}