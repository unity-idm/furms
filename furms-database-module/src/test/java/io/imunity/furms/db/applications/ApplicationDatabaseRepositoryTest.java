/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.applications;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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

	UUID projectId;
	UUID projectId2;

	@BeforeEach
	void init() throws IOException {
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
	void shouldFindAllApplyingUsers(){
		ApplicationEntity applicationEntity = new ApplicationEntity(null, projectId, "userId");
		ApplicationEntity savedApplication = applicationEntityRepository.save(applicationEntity);

		ApplicationEntity applicationEntity1 = new ApplicationEntity(null, projectId, "userId1");
		ApplicationEntity savedApplication1 = applicationEntityRepository.save(applicationEntity1);

		ApplicationEntity applicationEntity2 = new ApplicationEntity(null, projectId2, "userId");
		ApplicationEntity savedApplication2 = applicationEntityRepository.save(applicationEntity2);

		Set<FenixUserId> allApplyingUsers = applicationDatabaseRepository.findAllApplyingUsers(projectId.toString());

		assertEquals(Set.of(new FenixUserId("userId"), new FenixUserId("userId1")), allApplyingUsers);
	}

	@Test
	void shouldFindAllAppliedProjectsIds(){
		ApplicationEntity applicationEntity = new ApplicationEntity(null, projectId, "userId");
		ApplicationEntity savedApplication = applicationEntityRepository.save(applicationEntity);

		ApplicationEntity applicationEntity1 = new ApplicationEntity(null, projectId, "userId1");
		ApplicationEntity savedApplication1 = applicationEntityRepository.save(applicationEntity1);

		ApplicationEntity applicationEntity2 = new ApplicationEntity(null, projectId2, "userId");
		ApplicationEntity savedApplication2 = applicationEntityRepository.save(applicationEntity2);

		Set<String> allAppliedProjectsIds = applicationDatabaseRepository.findAllAppliedProjectsIds(new FenixUserId("userId"));

		assertEquals(Set.of(projectId.toString(), projectId2.toString()), allAppliedProjectsIds);
	}

	@Test
	void shouldCreate(){
		FenixUserId userId = new FenixUserId("userId");
		applicationDatabaseRepository.create(projectId.toString(), userId);

		boolean exists = applicationEntityRepository.existsByProjectIdAndUserId(projectId, "userId");
		assertTrue(exists);
	}

	@Test
	void shouldRemove(){
		ApplicationEntity applicationEntity = new ApplicationEntity(null, projectId, "userId");
		ApplicationEntity savedApplication = applicationEntityRepository.save(applicationEntity);

		applicationDatabaseRepository.remove(projectId.toString(), new FenixUserId("userId"));

		Optional<ApplicationEntity> foundApplication = applicationEntityRepository.findById(savedApplication.getId());
		assertTrue(foundApplication.isEmpty());
	}

	@Test
	void shouldExistBy(){
		ApplicationEntity applicationEntity = new ApplicationEntity(null, projectId, "userId");
		ApplicationEntity savedApplication = applicationEntityRepository.save(applicationEntity);

		boolean exists = applicationDatabaseRepository.existsBy(projectId.toString(), new FenixUserId("userId"));
		assertTrue(exists);
	}

	@Test
	void shouldNotExistBy(){
		ApplicationEntity applicationEntity = new ApplicationEntity(null, projectId, "userId");
		ApplicationEntity savedApplication = applicationEntityRepository.save(applicationEntity);

		boolean exists = applicationDatabaseRepository.existsBy(projectId2.toString(), new FenixUserId("userId"));
		assertFalse(exists);
	}
}