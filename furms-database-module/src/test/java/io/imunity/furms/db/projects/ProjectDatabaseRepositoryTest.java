/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.projects;


import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.spi.communites.CommunityRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.db.id.uuid.UUIDIdUtils.generateId;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProjectDatabaseRepositoryTest {

	@Autowired
	private CommunityRepository communityRepository;

	@Autowired
	private ProjectDatabaseRepository repository;

	@Autowired
	private ProjectEntityRepository entityRepository;

	private byte[] imgTestFile;
	private byte[] imgTestFile2;

	private UUID communityId;
	private UUID communityId2;

	private LocalDateTime startTime = LocalDateTime.of(2020, 5, 20, 5, 12, 16);
	private LocalDateTime endTime = LocalDateTime.of(2021, 6, 21, 4, 18, 4);
	private LocalDateTime newStartTime = LocalDateTime.of(2020, 8, 3, 4, 7, 5);
	private LocalDateTime newEndTime = LocalDateTime.of(2021, 9, 13, 3, 35, 33);

	@BeforeAll
	void init() throws IOException {
		imgTestFile = getClass().getClassLoader().getResourceAsStream("test.jpg").readAllBytes();
		imgTestFile2 = getClass().getClassLoader().getResourceAsStream("test2.jpg").readAllBytes();
		Community community = Community.builder()
			.name("name")
			.description("description")
			.logo(imgTestFile, "jpg")
			.build();
		Community community2 = Community.builder()
			.name("name2")
			.description("description2")
			.logo(imgTestFile2, "jpg")
			.build();
		communityId = UUID.fromString(communityRepository.create(community));
		communityId2 = UUID.fromString(communityRepository.create(community2));
	}

	@BeforeEach
	void setUp() {
		entityRepository.deleteAll();
	}

	@AfterAll
	void clean(){
		entityRepository.deleteAll();
	}

	@Test
	void shouldFindCreatedProject() {
		//given
		ProjectEntity entity = entityRepository.save(ProjectEntity.builder()
			.communityId(communityId)
			.name("name")
			.description("new_description")
			.logo(imgTestFile, "jpg")
			.acronym("acronym")
			.researchField("research filed")
			.startTime(startTime)
			.endTime(endTime)
			.build()
		);

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
				.communityId(communityId)
				.name("name")
				.description("new_description")
				.logo(imgTestFile, "jpg")
				.acronym("acronym")
				.researchField("research filed")
				.startTime(startTime)
				.endTime(endTime)
				.build()
		);

		//when
		Optional<Project> byId = repository.findById(wrongId.toString());

		//then
		assertThat(byId).isEmpty();
	}

	@Test
	void shouldFindAllProjects() {
		//given
		entityRepository.save(ProjectEntity.builder()
			.communityId(communityId)
			.name("name")
			.description("new_description")
			.logo(imgTestFile, "jpg")
			.acronym("acronym")
			.researchField("research filed")
			.startTime(startTime)
			.endTime(endTime)
			.build()
		);
		entityRepository.save(ProjectEntity.builder()
			.communityId(communityId)
			.name("name2")
			.description("new_description")
			.logo(imgTestFile2, "jpg")
			.acronym("acronym")
			.researchField("research filed")
			.startTime(startTime)
			.endTime(endTime)
			.build()
		);

		//when
		Set<Project> all = repository.findAll(communityId.toString());

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void shouldCreateProject() {
		//given
		Project request = Project.builder()
			.communityId(communityId.toString())
			.name("name")
			.description("new_description")
			.logo(imgTestFile, "jpg")
			.acronym("acronym")
			.researchField("research filed")
			.startTime(startTime)
			.endTime(endTime)
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
			.communityId(communityId)
			.name("name")
			.description("description")
			.logo(imgTestFile, "jpg")
			.acronym("acronym")
			.researchField("research filed")
			.startTime(startTime)
			.endTime(endTime)
			.build()
		);
		Project requestToUpdate = Project.builder()
			.id(old.getId().toString())
			.communityId(communityId.toString())
			.name("new_name")
			.description("new_description")
			.logo(imgTestFile2, "jpg")
			.acronym("new_acronym")
			.researchField("new_research filed")
			.startTime(newStartTime)
			.endTime(newEndTime)
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
		assertThat(byId.get().getAcronym()).isEqualTo("new_acronym");
		assertThat(byId.get().getResearchField()).isEqualTo("new_research filed");
		assertThat(byId.get().getStartTime()).isEqualTo(newStartTime);
		assertThat(byId.get().getEndTime()).isEqualTo(newEndTime);
	}

	@Test
	void savedProjectExists() {
		//given
		ProjectEntity entity = entityRepository.save(ProjectEntity.builder()
			.communityId(communityId)
			.name("name")
			.description("new_description")
			.logo(imgTestFile, "jpg")
			.acronym("acronym")
			.researchField("research filed")
			.startTime(startTime)
			.endTime(endTime)
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
		entityRepository.save(ProjectEntity.builder()
			.communityId(communityId)
			.name("name")
			.description("new_description")
			.logo(imgTestFile, "jpg")
			.acronym("acronym")
			.researchField("research filed")
			.startTime(startTime)
			.endTime(endTime)
			.build()
		);
		String uniqueName = "unique_name";

		//when + then
		assertThat(repository.isUniqueName(uniqueName)).isTrue();
	}

	@Test
	void shouldReturnFalseForNonUniqueName() {
		//given
		ProjectEntity existedProject = entityRepository.save(ProjectEntity.builder()
			.communityId(communityId)
			.name("name")
			.description("new_description")
			.logo(imgTestFile, "jpg")
			.acronym("acronym")
			.researchField("research filed")
			.startTime(startTime)
			.endTime(endTime)
			.build());

		//when + then
		assertThat(repository.isUniqueName(existedProject.getName())).isFalse();
	}



}