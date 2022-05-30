/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.applications;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.applications.ProjectApplication;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ApplicationDatabaseRepositoryTest extends DBIntegrationTest {

	@Autowired
	private CommunityRepository communityRepository;
	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ApplicationEntityRepository applicationEntityRepository;
	@Autowired
	private ApplicationDatabaseRepository applicationDatabaseRepository;

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
	void shouldFindAllApplyingUsers(){
		ApplicationEntity applicationEntity = new ApplicationEntity(null, projectId.id, "userId");
		applicationEntityRepository.save(applicationEntity);

		ApplicationEntity applicationEntity1 = new ApplicationEntity(null, projectId.id, "userId1");
		applicationEntityRepository.save(applicationEntity1);

		ApplicationEntity applicationEntity2 = new ApplicationEntity(null, projectId2.id, "userId");
		applicationEntityRepository.save(applicationEntity2);

		Set<FenixUserId> allApplyingUsers = applicationDatabaseRepository.findAllApplyingUsers(projectId);

		assertEquals(Set.of(new FenixUserId("userId"), new FenixUserId("userId1")), allApplyingUsers);
	}

	@Test
	void shouldFindAllApplyingUsersByProjectIds(){
		ApplicationEntity applicationEntity = new ApplicationEntity(null, projectId.id, "userId");
		applicationEntityRepository.save(applicationEntity);

		ApplicationEntity applicationEntity1 = new ApplicationEntity(null, projectId.id, "userId1");
		applicationEntityRepository.save(applicationEntity1);

		ApplicationEntity applicationEntity2 = new ApplicationEntity(null, projectId2.id, "userId2");
		applicationEntityRepository.save(applicationEntity2);

		Set<ProjectApplication> allApplyingUsers = applicationDatabaseRepository.findAllApplyingUsers(List.of(projectId, projectId2));

		assertEquals(Set.of(
			new ProjectApplication(projectId.id.toString(), "name", new FenixUserId("userId")),
			new ProjectApplication(projectId.id.toString(), "name", new FenixUserId("userId1")),
			new ProjectApplication(projectId2.id.toString(), "name2", new FenixUserId("userId2"))
			),
			allApplyingUsers
		);
	}

	@Test
	void shouldFindAllAppliedProjectsIds(){
		ApplicationEntity applicationEntity = new ApplicationEntity(null, projectId.id, "userId");
		applicationEntityRepository.save(applicationEntity);

		ApplicationEntity applicationEntity1 = new ApplicationEntity(null, projectId.id, "userId1");
		applicationEntityRepository.save(applicationEntity1);

		ApplicationEntity applicationEntity2 = new ApplicationEntity(null, projectId2.id, "userId");
		applicationEntityRepository.save(applicationEntity2);

		Set<ProjectId> allAppliedProjectsIds = applicationDatabaseRepository.findAllAppliedProjectsIds(new FenixUserId("userId"));

		assertEquals(Set.of(projectId, projectId2), allAppliedProjectsIds);
	}

	@Test
	void shouldCreate(){
		FenixUserId userId = new FenixUserId("userId");
		applicationDatabaseRepository.create(projectId, userId);

		boolean exists = applicationEntityRepository.existsByProjectIdAndUserId(projectId.id, "userId");
		assertTrue(exists);
	}

	@Test
	void shouldRemove(){
		ApplicationEntity applicationEntity = new ApplicationEntity(null, projectId.id, "userId");
		ApplicationEntity savedApplication = applicationEntityRepository.save(applicationEntity);

		applicationDatabaseRepository.remove(projectId, new FenixUserId("userId"));

		Optional<ApplicationEntity> foundApplication = applicationEntityRepository.findById(savedApplication.getId());
		assertTrue(foundApplication.isEmpty());
	}

	@Test
	void shouldExistBy(){
		ApplicationEntity applicationEntity = new ApplicationEntity(null, projectId.id, "userId");
		applicationEntityRepository.save(applicationEntity);

		boolean exists = applicationDatabaseRepository.existsBy(projectId, new FenixUserId("userId"));
		assertTrue(exists);
	}

	@Test
	void shouldNotExistBy(){
		ApplicationEntity applicationEntity = new ApplicationEntity(null, projectId.id, "userId");
		applicationEntityRepository.save(applicationEntity);

		boolean exists = applicationDatabaseRepository.existsBy(projectId2, new FenixUserId("userId"));
		assertFalse(exists);
	}
}