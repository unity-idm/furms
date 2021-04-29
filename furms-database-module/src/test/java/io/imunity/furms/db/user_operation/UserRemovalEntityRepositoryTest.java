/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.user_operation;

import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.user_operation.UserAdditionStatus;
import io.imunity.furms.domain.user_operation.UserRemovalStatus;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserRemovalEntityRepositoryTest extends DBIntegrationTest {

	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private CommunityRepository communityRepository;
	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private UserAdditionEntityRepository userAdditionEntityRepository;
	@Autowired
	private UserRemovalEntityRepository userRemovalEntityRepository;

	private UUID siteId;
	private UUID projectId;
	private UUID userAdditionalId;

	@BeforeEach
	void init() throws IOException {
		Site site = Site.builder()
			.name("name")
			.build();
		siteId = UUID.fromString(siteRepository.create(site, new SiteExternalId("id")));

		Community community = Community.builder()
			.name("name")
			.logo(FurmsImage.empty())
			.build();
		UUID communityId = UUID.fromString(communityRepository.create(community));

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

		projectId = UUID.fromString(projectRepository.create(project));

		userAdditionalId = userAdditionEntityRepository.save(
			UserAdditionSaveEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.correlationId(UUID.randomUUID())
				.uid("uid")
				.userId("userId")
				.status(UserAdditionStatus.PENDING)
				.build()
		).getId();
	}

	@AfterEach
	void clean(){
		userAdditionEntityRepository.deleteAll();
	}

	@Test
	void shouldCreate(){
		UserRemovalSaveEntity userAdditionSaveEntity = userRemovalEntityRepository.save(
			UserRemovalSaveEntity.builder()
				.siteId(siteId)
				.userAdditionId(userAdditionalId)
				.projectId(projectId)
				.correlationId(UUID.randomUUID())
				.uid("uid")
				.userId("userId")
				.status(UserRemovalStatus.PENDING)
				.build()
		);

		Optional<UserRemovalSaveEntity> byId = userRemovalEntityRepository.findById(userAdditionSaveEntity.getId());
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindByCorrelation(){
		UserRemovalSaveEntity userAdditionSaveEntity = userRemovalEntityRepository.save(
			UserRemovalSaveEntity.builder()
				.siteId(siteId)
				.userAdditionId(userAdditionalId)
				.projectId(projectId)
				.correlationId(UUID.randomUUID())
				.uid("uid")
				.userId("userId")
				.status(UserRemovalStatus.REMOVED)
				.build()
		);

		Optional<UserRemovalSaveEntity> byId = userRemovalEntityRepository.findByCorrelationId(userAdditionSaveEntity.correlationId);
		assertThat(byId).isPresent();
	}

	@Test
	void shouldDelete(){
		UserRemovalSaveEntity userAdditionSaveEntity = userRemovalEntityRepository.save(
			UserRemovalSaveEntity.builder()
				.siteId(siteId)
				.userAdditionId(userAdditionalId)
				.projectId(projectId)
				.correlationId(UUID.randomUUID())
				.uid("uid")
				.userId("userId")
				.status(UserRemovalStatus.REMOVED)
				.build()
		);

		userRemovalEntityRepository.deleteById(userAdditionSaveEntity.getId());
		Optional<UserRemovalSaveEntity> byId = userRemovalEntityRepository.findById(userAdditionSaveEntity.getId());

		assertThat(byId).isEmpty();
	}

	@Test
	void shouldUpdate(){
		UserRemovalSaveEntity userAdditionSaveEntity = userRemovalEntityRepository.save(
			UserRemovalSaveEntity.builder()
				.siteId(siteId)
				.userAdditionId(userAdditionalId)
				.projectId(projectId)
				.correlationId(UUID.randomUUID())
				.uid("uid")
				.userId("userId")
				.status(UserRemovalStatus.PENDING)
				.build()
		);
		userRemovalEntityRepository.save(
			UserRemovalSaveEntity.builder()
				.id(userAdditionSaveEntity.getId())
				.siteId(siteId)
				.userAdditionId(userAdditionalId)
				.projectId(projectId)
				.correlationId(UUID.randomUUID())
				.uid("uid")
				.userId("userId")
				.status(UserRemovalStatus.REMOVED)
				.build()
		);
		Optional<UserRemovalSaveEntity> byId = userRemovalEntityRepository.findById(userAdditionSaveEntity.getId());

		assertThat(byId).isPresent();
		assertThat(byId.get().status).isEqualTo(UserRemovalStatus.REMOVED);
	}
}