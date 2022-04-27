/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.projects;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.spi.communites.CommunityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static io.imunity.furms.db.id.uuid.UUIDIdUtils.generateId;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProjectEntityRepositoryTest extends DBIntegrationTest {

	@Autowired
	private CommunityRepository communityRepository;
	@Autowired
	private ProjectEntityRepository projectRepository;

	private CommunityId communityId;
	private CommunityId communityId2;

	private final LocalDateTime startTime = LocalDateTime.of(2020, 5, 20, 5, 12, 16);
	private final LocalDateTime endTime = LocalDateTime.of(2021, 6, 21, 4, 18, 4);
	private final LocalDateTime newStartTime = LocalDateTime.of(2020, 8, 3, 4, 7, 5);
	private final LocalDateTime newEndTime = LocalDateTime.of(2021, 9, 13, 3, 35, 33);

	private byte[] imgTestFile;
	private byte[] imgTestFile2;

	@BeforeEach
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
		communityId = communityRepository.create(community);
		communityId2 = communityRepository.create(community2);
	}

	@Test
	void shouldCreateProject() {
		//given
		ProjectEntity entityToSave = ProjectEntity.builder()
			.name("name")
			.communityId(communityId.id)
			.description("description")
			.logo(imgTestFile2, "jpg")
			.acronym("acronym")
			.researchField("researchField")
			.startTime(startTime)
			.endTime(endTime)
			.build();

		//when
		ProjectEntity saved = projectRepository.save(entityToSave);

		//then
		assertThat(projectRepository.findAll()).hasSize(1);
		Optional<ProjectEntity> byId = projectRepository.findById(saved.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().getCommunityId()).isEqualTo(communityId.id);
		assertThat(byId.get().getName()).isEqualTo("name");
		assertThat(byId.get().getAcronym()).isEqualTo("acronym");
		assertThat(byId.get().getResearchField()).isEqualTo("researchField");
		assertThat(byId.get().getDescription()).isEqualTo("description");
		assertThat(byId.get().getStartTime()).isEqualTo(startTime);
		assertThat(byId.get().getEndTime()).isEqualTo(endTime);
		assertThat(byId.get().getLogoImage()).isEqualTo(imgTestFile2);
		assertThat(byId.get().getLogoType()).isEqualTo("jpg");
	}

	@Test
	void shouldUpdateProject() {
		//given
		ProjectEntity old = ProjectEntity.builder()
			.name("name")
			.communityId(communityId.id)
			.description("description")
			.logo(imgTestFile, "jpg")
			.acronym("acronym")
			.researchField("researchField")
			.startTime(startTime)
			.endTime(endTime)
			.build();
		projectRepository.save(old);
		ProjectEntity toUpdate = ProjectEntity.builder()
			.name("new_name")
			.communityId(communityId2.id)
			.description("new_description")
			.logo(imgTestFile2, "jpg")
			.acronym("new_acronym")
			.researchField("new_researchField")
			.startTime(newStartTime)
			.endTime(newEndTime)
			.build();

		//when
		projectRepository.save(toUpdate);

		//then
		Optional<ProjectEntity> byId = projectRepository.findById(toUpdate.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().getCommunityId()).isEqualTo(communityId2.id);
		assertThat(byId.get().getName()).isEqualTo("new_name");
		assertThat(byId.get().getAcronym()).isEqualTo("new_acronym");
		assertThat(byId.get().getResearchField()).isEqualTo("new_researchField");
		assertThat(byId.get().getDescription()).isEqualTo("new_description");
		assertThat(byId.get().getStartTime()).isEqualTo(newStartTime);
		assertThat(byId.get().getEndTime()).isEqualTo(newEndTime);
		assertThat(byId.get().getLogoImage()).isEqualTo(imgTestFile2);
		assertThat(byId.get().getLogoType()).isEqualTo("jpg");
	}

	@Test
	void shouldFindCreatedProjects() {
		//given
		ProjectEntity toFind = ProjectEntity.builder()
			.name("name")
			.communityId(communityId.id)
			.description("description")
			.logo(imgTestFile, "jpg")
			.acronym("acronym")
			.researchField("researchField")
			.startTime(startTime)
			.endTime(endTime)
			.build();
		projectRepository.save(toFind);

		//when
		Optional<ProjectEntity> byId = projectRepository.findById(toFind.getId());

		//then
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindAllAvailableProjects() {
		//given
		projectRepository.save(ProjectEntity.builder()
			.name("name")
			.communityId(communityId.id)
			.description("description")
			.logo(imgTestFile, "jpg")
			.acronym("acronym")
			.researchField("researchField")
			.startTime(startTime)
			.endTime(endTime)
			.build()
		);
		projectRepository.save(ProjectEntity.builder()
			.name("new_name")
			.communityId(communityId2.id)
			.description("new_description")
			.logo(imgTestFile2, "jpg")
			.acronym("new_acronym")
			.researchField("new_researchField")
			.startTime(newStartTime)
			.endTime(newEndTime)
			.build()
		);

		//when
		Iterable<ProjectEntity> all = projectRepository.findAll();

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void savedProjectExistsByProjectId() {
		//given
		ProjectEntity site = projectRepository.save(ProjectEntity.builder()
			.communityId(communityId.id)
			.name("name")
			.description("description")
			.logo(imgTestFile, "jpg")
			.acronym("acronym")
			.researchField("research filed")
			.startTime(startTime)
			.endTime(endTime)
			.build());

		//when + then
		assertThat(projectRepository.existsById(site.getId())).isTrue();
		assertThat(projectRepository.existsById(generateId())).isFalse();
	}

	@Test
	void savedProjectExistsByName() {
		//given
		ProjectEntity site = projectRepository.save(ProjectEntity.builder()
			.name("name")
			.communityId(communityId.id)
			.description("description")
			.logo(imgTestFile, "jpg")
			.acronym("acronym")
			.researchField("researchField")
			.startTime(startTime)
			.endTime(endTime)
			.build());

		//when
		boolean exists = projectRepository.existsByCommunityIdAndName(communityId.id, site.getName());
		boolean nonExists = projectRepository.existsByCommunityIdAndName(communityId.id, "wrong_name");

		//then
		assertThat(exists).isTrue();
		assertThat(nonExists).isFalse();
	}

	@Test
	void shouldDeleteProject() {
		//given
		ProjectEntity entityToRemove = projectRepository.save(ProjectEntity.builder()
			.name("name")
			.communityId(communityId.id)
			.description("description")
			.logo(imgTestFile, "jpg")
			.acronym("acronym")
			.researchField("researchField")
			.startTime(startTime)
			.endTime(endTime)
			.build());

		//when
		projectRepository.deleteById(entityToRemove.getId());

		//then
		assertThat(projectRepository.findAll()).hasSize(0);
	}

	@Test
	void shouldDeleteAllProjects() {
		//given
		projectRepository.save(ProjectEntity.builder()
			.name("name")
			.communityId(communityId.id)
			.description("description")
			.logo(imgTestFile, "jpg")
			.acronym("acronym")
			.researchField("researchField")
			.startTime(startTime)
			.endTime(endTime)
			.build());
		projectRepository.save(ProjectEntity.builder()
			.name("new_name")
			.communityId(communityId2.id)
			.description("new_description")
			.logo(imgTestFile2, "jpg")
			.acronym("new_acronym")
			.researchField("new_researchField")
			.startTime(newStartTime)
			.endTime(newEndTime)
			.build());

		//when
		projectRepository.deleteAll();

		//then
		assertThat(projectRepository.findAll()).hasSize(0);
	}

}