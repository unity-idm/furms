/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.user_operation;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserAdditionStatus;
import io.imunity.furms.domain.user_operation.UserRemoval;
import io.imunity.furms.domain.user_operation.UserRemovalStatus;
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
	private UserRemovalEntityRepository userRemovalEntityRepository;
	@Autowired
	private UserOperationDatabaseRepository userOperationDatabaseRepository;

	private UUID siteId;
	private UUID projectId;
	private UUID userAdditionalId;

	@BeforeEach
	void init() {
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
	}

	@Test
	void shouldFindAllUserAdditions() {
		userAdditionEntityRepository.save(
			UserAdditionSaveEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.correlationId(UUID.randomUUID())
				.uid("uid")
				.userId("userId")
				.status(UserAdditionStatus.PENDING)
				.build()
		);

		userAdditionEntityRepository.save(
			UserAdditionSaveEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.correlationId(UUID.randomUUID())
				.uid("uid")
				.userId("userId")
				.status(UserAdditionStatus.ACKNOWLEDGED)
				.build()
		);

		Set<UserAddition> allUserAdditions = userOperationDatabaseRepository.findAllUserAdditions(projectId.toString(), "userId");
		assertThat(allUserAdditions.size()).isEqualTo(2);
	}

	@Test
	void shouldCreateUserAddition() {
		UserAddition userAddition = UserAddition.builder()
			.siteId(new SiteId(siteId.toString(), new SiteExternalId("id")))
			.projectId(projectId.toString())
			.correlationId(CorrelationId.randomID())
			.uid("uid")
			.userId("userId")
			.status(UserAdditionStatus.ACKNOWLEDGED)
			.build();

		String id = userOperationDatabaseRepository.create(userAddition);

		Optional<UserAdditionSaveEntity> byId = userAdditionEntityRepository.findById(UUID.fromString(id));
		assertThat(byId).isPresent();
		assertThat(byId.get().siteId.toString()).isEqualTo(userAddition.siteId.id);
		assertThat(byId.get().projectId.toString()).isEqualTo(userAddition.projectId);
		assertThat(byId.get().correlationId.toString()).isEqualTo(userAddition.correlationId.id);
		assertThat(byId.get().uid).isEqualTo(userAddition.uid);
		assertThat(byId.get().status).isEqualTo(userAddition.status.getPersistentId());
	}

	@Test
	void shouldCreateUserRemoval() {
		UserAdditionSaveEntity userAdditionSaveEntity = userAdditionEntityRepository.save(
			UserAdditionSaveEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.correlationId(UUID.randomUUID())
				.uid("uid")
				.userId("userId")
				.status(UserAdditionStatus.ACKNOWLEDGED)
				.build()
		);

		UserRemoval userRemoval = UserRemoval.builder()
			.siteId(new SiteId(siteId.toString(), new SiteExternalId("id")))
			.projectId(projectId.toString())
			.correlationId(CorrelationId.randomID())
			.userAdditionId(userAdditionSaveEntity.getId().toString())
			.uid("uid")
			.userId("userId")
			.status(UserRemovalStatus.ACKNOWLEDGED)
			.build();

		String id = userOperationDatabaseRepository.create(userRemoval);

		Optional<UserRemovalSaveEntity> byId = userRemovalEntityRepository.findById(UUID.fromString(id));
		assertThat(byId).isPresent();
		assertThat(byId.get().siteId.toString()).isEqualTo(userRemoval.siteId.id);
		assertThat(byId.get().projectId.toString()).isEqualTo(userRemoval.projectId);
		assertThat(byId.get().correlationId.toString()).isEqualTo(userRemoval.correlationId.id);
		assertThat(byId.get().uid).isEqualTo(userRemoval.uid);
		assertThat(byId.get().status).isEqualTo(userRemoval.status.getPersistentId());
	}

	@Test
	void shouldUpdateUserAddition() {
		UserAdditionSaveEntity userAdditionSaveEntity = userAdditionEntityRepository.save(
			UserAdditionSaveEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.userId("id")
				.correlationId(UUID.randomUUID())
				.status(UserAdditionStatus.PENDING)
				.build()
		);

		userOperationDatabaseRepository.update(
			UserAddition.builder()
				.correlationId(new CorrelationId(userAdditionSaveEntity.correlationId.toString()))
				.status(UserAdditionStatus.ACKNOWLEDGED)
				.uid("uid")
				.build()
		);

		Optional<UserAdditionSaveEntity> byId = userAdditionEntityRepository.findById(UUID.fromString(userAdditionSaveEntity.getId().toString()));
		assertThat(byId).isPresent();
		assertThat(byId.get().siteId.toString()).isEqualTo(userAdditionSaveEntity.siteId.toString());
		assertThat(byId.get().projectId.toString()).isEqualTo(userAdditionSaveEntity.projectId.toString());
		assertThat(byId.get().correlationId.toString()).isEqualTo(userAdditionSaveEntity.correlationId.toString());
		assertThat(byId.get().uid).isEqualTo("uid");
		assertThat(byId.get().status).isEqualTo(UserAdditionStatus.ACKNOWLEDGED.getPersistentId());
	}

	@Test
	void shouldUpdateUserAdditionStatus() {
		UserAdditionSaveEntity userAdditionSaveEntity = userAdditionEntityRepository.save(
			UserAdditionSaveEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.userId("id")
				.correlationId(UUID.randomUUID())
				.status(UserAdditionStatus.PENDING)
				.build()
		);

		userOperationDatabaseRepository.updateStatus(new CorrelationId(userAdditionSaveEntity.correlationId.toString()), UserAdditionStatus.ADDED);

		Optional<UserAdditionSaveEntity> byId = userAdditionEntityRepository.findById(UUID.fromString(userAdditionSaveEntity.getId().toString()));
		assertThat(byId).isPresent();
		assertThat(byId.get().status).isEqualTo(UserAdditionStatus.ADDED.getPersistentId());
	}

	@Test
	void shouldUpdateUserRemovalStatus() {
		UserAdditionSaveEntity userAdditionSaveEntity = userAdditionEntityRepository.save(
			UserAdditionSaveEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.correlationId(UUID.randomUUID())
				.uid("uid")
				.userId("userId")
				.status(UserAdditionStatus.ACKNOWLEDGED)
				.build()
		);

		UserRemovalSaveEntity userRemovalSaveEntity = userRemovalEntityRepository.save(
			UserRemovalSaveEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.correlationId(UUID.randomUUID())
				.userAdditionId(userAdditionSaveEntity.getId())
				.uid("uid")
				.userId("userId")
				.status(UserRemovalStatus.ACKNOWLEDGED)
				.build()
		);

		userOperationDatabaseRepository.updateStatus(new CorrelationId(userRemovalSaveEntity.correlationId.toString()), UserRemovalStatus.REMOVED);

		Optional<UserRemovalSaveEntity> byId = userRemovalEntityRepository.findById(UUID.fromString(userRemovalSaveEntity.getId().toString()));
		assertThat(byId).isPresent();
		assertThat(byId.get().status).isEqualTo(UserRemovalStatus.REMOVED.getPersistentId());
	}
}