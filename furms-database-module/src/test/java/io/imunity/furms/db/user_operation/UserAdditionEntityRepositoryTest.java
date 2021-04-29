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
	private UserRemovalEntityRepository userRemovalEntityRepository;

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

	@AfterEach
	void clean(){
		userAdditionEntityRepository.deleteAll();
	}

	@Test
	void shouldFindAllByProjectIdAndUserIdWithRelatedSite(){
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
				.userId("userId2")
				.status(UserAdditionStatus.ACK)
				.build()
		);

		Set<UserAdditionReadEntity> userAdditions = userAdditionEntityRepository.findAllByProjectIdAndUserId(projectId, "userId");
		assertThat(userAdditions.size()).isEqualTo(1);
		assertThat(userAdditions.iterator().next().status).isEqualTo(UserAdditionStatus.PENDING);
		assertThat(userAdditions.iterator().next().site.getExternalId()).isEqualTo("id");
		assertThat(userAdditions.iterator().next().site.getId()).isEqualTo(siteId);
	}

	@Test
	void shouldConfirmUserAdditionWhenRemovingIsPending() {
		UserAdditionSaveEntity userAdditionSaveEntity = userAdditionEntityRepository.save(
			UserAdditionSaveEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.correlationId(UUID.randomUUID())
				.uid("uid")
				.userId("userId")
				.status(UserAdditionStatus.ADDED)
				.build()
		);

		userRemovalEntityRepository.save(
			UserRemovalSaveEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.correlationId(UUID.randomUUID())
				.uid("uid")
				.userId("userId2")
				.userAdditionId(userAdditionSaveEntity.getId())
				.status(UserRemovalStatus.PENDING)
				.build()
		);

		boolean userId = userAdditionEntityRepository.isUserAdded("userId", UserAdditionStatus.ADDED, UserRemovalStatus.REMOVED);
		assertThat(userId).isTrue();
	}

	@Test
	void shouldConfirmUserAdditionWhenRemovingDoesntExist(){
		UserAdditionSaveEntity userAdditionSaveEntity = userAdditionEntityRepository.save(
			UserAdditionSaveEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.correlationId(UUID.randomUUID())
				.uid("uid")
				.userId("userId")
				.status(UserAdditionStatus.ADDED)
				.build()
		);

		boolean userId = userAdditionEntityRepository.isUserAdded("userId", UserAdditionStatus.ADDED, UserRemovalStatus.REMOVED);
		assertThat(userId).isTrue();
	}

	@Test
	void shouldNotConfirmUserAdditionWhenRemovingIsDone(){
		UserAdditionSaveEntity userAdditionSaveEntity = userAdditionEntityRepository.save(
			UserAdditionSaveEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.correlationId(UUID.randomUUID())
				.uid("uid")
				.userId("userId")
				.status(UserAdditionStatus.ADDED)
				.build()
		);

		userRemovalEntityRepository.save(
			UserRemovalSaveEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.correlationId(UUID.randomUUID())
				.uid("uid")
				.userId("userId2")
				.userAdditionId(userAdditionSaveEntity.getId())
				.status(UserRemovalStatus.REMOVED)
				.build()
		);

		boolean userId = userAdditionEntityRepository.isUserAdded("userId", UserAdditionStatus.ADDED, UserRemovalStatus.REMOVED);
		assertThat(userId).isFalse();
	}

	@Test
	void shouldCreate(){
		UserAdditionSaveEntity userAdditionSaveEntity = userAdditionEntityRepository.save(
			UserAdditionSaveEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.correlationId(UUID.randomUUID())
				.uid("uid")
				.userId("userId")
				.status(UserAdditionStatus.ADDED)
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
				.correlationId(UUID.randomUUID())
				.uid("uid")
				.userId("userId")
				.status(UserAdditionStatus.ADDED)
				.build()
		);

		Optional<UserAdditionSaveEntity> byId = userAdditionEntityRepository.findByCorrelationId(userAdditionSaveEntity.correlationId);
		assertThat(byId).isPresent();
	}

	@Test
	void shouldDelete(){
		UserAdditionSaveEntity userAdditionSaveEntity = userAdditionEntityRepository.save(
			UserAdditionSaveEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.correlationId(UUID.randomUUID())
				.uid("uid")
				.userId("userId")
				.status(UserAdditionStatus.ADDED)
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
				.correlationId(UUID.randomUUID())
				.uid("uid")
				.userId("userId")
				.status(UserAdditionStatus.PENDING)
				.build()
		);
		userAdditionEntityRepository.save(
			UserAdditionSaveEntity.builder()
				.id(userAdditionSaveEntity.getId())
				.siteId(siteId)
				.projectId(projectId)
				.correlationId(UUID.randomUUID())
				.uid("uid")
				.userId("userId")
				.status(UserAdditionStatus.ADDED)
				.build()
		);
		Optional<UserAdditionSaveEntity> byId = userAdditionEntityRepository.findById(userAdditionSaveEntity.getId());

		assertThat(byId).isPresent();
		assertThat(byId.get().status).isEqualTo(UserAdditionStatus.ADDED);
	}
}