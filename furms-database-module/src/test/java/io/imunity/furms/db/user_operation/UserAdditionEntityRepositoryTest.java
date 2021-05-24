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
import io.imunity.furms.domain.user_operation.UserStatus;
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
class UserAdditionEntityRepositoryTest extends DBIntegrationTest {

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

	private UUID siteId;
	private UUID projectId;

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
	void shouldFindAllByProjectIdAndUserIdWithRelatedSite(){
		UserAdditionSaveEntity userAddition = userAdditionEntityRepository.save(
			UserAdditionSaveEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.userId("userId")
				.build()
		);
		userAdditionJobEntityRepository.save(
			UserAdditionJobEntity.builder()
				.correlationId(UUID.randomUUID())
				.userAdditionId(userAddition.getId())
				.status(UserStatus.REMOVAL_PENDING)
				.build()
		);

		UserAdditionSaveEntity userAddition2 = userAdditionEntityRepository.save(
			UserAdditionSaveEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.userId("userId2")
				.build()
		);
		userAdditionJobEntityRepository.save(
			UserAdditionJobEntity.builder()
				.correlationId(UUID.randomUUID())
				.userAdditionId(userAddition2.getId())
				.status(UserStatus.REMOVAL_PENDING)
				.build()
		);

		Set<UserAdditionReadEntity> userAdditions = userAdditionEntityRepository.findAllByProjectIdAndUserId(projectId, "userId");
		assertThat(userAdditions.size()).isEqualTo(1);
		assertThat(userAdditions.iterator().next().status).isEqualTo(UserStatus.REMOVAL_PENDING.getPersistentId());
		assertThat(userAdditions.iterator().next().site.getExternalId()).isEqualTo("id");
		assertThat(userAdditions.iterator().next().site.getId()).isEqualTo(siteId);
	}

	@Test
	void shouldFindAllBySiteIdAndUserIdWithRelatedSiteProjectInfo(){
		UserAdditionSaveEntity userAddition = userAdditionEntityRepository.save(
				UserAdditionSaveEntity.builder()
						.siteId(siteId)
						.projectId(projectId)
						.userId("userId")
						.build()
		);
		userAdditionJobEntityRepository.save(
				UserAdditionJobEntity.builder()
						.correlationId(UUID.randomUUID())
						.userAdditionId(userAddition.getId())
						.status(UserStatus.REMOVAL_PENDING)
						.build()
		);

		UserAdditionSaveEntity userAddition2 = userAdditionEntityRepository.save(
				UserAdditionSaveEntity.builder()
						.siteId(siteId)
						.projectId(projectId)
						.userId("userId2")
						.build()
		);
		userAdditionJobEntityRepository.save(
				UserAdditionJobEntity.builder()
						.correlationId(UUID.randomUUID())
						.userAdditionId(userAddition2.getId())
						.status(UserStatus.REMOVAL_PENDING)
						.build()
		);

		final Set<UserAdditionReadWithProjectsEntity> userAdditions = userAdditionEntityRepository.findAllWithSiteAndProjectsBySiteIdAndUserId(siteId, "userId");
		assertThat(userAdditions.size()).isEqualTo(1);
		assertThat(userAdditions.iterator().next().status).isEqualTo(UserStatus.REMOVAL_PENDING.getPersistentId());
		assertThat(userAdditions.iterator().next().siteName).isEqualTo("name");
		assertThat(userAdditions.iterator().next().projectName).isEqualTo("name");
	}

	@Test
	void shouldConfirmUserAddition() {
		UserAdditionSaveEntity userAdditionSaveEntity = userAdditionEntityRepository.save(
			UserAdditionSaveEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.userId("userId")
				.build()
		);

		userAdditionJobEntityRepository.save(
			UserAdditionJobEntity.builder()
				.correlationId(UUID.randomUUID())
				.userAdditionId(userAdditionSaveEntity.getId())
				.status(UserStatus.REMOVAL_PENDING)
				.build()
		);

		boolean added = userAdditionEntityRepository.existsBySiteIdAndUserId(siteId, "userId");
		assertThat(added).isTrue();
	}

	@Test
	void shouldCreate(){
		UserAdditionSaveEntity userAdditionSaveEntity = userAdditionEntityRepository.save(
			UserAdditionSaveEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.userId("userId")
				.build()
		);

		Optional<UserAdditionSaveEntity> byId = userAdditionEntityRepository.findById(userAdditionSaveEntity.getId());
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindByCorrelation(){
		UserAdditionSaveEntity userAdditionSaveEntity = userAdditionEntityRepository.save(
			UserAdditionSaveEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.userId("userId")
				.build()
		);
		UserAdditionJobEntity save = userAdditionJobEntityRepository.save(
			UserAdditionJobEntity.builder()
				.correlationId(UUID.randomUUID())
				.userAdditionId(userAdditionSaveEntity.getId())
				.status(UserStatus.REMOVAL_PENDING)
				.build()
		);

		Optional<UserAdditionSaveEntity> byId = userAdditionEntityRepository.findByCorrelationId(save.correlationId);
		assertThat(byId).isPresent();
	}

	@Test
	void shouldDelete(){
		UserAdditionSaveEntity userAdditionSaveEntity = userAdditionEntityRepository.save(
			UserAdditionSaveEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.userId("userId")
				.build()
		);

		userAdditionEntityRepository.deleteById(userAdditionSaveEntity.getId());
		Optional<UserAdditionSaveEntity> byId = userAdditionEntityRepository.findById(userAdditionSaveEntity.getId());

		assertThat(byId).isEmpty();
	}

	@Test
	void shouldUpdate(){
		UserAdditionSaveEntity userAdditionSaveEntity = userAdditionEntityRepository.save(
			UserAdditionSaveEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.userId("userId")
				.build()
		);
		userAdditionEntityRepository.save(
			UserAdditionSaveEntity.builder()
				.id(userAdditionSaveEntity.getId())
				.siteId(siteId)
				.projectId(projectId)
				.userId("userId")
				.build()
		);
		Optional<UserAdditionSaveEntity> byId = userAdditionEntityRepository.findById(userAdditionSaveEntity.getId());

		assertThat(byId).isPresent();
		assertThat(byId.get().userId).isEqualTo("userId");
	}
}