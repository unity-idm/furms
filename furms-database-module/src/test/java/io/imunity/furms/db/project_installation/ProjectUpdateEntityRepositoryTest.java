/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_installation;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.project_installation.ProjectUpdateStatus;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.ACKNOWLEDGED;
import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProjectUpdateEntityRepositoryTest extends DBIntegrationTest {

	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private CommunityRepository communityRepository;
	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ProjectUpdateJobEntityRepository entityRepository;

	private SiteId siteId;
	private SiteId siteId2;

	private CommunityId communityId;

	private ProjectId projectId;
	private ProjectId projectId2;

	@BeforeEach
	void init() {
		Site site = Site.builder()
			.name("name")
			.build();
		Site site1 = Site.builder()
			.name("name2")
			.build();
		siteId = siteRepository.create(site, new SiteExternalId("id"));
		siteId2 = siteRepository.create(site1, new SiteExternalId("id2"));

		Community community = Community.builder()
			.name("name")
			.logo(FurmsImage.empty())
			.build();
		Community community2 = Community.builder()
			.name("name1")
			.logo(FurmsImage.empty())
			.build();
		communityId = communityRepository.create(community);
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
	void shouldCreateProjectUpdateJob() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectUpdateJobEntity entityToSave = ProjectUpdateJobEntity.builder()
				.correlationId(correlationId)
				.siteId(siteId.id)
				.projectId(projectId.id)
				.status(ProjectUpdateStatus.PENDING)
				.build();

		//when
		ProjectUpdateJobEntity saved = entityRepository.save(entityToSave);

		//then
		assertThat(entityRepository.findAll()).hasSize(1);
		Optional<ProjectUpdateJobEntity> byId = entityRepository.findById(saved.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(saved.getId());
		assertThat(byId.get().status).isEqualTo(ProjectUpdateStatus.PENDING.getPersistentId());
		assertThat(byId.get().correlationId).isEqualTo(correlationId);
	}

	@Test
	void shouldUpdateProjectUpdateJob() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectUpdateJobEntity entityToSave = ProjectUpdateJobEntity.builder()
				.correlationId(correlationId)
				.siteId(siteId.id)
				.projectId(projectId.id)
				.status(ProjectUpdateStatus.PENDING)
				.build();

		//when
		ProjectUpdateJobEntity save = entityRepository.save(entityToSave);

		ProjectUpdateJobEntity entityToUpdate = ProjectUpdateJobEntity.builder()
			.id(save.getId())
			.correlationId(save.correlationId)
			.siteId(save.siteId)
			.projectId(save.projectId)
			.status(ProjectUpdateStatus.ACKNOWLEDGED)
			.build();

		entityRepository.save(entityToUpdate);

		//then
		Optional<ProjectUpdateJobEntity> byId = entityRepository.findById(entityToSave.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(save.getId());
		assertThat(byId.get().status).isEqualTo(ACKNOWLEDGED.getPersistentId());
		assertThat(byId.get().correlationId).isEqualTo(correlationId);
	}

	@Test
	void shouldFindCreatedProjectUpdateJob() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectUpdateJobEntity toSave = ProjectUpdateJobEntity.builder()
				.correlationId(correlationId)
				.siteId(siteId.id)
				.projectId(projectId.id)
				.status(ProjectUpdateStatus.PENDING)
				.build();

		entityRepository.save(toSave);

		//when
		Optional<ProjectUpdateJobEntity> byId = entityRepository.findById(toSave.getId());

		//then
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindCreatedProjectUpdateJobByProjectIdAndSiteId() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectUpdateJobEntity toSave = ProjectUpdateJobEntity.builder()
			.correlationId(correlationId)
			.siteId(siteId.id)
			.projectId(projectId.id)
			.status(ProjectUpdateStatus.PENDING)
			.build();

		ProjectUpdateJobEntity saved = entityRepository.save(toSave);

		//when
		Optional<ProjectUpdateJobEntity> entity = entityRepository.findByProjectIdAndSiteId(projectId.id, siteId.id);

		//then
		assertThat(entity).isPresent();
		assertThat(entity.get().getId()).isEqualTo(saved.getId());
		assertThat(entity.get().status).isEqualTo(PENDING.getPersistentId());
		assertThat(entity.get().correlationId).isEqualTo(correlationId);
		assertThat(entity.get().projectId).isEqualTo(projectId.id);
	}

	@Test
	void shouldFindAllProjectUpdateJobByCommunityId() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectUpdateJobEntity toSave = ProjectUpdateJobEntity.builder()
			.correlationId(correlationId)
			.siteId(siteId.id)
			.projectId(projectId.id)
			.status(ProjectUpdateStatus.PENDING)
			.build();
		ProjectUpdateJobEntity toSave1 = ProjectUpdateJobEntity.builder()
			.correlationId(correlationId)
			.siteId(siteId2.id)
			.projectId(projectId2.id)
			.status(ProjectUpdateStatus.PENDING)
			.build();

		ProjectUpdateJobEntity saved = entityRepository.save(toSave);
		entityRepository.save(toSave1);

		//when
		Set<ProjectUpdateJobEntity> entities = entityRepository.findAllByCommunityId(communityId.id);

		//then
		assertThat(entities.size()).isEqualTo(1);
		ProjectUpdateJobEntity entity = entities.iterator().next();
		assertThat(entity.getId()).isEqualTo(saved.getId());
		assertThat(entity.status).isEqualTo(PENDING.getPersistentId());
		assertThat(entity.correlationId).isEqualTo(correlationId);
		assertThat(entity.projectId).isEqualTo(projectId.id);
	}

	@Test
	void shouldFindAllProjectUpdateJobByProjectId() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectUpdateJobEntity toSave = ProjectUpdateJobEntity.builder()
			.correlationId(correlationId)
			.siteId(siteId.id)
			.projectId(projectId.id)
			.status(ProjectUpdateStatus.PENDING)
			.build();
		ProjectUpdateJobEntity toSave1 = ProjectUpdateJobEntity.builder()
			.correlationId(correlationId)
			.siteId(siteId2.id)
			.projectId(projectId2.id)
			.status(ProjectUpdateStatus.PENDING)
			.build();

		ProjectUpdateJobEntity saved = entityRepository.save(toSave);
		entityRepository.save(toSave1);

		//when
		List<ProjectUpdateJobEntity> entities = entityRepository.findByProjectId(projectId.id);

		//then
		assertThat(entities.size()).isEqualTo(1);
		ProjectUpdateJobEntity entity = entities.iterator().next();
		assertThat(entity.getId()).isEqualTo(saved.getId());
		assertThat(entity.status).isEqualTo(PENDING.getPersistentId());
		assertThat(entity.correlationId).isEqualTo(correlationId);
		assertThat(entity.projectId).isEqualTo(projectId.id);
	}

	@Test
	void shouldFindCreatedProjectUpdateJobByCorrelationId() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectUpdateJobEntity toFind = ProjectUpdateJobEntity.builder()
				.correlationId(correlationId)
				.siteId(siteId.id)
				.projectId(projectId.id)
				.status(ProjectUpdateStatus.PENDING)
				.build();

		entityRepository.save(toFind);
		ProjectUpdateJobEntity findById = entityRepository.findByCorrelationId(correlationId).get();

		//when
		Optional<ProjectUpdateJobEntity> byId = entityRepository.findById(findById.getId());

		//then
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindAllAvailableProjectUpdateJob() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectUpdateJobEntity toSave = ProjectUpdateJobEntity.builder()
				.correlationId(correlationId)
				.siteId(siteId.id)
				.projectId(projectId.id)
				.status(ProjectUpdateStatus.PENDING)
				.build();
		UUID correlationId1 = UUID.randomUUID();
		ProjectUpdateJobEntity toSave1 = ProjectUpdateJobEntity.builder()
			.correlationId(correlationId1)
			.siteId(siteId2.id)
			.projectId(projectId2.id)
			.status(ProjectUpdateStatus.ACKNOWLEDGED)
			.build();

		entityRepository.save(toSave);
		entityRepository.save(toSave1);

		//when
		Iterable<ProjectUpdateJobEntity> all = entityRepository.findAll();

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void shouldDeleteProjectUpdateJob() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectUpdateJobEntity toSave = ProjectUpdateJobEntity.builder()
				.correlationId(correlationId)
				.siteId(siteId2.id)
				.projectId(projectId2.id)
				.status(ProjectUpdateStatus.PENDING)
				.build();

		//when
		entityRepository.save(toSave);
		entityRepository.deleteById(toSave.getId());

		//then
		assertThat(entityRepository.findById(toSave.getId())).isEmpty();
	}

	@Test
	void shouldDeleteAllProjectUpdateJobs() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectUpdateJobEntity toSave = ProjectUpdateJobEntity.builder()
				.correlationId(correlationId)
				.siteId(siteId.id)
				.projectId(projectId.id)
				.status(ProjectUpdateStatus.PENDING)
				.build();
		UUID correlationId1 = UUID.randomUUID();
		ProjectUpdateJobEntity toSave1 = ProjectUpdateJobEntity.builder()
			.correlationId(correlationId1)
			.siteId(siteId2.id)
			.projectId(projectId2.id)
			.status(ProjectUpdateStatus.ACKNOWLEDGED)
			.build();

		//when
		entityRepository.save(toSave);
		entityRepository.save(toSave1);
		entityRepository.deleteAll();

		//then
		assertThat(entityRepository.findAll()).hasSize(0);
	}

}