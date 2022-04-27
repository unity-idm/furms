/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_installation;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.images.FurmsImage;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.ACKNOWLEDGED;
import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.INSTALLED;
import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProjectInstallationEntityRepositoryTest extends DBIntegrationTest {

	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private CommunityRepository communityRepository;
	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ProjectInstallationJobEntityRepository entityRepository;

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
	void shouldCreateProjectInstallationJob() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectInstallationJobEntity entityToSave = ProjectInstallationJobEntity.builder()
				.correlationId(correlationId)
				.siteId(siteId.id)
				.projectId(projectId.id)
				.status(PENDING)
				.gid("gid")
				.build();

		//when
		ProjectInstallationJobEntity saved = entityRepository.save(entityToSave);

		//then
		assertThat(entityRepository.findAll()).hasSize(1);
		Optional<ProjectInstallationJobEntity> byId = entityRepository.findById(saved.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(saved.getId());
		assertThat(byId.get().status).isEqualTo(PENDING.getPersistentId());
		assertThat(byId.get().correlationId).isEqualTo(correlationId);
		assertThat(byId.get().gid).isEqualTo("gid");
	}


	@Test
	void shouldFindProjectInstallationJobByCommunityId() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectInstallationJobEntity entityToSave = ProjectInstallationJobEntity.builder()
			.correlationId(correlationId)
			.siteId(siteId.id)
			.projectId(projectId.id)
			.status(PENDING)
			.gid("gid")
			.build();
		ProjectInstallationJobEntity secondEntityToSave = ProjectInstallationJobEntity.builder()
			.correlationId(UUID.randomUUID())
			.siteId(siteId.id)
			.projectId(projectId2.id)
			.status(PENDING)
			.gid("gid")
			.build();

		//when
		entityRepository.save(entityToSave);
		entityRepository.save(secondEntityToSave);

		//then
		Set<ProjectInstallationJobStatusEntity> entities = entityRepository.findAllByCommunityId(communityId.id);
		assertThat(entities).hasSize(1);
		ProjectInstallationJobStatusEntity entity = entities.iterator().next();
		assertThat(entity.projectId).isEqualTo(projectId.id);
		assertThat(entity.status).isEqualTo(PENDING.getPersistentId());
		assertThat(entity.siteId).isEqualTo(siteId.id);
		assertThat(entity.siteName).isEqualTo("name");
	}

	@Test
	void shouldFindSiteInstalledProjectsBySiteId() {
		//given
		entityRepository.save(ProjectInstallationJobEntity.builder()
				.correlationId(UUID.randomUUID())
				.siteId(siteId.id)
				.projectId(projectId.id)
				.status(INSTALLED)
				.gid("gid")
				.build());
		entityRepository.save(ProjectInstallationJobEntity.builder()
				.correlationId(UUID.randomUUID())
				.siteId(siteId2.id)
				.projectId(projectId.id)
				.status(INSTALLED)
				.gid("gid")
				.build());
		entityRepository.save(ProjectInstallationJobEntity.builder()
				.correlationId(UUID.randomUUID())
				.siteId(siteId.id)
				.projectId(projectId2.id)
				.status(INSTALLED)
				.gid("gid")
				.build());
		entityRepository.save(ProjectInstallationJobEntity.builder()
				.correlationId(UUID.randomUUID())
				.siteId(siteId2.id)
				.projectId(projectId2.id)
				.status(INSTALLED)
				.gid("gid")
				.build());

		//when
		final Set<ProjectInstallationJobStatusEntity> installed = entityRepository.findAllInstalledBySiteId(siteId.id);

		//then
		assertThat(installed).hasSize(2);
		assertThat(installed.stream().allMatch(install -> install.status == INSTALLED.getPersistentId())).isTrue();
		assertThat(installed.stream().allMatch(install -> install.siteId.equals(siteId.id))).isTrue();
	}

	@Test
	void shouldFindProjectInstallationJobByProjectId() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectInstallationJobEntity entityToSave = ProjectInstallationJobEntity.builder()
			.correlationId(correlationId)
			.siteId(siteId.id)
			.projectId(projectId.id)
			.status(PENDING)
			.gid("gid")
			.build();
		ProjectInstallationJobEntity secondEntityToSave = ProjectInstallationJobEntity.builder()
			.correlationId(UUID.randomUUID())
			.siteId(siteId.id)
			.projectId(projectId2.id)
			.status(PENDING)
			.gid("gid")
			.build();

		//when
		entityRepository.save(entityToSave);
		entityRepository.save(secondEntityToSave);

		//then
		Set<ProjectInstallationJobStatusEntity> entities = entityRepository.findAllByProjectId(projectId.id);
		assertThat(entities).hasSize(1);
		ProjectInstallationJobStatusEntity entity = entities.iterator().next();
		assertThat(entity.projectId).isEqualTo(projectId.id);
		assertThat(entity.status).isEqualTo(PENDING.getPersistentId());
		assertThat(entity.siteId).isEqualTo(siteId.id);
		assertThat(entity.siteName).isEqualTo("name");
	}

	@Test
	void shouldExistsIfAcknowledged() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectInstallationJobEntity entityToSave = ProjectInstallationJobEntity.builder()
			.correlationId(correlationId)
			.siteId(siteId.id)
			.projectId(projectId.id)
			.status(ACKNOWLEDGED)
			.build();

		//when
		entityRepository.save(entityToSave);

		//then
		assertThat(entityRepository.existsByProjectIdAndStatusOrProjectIdAndStatus(projectId.id, 0, projectId.id,1)).isTrue();
	}

	@Test
	void shouldExistsIfPending() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectInstallationJobEntity entityToSave = ProjectInstallationJobEntity.builder()
			.correlationId(correlationId)
			.siteId(siteId.id)
			.projectId(projectId.id)
			.status(PENDING)
			.build();

		//when
		entityRepository.save(entityToSave);

		//then
		assertThat(entityRepository.existsByProjectIdAndStatusOrProjectIdAndStatus(projectId.id, 0, projectId.id, 1)).isTrue();
	}

	@Test
	void shouldUpdateProjectInstallationJob() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectInstallationJobEntity entityToSave = ProjectInstallationJobEntity.builder()
				.correlationId(correlationId)
				.siteId(siteId.id)
				.projectId(projectId.id)
				.status(PENDING)
				.build();

		//when
		ProjectInstallationJobEntity save = entityRepository.save(entityToSave);

		ProjectInstallationJobEntity entityToUpdate = ProjectInstallationJobEntity.builder()
			.id(save.getId())
			.correlationId(save.correlationId)
			.siteId(save.siteId)
			.projectId(save.projectId)
			.status(ACKNOWLEDGED)
			.build();

		entityRepository.save(entityToUpdate);

		//then
		Optional<ProjectInstallationJobEntity> byId = entityRepository.findById(entityToSave.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(save.getId());
		assertThat(byId.get().status).isEqualTo(ACKNOWLEDGED.getPersistentId());
		assertThat(byId.get().correlationId).isEqualTo(correlationId);
	}

	@Test
	void shouldFindCreatedProjectInstallationJob() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectInstallationJobEntity toSave = ProjectInstallationJobEntity.builder()
				.correlationId(correlationId)
				.siteId(siteId.id)
				.projectId(projectId.id)
				.status(PENDING)
				.build();

		entityRepository.save(toSave);

		//when
		Optional<ProjectInstallationJobEntity> byId = entityRepository.findById(toSave.getId());

		//then
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindCreatedProjectInstallationJobByCorrelationId() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectInstallationJobEntity toFind = ProjectInstallationJobEntity.builder()
				.correlationId(correlationId)
				.siteId(siteId.id)
				.projectId(projectId.id)
				.status(PENDING)
				.build();

		entityRepository.save(toFind);
		ProjectInstallationJobEntity findById = entityRepository.findByCorrelationId(correlationId).get();

		//when
		Optional<ProjectInstallationJobEntity> byId = entityRepository.findById(findById.getId());

		//then
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindAllAvailableProjectInstallationJob() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectInstallationJobEntity toSave = ProjectInstallationJobEntity.builder()
				.correlationId(correlationId)
				.siteId(siteId.id)
				.projectId(projectId.id)
				.status(PENDING)
				.build();
		UUID correlationId1 = UUID.randomUUID();
		ProjectInstallationJobEntity toSave1 = ProjectInstallationJobEntity.builder()
			.correlationId(correlationId1)
			.siteId(siteId2.id)
			.projectId(projectId2.id)
			.status(ACKNOWLEDGED)
			.build();

		entityRepository.save(toSave);
		entityRepository.save(toSave1);

		//when
		Iterable<ProjectInstallationJobEntity> all = entityRepository.findAll();

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void shouldDeleteProjectInstallationJob() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectInstallationJobEntity toSave = ProjectInstallationJobEntity.builder()
				.correlationId(correlationId)
				.siteId(siteId2.id)
				.projectId(projectId2.id)
				.status(PENDING)
				.build();

		//when
		entityRepository.save(toSave);
		entityRepository.deleteById(toSave.getId());

		//then
		assertThat(entityRepository.findById(toSave.getId())).isEmpty();
	}

	@Test
	void shouldDeleteAllProjectInstallationJobs() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectInstallationJobEntity toSave = ProjectInstallationJobEntity.builder()
				.correlationId(correlationId)
				.siteId(siteId.id)
				.projectId(projectId.id)
				.status(PENDING)
				.build();
		UUID correlationId1 = UUID.randomUUID();
		ProjectInstallationJobEntity toSave1 = ProjectInstallationJobEntity.builder()
			.correlationId(correlationId1)
			.siteId(siteId2.id)
			.projectId(projectId2.id)
			.status(ACKNOWLEDGED)
			.build();

		//when
		entityRepository.save(toSave);
		entityRepository.save(toSave1);
		entityRepository.deleteAll();

		//then
		assertThat(entityRepository.findAll()).hasSize(0);
	}

}