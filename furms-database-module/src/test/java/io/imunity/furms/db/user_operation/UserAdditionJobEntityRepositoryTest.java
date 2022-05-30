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
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.user_operation.UserAdditionId;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserAdditionJobEntityRepositoryTest extends DBIntegrationTest {

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

	private UserAdditionId userAdditionalId;

	@BeforeEach
	void init() {
		Site site = Site.builder()
			.name("name")
			.build();
		SiteId siteId = siteRepository.create(site, new SiteExternalId("id"));

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

		ProjectId projectId = projectRepository.create(project);

		UUID uAdditionalId = userAdditionEntityRepository.save(
			UserAdditionSaveEntity.builder()
				.siteId(siteId.id)
				.projectId(projectId.id)
				.userId("userId")
				.build()
		).getId();

		userAdditionalId = new UserAdditionId(uAdditionalId);
	}

	@Test
	void shouldCreate(){
		UserAdditionJobEntity userAdditionSaveEntity = userAdditionJobEntityRepository.save(
			UserAdditionJobEntity.builder()
				.userAdditionId(userAdditionalId.id)
				.correlationId(UUID.randomUUID())
				.status(UserStatus.ADDING_PENDING)
				.build()
		);

		Optional<UserAdditionJobEntity> byId = userAdditionJobEntityRepository.findById(userAdditionSaveEntity.getId());
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindByCorrelation(){
		UserAdditionJobEntity userAdditionSaveEntity = userAdditionJobEntityRepository.save(
			UserAdditionJobEntity.builder()
				.userAdditionId(userAdditionalId.id)
				.correlationId(UUID.randomUUID())
				.status(UserStatus.ADDING_PENDING)
				.build()
		);

		Optional<UserAdditionJobEntity> byId = userAdditionJobEntityRepository.findByCorrelationId(userAdditionSaveEntity.correlationId);
		assertThat(byId).isPresent();
	}

	@Test
	void shouldDelete(){
		UserAdditionJobEntity userAdditionSaveEntity = userAdditionJobEntityRepository.save(
			UserAdditionJobEntity.builder()
				.userAdditionId(userAdditionalId.id)
				.correlationId(UUID.randomUUID())
				.status(UserStatus.ADDING_PENDING)
				.build()
		);

		userAdditionJobEntityRepository.deleteById(userAdditionSaveEntity.getId());
		Optional<UserAdditionJobEntity> byId = userAdditionJobEntityRepository.findById(userAdditionSaveEntity.getId());

		assertThat(byId).isEmpty();
	}

	@Test
	void shouldUpdate(){
		UserAdditionJobEntity userAdditionSaveEntity = userAdditionJobEntityRepository.save(
			UserAdditionJobEntity.builder()
				.userAdditionId(userAdditionalId.id)
				.correlationId(UUID.randomUUID())
				.status(UserStatus.ADDING_PENDING)
				.build()
		);
		userAdditionJobEntityRepository.save(
			UserAdditionJobEntity.builder()
				.id(userAdditionSaveEntity.getId())
				.userAdditionId(userAdditionalId.id)
				.correlationId(UUID.randomUUID())
				.status(UserStatus.REMOVAL_PENDING)
				.build()
		);
		Optional<UserAdditionJobEntity> byId = userAdditionJobEntityRepository.findById(userAdditionSaveEntity.getId());

		assertThat(byId).isPresent();
		assertThat(byId.get().status).isEqualTo(UserStatus.REMOVAL_PENDING.getPersistentId());
	}
}