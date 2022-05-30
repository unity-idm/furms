/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.user_operation;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserAdditionId;
import io.imunity.furms.domain.user_operation.UserAdditionJob;
import io.imunity.furms.domain.user_operation.UserAdditionWithProject;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserOperationDatabaseRepositoryTest extends DBIntegrationTest {

	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private CommunityRepository communityRepository;
	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private UserAdditionEntityRepository userAdditionEntityRepository;
	@Autowired
	private UserAdditionJobEntityRepository userAdditionJobEntityRepository;
	@Autowired
	private UserOperationDatabaseRepository userOperationDatabaseRepository;

	private SiteId siteId;
	private ProjectId projectId;

	@BeforeEach
	void init() {
		Site site = Site.builder()
			.name("name")
			.build();
		siteId = siteRepository.create(site, new SiteExternalId("id"));

		Community community = Community.builder()
			.name("name")
			.logo(FurmsImage.empty())
			.build();
		CommunityId communityId = communityRepository.create(community);

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

		projectId = projectRepository.create(project);
	}

	@Test
	void shouldCreateUserAddition() {
		CorrelationId correlationId = CorrelationId.randomID();
		UserAddition userAddition = UserAddition.builder()
			.siteId(new SiteId(siteId.id.toString(), new SiteExternalId("id")))
			.projectId(projectId)
			.correlationId(correlationId)
			.userId("userId")
			.status(UserStatus.ADDING_ACKNOWLEDGED)
			.build();

		UserAdditionId id = userOperationDatabaseRepository.create(userAddition);

		Optional<UserAdditionSaveEntity> byId = userAdditionEntityRepository.findById(id.id);
		Optional<UserAdditionJobEntity> byIdJob = userAdditionJobEntityRepository.findByCorrelationId(UUID.fromString(correlationId.id));
		assertThat(byId).isPresent();
		assertThat(byId.get().siteId).isEqualTo(userAddition.siteId.id);
		assertThat(byId.get().projectId).isEqualTo(userAddition.projectId.id);
		assertThat(byIdJob.get().status).isEqualTo(UserStatus.ADDING_ACKNOWLEDGED.getPersistentId());
	}

	@Test
	void shouldRemoveUserAdditionByCorrelationId() {
		CorrelationId correlationId = CorrelationId.randomID();
		UserAddition userAddition = UserAddition.builder()
			.siteId(new SiteId(siteId.id.toString(), new SiteExternalId("id")))
			.projectId(projectId)
			.correlationId(correlationId)
			.userId("userId")
			.status(UserStatus.ADDING_ACKNOWLEDGED)
			.build();

		UserAdditionId id = userOperationDatabaseRepository.create(userAddition);

		userOperationDatabaseRepository.deleteByCorrelationId(correlationId);

		assertThat(userAdditionEntityRepository.findById(id.id)).isEmpty();
	}

	@Test
	void shouldFindAllWithRelatedSiteAndProjectBySiteIdAndUserId() {
		CorrelationId correlationId = CorrelationId.randomID();
		UserAddition userAddition = UserAddition.builder()
				.siteId(new SiteId(siteId.id.toString(), new SiteExternalId("id")))
				.projectId(projectId)
				.correlationId(correlationId)
				.userId("userId")
				.status(UserStatus.ADDING_ACKNOWLEDGED)
				.build();

		userOperationDatabaseRepository.create(userAddition);

		final Set<UserAdditionWithProject> userAdditions = userOperationDatabaseRepository
				.findAllUserAdditionsWithSiteAndProjectBySiteId(new FenixUserId("userId"), userAddition.siteId);
		assertThat(userAdditions).hasSize(1);
		assertThat(userAdditions.stream().findFirst().get().getStatus()).isEqualTo(UserStatus.ADDING_ACKNOWLEDGED);
	}

	@Test
	void shouldUpdateJob() {
		UserAdditionSaveEntity userAdditionSaveEntity = userAdditionEntityRepository.save(
			UserAdditionSaveEntity.builder()
				.siteId(siteId.id)
				.projectId(projectId.id)
				.userId("userId")
				.build()
		);
		UserAdditionJobEntity save = userAdditionJobEntityRepository.save(UserAdditionJobEntity.builder()
			.userAdditionId(userAdditionSaveEntity.getId())
			.status(UserStatus.ADDING_ACKNOWLEDGED)
			.correlationId(UUID.randomUUID())
			.build()
		);

		UserAdditionJob job = UserAdditionJob.builder()
			.userAdditionId(userAdditionSaveEntity.getId().toString())
			.status(UserStatus.ADDED)
			.correlationId(CorrelationId.randomID())
			.build();

		userOperationDatabaseRepository.update(job);

		Optional<UserAdditionJobEntity> byId = userAdditionJobEntityRepository.findById(save.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().correlationId.toString()).isEqualTo(job.correlationId.id);
		assertThat(byId.get().status).isEqualTo(job.status.getPersistentId());
	}

	@Test
	void shouldUpdateUserAddition() {
		UserAdditionSaveEntity userAdditionSaveEntity = userAdditionEntityRepository.save(
			UserAdditionSaveEntity.builder()
				.siteId(siteId.id)
				.projectId(projectId.id)
				.userId("id")
				.build()
		);
		UserAdditionJobEntity save = userAdditionJobEntityRepository.save(
			UserAdditionJobEntity.builder()
				.correlationId(UUID.randomUUID())
				.status(UserStatus.ADDING_ACKNOWLEDGED)
				.userAdditionId(userAdditionSaveEntity.getId())
				.build()
		);

		CorrelationId correlationId = CorrelationId.randomID();
		userOperationDatabaseRepository.update(
			UserAddition.builder()
				.id(userAdditionSaveEntity.getId().toString())
				.correlationId(correlationId)
				.status(UserStatus.ADDING_ACKNOWLEDGED)
				.build()
		);

		Optional<UserAdditionSaveEntity> byId = userAdditionEntityRepository.findById(UUID.fromString(userAdditionSaveEntity.getId().toString()));
		Optional<UserAdditionJobEntity> byIdJob = userAdditionJobEntityRepository.findById(save.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().siteId.toString()).isEqualTo(userAdditionSaveEntity.siteId.toString());
		assertThat(byId.get().projectId.toString()).isEqualTo(userAdditionSaveEntity.projectId.toString());
		assertThat(byIdJob.get().status).isEqualTo(UserStatus.ADDING_ACKNOWLEDGED.getPersistentId());
	}

	@Test
	void shouldUpdateUserStatus() {
		CorrelationId correlationId = CorrelationId.randomID();
		UserAdditionSaveEntity userAdditionSaveEntity = userAdditionEntityRepository.save(
			UserAdditionSaveEntity.builder()
				.siteId(siteId.id)
				.projectId(projectId.id)
				.userId("id")
				.build()
		);
		userAdditionJobEntityRepository.save(
			UserAdditionJobEntity.builder()
				.correlationId(UUID.fromString(correlationId.id))
				.userAdditionId(userAdditionSaveEntity.getId())
				.status(UserStatus.ADDING_ACKNOWLEDGED)
				.build()
		);

		userOperationDatabaseRepository.updateStatus(correlationId, UserStatus.ADDED, Optional.empty());

		Optional<UserAdditionJobEntity> byId = userAdditionJobEntityRepository.findByCorrelationId(UUID.fromString(correlationId.id));
		assertThat(byId).isPresent();
		assertThat(byId.get().status).isEqualTo(UserStatus.ADDED.getPersistentId());
		assertThat(byId.get().message).isEqualTo(null);
	}
}