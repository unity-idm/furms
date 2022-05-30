/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.applications;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

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

	ProjectId projectId;
	ProjectId projectId2;

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
		CommunityId communityId = communityRepository.create(community);
		CommunityId communityId2 = communityRepository.create(community2);

		Project project = Project.builder()
			.communityId(communityId)
			.name("name")
			.description("new_description")
			.logo(FurmsImage.empty())
			.acronym("acronym")
			.researchField("research filed")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();
		Project project2 = Project.builder()
			.communityId(communityId2)
			.name("name2")
			.logo(FurmsImage.empty())
			.description("new_description")
			.acronym("acronym")
			.researchField("research filed")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();

		projectId = projectRepository.create(project);
		projectId2 = projectRepository.create(project2);
	}

	@Test
	void shouldReturnCreatedApplication() {
		ApplicationEntity applicationEntity = new ApplicationEntity(null, projectId.id, "userId");
		ApplicationEntity savedApplication = applicationEntityRepository.save(applicationEntity);

		ApplicationEntity foundApplication = applicationEntityRepository.findById(savedApplication.getId()).get();
		assertEquals(savedApplication, foundApplication);
	}

	@Test
	void shouldRemove() {
		ApplicationEntity applicationEntity = new ApplicationEntity(null, projectId.id, "userId");
		ApplicationEntity savedApplication = applicationEntityRepository.save(applicationEntity);

		applicationEntityRepository.deleteById(savedApplication.getId());

		Optional<ApplicationEntity> foundApplication = applicationEntityRepository.findById(savedApplication.getId());
		assertTrue(foundApplication.isEmpty());
	}

	@Test
	void shouldFindAllByProjectId() {
		ApplicationEntity applicationEntity = new ApplicationEntity(null, projectId.id, "userId");
		ApplicationEntity savedApplication = applicationEntityRepository.save(applicationEntity);

		ApplicationEntity applicationEntity1 = new ApplicationEntity(null, projectId.id, "userId1");
		ApplicationEntity savedApplication1 = applicationEntityRepository.save(applicationEntity1);

		ApplicationEntity applicationEntity2 = new ApplicationEntity(null, projectId2.id, "userId");
		applicationEntityRepository.save(applicationEntity2);

		Set<ApplicationEntity> allByProjectId = applicationEntityRepository.findAllByProjectId(projectId.id);
		assertEquals(Set.of(savedApplication, savedApplication1), allByProjectId);
	}

	@Test
	void shouldFindAllByUserId() {
		ApplicationEntity applicationEntity = new ApplicationEntity(null, projectId.id, "userId");
		ApplicationEntity savedApplication = applicationEntityRepository.save(applicationEntity);

		ApplicationEntity applicationEntity1 = new ApplicationEntity(null, projectId2.id, "userId");
		ApplicationEntity savedApplication1 = applicationEntityRepository.save(applicationEntity1);

		ApplicationEntity applicationEntity2 = new ApplicationEntity(null, projectId.id, "userId1");
		applicationEntityRepository.save(applicationEntity2);

		Set<ApplicationEntity> allByProjectId = applicationEntityRepository.findAllByUserId("userId");
		assertEquals(Set.of(savedApplication, savedApplication1), allByProjectId);
	}

	@Test
	void shouldDeleteByProjectIdAndUserId(){
		ApplicationEntity applicationEntity = new ApplicationEntity(null, projectId.id, "userId");
		ApplicationEntity savedApplication = applicationEntityRepository.save(applicationEntity);
		applicationEntityRepository.deleteByProjectIdAndUserId(savedApplication.projectId, savedApplication.userId);

		Optional<ApplicationEntity> foundApplication = applicationEntityRepository.findById(savedApplication.getId());
		assertTrue(foundApplication.isEmpty());
	}

	@Test
	void shouldExistByProjectIdAndUserId(){
		ApplicationEntity applicationEntity = new ApplicationEntity(null, projectId.id, "userId");
		ApplicationEntity savedApplication = applicationEntityRepository.save(applicationEntity);

		boolean applicationExists = applicationEntityRepository.existsByProjectIdAndUserId(savedApplication.projectId, savedApplication.userId);
		assertTrue(applicationExists);
	}

	@Test
	void shouldNotExistByProjectIdAndUserId(){
		ApplicationEntity applicationEntity = new ApplicationEntity(null, projectId.id, "userId1");
		ApplicationEntity savedApplication = applicationEntityRepository.save(applicationEntity);

		boolean applicationExists = applicationEntityRepository.existsByProjectIdAndUserId(savedApplication.projectId, "userId");
		assertFalse(applicationExists);
	}
}