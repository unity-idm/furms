/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.applications;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
class ApplicationEntityRepositoryTest extends DBIntegrationTest {

	@Autowired
	private ApplicationEntityRepository applicationEntityRepository;
	@Autowired
	private CommunityRepository communityRepository;
	@Autowired
	private ProjectRepository projectRepository;

	UUID projectId;
	UUID projectId2;

	@BeforeEach
	void init() {
		Community community = Community.builder()
			.name("name")
			.logo(FurmsImage.empty())
			.build();
		Community community2 = Community.builder()
			.name("name1")
			.logo(FurmsImage.empty())
			.build();
		UUID communityId = UUID.fromString(communityRepository.create(community));
		UUID communityId2 = UUID.fromString(communityRepository.create(community2));

		Project project = Project.builder()
			.communityId(communityId.toString())
			.name("name")
			.description("new_description")
			.logo(FurmsImage.empty())
			.acronym("acronym")
			.researchField("research filed")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();
		Project project2 = Project.builder()
			.communityId(communityId2.toString())
			.name("name2")
			.logo(FurmsImage.empty())
			.description("new_description")
			.acronym("acronym")
			.researchField("research filed")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();

		projectId = UUID.fromString(projectRepository.create(project));
		projectId2 = UUID.fromString(projectRepository.create(project2));
	}

	@Test
	void shouldCreate() {
		ApplicationEntity applicationEntity = new ApplicationEntity(null, projectId, "userId");
		ApplicationEntity savedApplication = applicationEntityRepository.save(applicationEntity);

		ApplicationEntity foundApplication = applicationEntityRepository.findById(savedApplication.getId()).get();
		assertEquals(savedApplication, foundApplication);
	}

	@Test
	void shouldRemove() {
		ApplicationEntity applicationEntity = new ApplicationEntity(null, projectId, "userId");
		ApplicationEntity savedApplication = applicationEntityRepository.save(applicationEntity);

		applicationEntityRepository.deleteById(savedApplication.getId());

		Optional<ApplicationEntity> foundApplication = applicationEntityRepository.findById(savedApplication.getId());
		assertTrue(foundApplication.isEmpty());
	}

	@Test
	void shouldFindAllByProjectId() {
		ApplicationEntity applicationEntity = new ApplicationEntity(null, projectId, "userId");
		ApplicationEntity savedApplication = applicationEntityRepository.save(applicationEntity);

		ApplicationEntity applicationEntity1 = new ApplicationEntity(null, projectId, "userId1");
		ApplicationEntity savedApplication1 = applicationEntityRepository.save(applicationEntity1);

		ApplicationEntity applicationEntity2 = new ApplicationEntity(null, projectId2, "userId");
		ApplicationEntity savedApplication2 = applicationEntityRepository.save(applicationEntity2);

		Set<ApplicationEntity> allByProjectId = applicationEntityRepository.findAllByProjectId(projectId);
		assertEquals(Set.of(savedApplication, savedApplication1), allByProjectId);
	}

	@Test
	void shouldFindAllByUserId() {
		ApplicationEntity applicationEntity = new ApplicationEntity(null, projectId, "userId");
		ApplicationEntity savedApplication = applicationEntityRepository.save(applicationEntity);

		ApplicationEntity applicationEntity1 = new ApplicationEntity(null, projectId2, "userId");
		ApplicationEntity savedApplication1 = applicationEntityRepository.save(applicationEntity1);

		ApplicationEntity applicationEntity2 = new ApplicationEntity(null, projectId, "userId1");
		ApplicationEntity savedApplication2 = applicationEntityRepository.save(applicationEntity2);

		Set<ApplicationEntity> allByProjectId = applicationEntityRepository.findAllByUserId("userId");
		assertEquals(Set.of(savedApplication, savedApplication1), allByProjectId);
	}

	@Test
	void shouldDeleteByProjectIdAndUserId(){
		ApplicationEntity applicationEntity = new ApplicationEntity(null, projectId, "userId");
		ApplicationEntity savedApplication = applicationEntityRepository.save(applicationEntity);
		applicationEntityRepository.deleteByProjectIdAndUserId(savedApplication.projectId, savedApplication.userId);

		Optional<ApplicationEntity> foundApplication = applicationEntityRepository.findById(savedApplication.getId());
		assertTrue(foundApplication.isEmpty());
	}

	@Test
	void shouldExistByProjectIdAndUserId(){
		ApplicationEntity applicationEntity = new ApplicationEntity(null, projectId, "userId");
		ApplicationEntity savedApplication = applicationEntityRepository.save(applicationEntity);

		boolean existApplication = applicationEntityRepository.existsByProjectIdAndUserId(savedApplication.projectId, savedApplication.userId);
		assertTrue(existApplication);
	}

	@Test
	void shouldNotExistByProjectIdAndUserId(){
		ApplicationEntity applicationEntity = new ApplicationEntity(null, projectId, "userId1");
		ApplicationEntity savedApplication = applicationEntityRepository.save(applicationEntity);

		boolean existApplication = applicationEntityRepository.existsByProjectIdAndUserId(savedApplication.projectId, "userId");
		assertFalse(existApplication);
	}
}