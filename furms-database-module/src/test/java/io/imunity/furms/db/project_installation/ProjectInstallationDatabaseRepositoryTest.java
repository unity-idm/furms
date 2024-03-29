/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_installation;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.project_installation.Error;
import io.imunity.furms.domain.project_installation.ProjectInstallationId;
import io.imunity.furms.domain.project_installation.ProjectInstallationJob;
import io.imunity.furms.domain.project_installation.ProjectInstallationJobStatus;
import io.imunity.furms.domain.project_installation.ProjectInstallationResult;
import io.imunity.furms.domain.project_installation.ProjectUpdateId;
import io.imunity.furms.domain.project_installation.ProjectUpdateJob;
import io.imunity.furms.domain.project_installation.ProjectUpdateJobStatus;
import io.imunity.furms.domain.project_installation.ProjectUpdateResult;
import io.imunity.furms.domain.project_installation.ProjectUpdateStatus;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.site_agent.CorrelationId;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.ACKNOWLEDGED;
import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.FAILED;
import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.INSTALLED;
import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProjectInstallationDatabaseRepositoryTest extends DBIntegrationTest {
	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private CommunityRepository communityRepository;
	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ProjectInstallationJobEntityRepository installationRepository;
	@Autowired
	private ProjectUpdateJobEntityRepository updateRepository;

	@Autowired
	private ProjectOperationJobDatabaseRepository entityDatabaseRepository;

	private SiteId siteId;

	private CommunityId communityId;

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
		communityId = communityRepository.create(community);

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
	void shouldCreateProjectInstallationJob() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectInstallationJob request = ProjectInstallationJob.builder()
				.correlationId(correlationId)
				.siteId(siteId)
				.projectId(projectId)
				.status(PENDING)
				.build();

		//when
		entityDatabaseRepository.createOrUpdate(request);

		//then
		ProjectInstallationJobEntity byId = installationRepository.findAll().iterator().next();
		assertThat(byId.correlationId.toString()).isEqualTo(correlationId.id);
		assertThat(byId.status).isEqualTo(PENDING.getPersistentId());
	}

	@Test
	void shouldReturnTrueWhenProjectInstallationJobIsPending() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectInstallationJob request = ProjectInstallationJob.builder()
			.correlationId(correlationId)
			.siteId(siteId)
			.projectId(projectId)
			.status(PENDING)
			.build();
		entityDatabaseRepository.createOrUpdate(request);


		assertThat(entityDatabaseRepository.pendingOrAcknowledgedInstallationProjectExistsBySiteIdAndProjectId(siteId, projectId)).isTrue();
	}

	@Test
	void shouldReturnTrueWhenProjectInstallationJobIsAcknowledged() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectInstallationJob request = ProjectInstallationJob.builder()
			.correlationId(correlationId)
			.siteId(siteId)
			.projectId(projectId)
			.status(ACKNOWLEDGED)
			.build();
		entityDatabaseRepository.createOrUpdate(request);


		assertThat(entityDatabaseRepository.pendingOrAcknowledgedInstallationProjectExistsBySiteIdAndProjectId(siteId, projectId)).isTrue();
	}

	@Test
	void shouldReturnFalseWhenProjectInstallationJobIsFaild() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectInstallationJob request = ProjectInstallationJob.builder()
			.correlationId(correlationId)
			.siteId(siteId)
			.projectId(projectId)
			.status(FAILED)
			.build();
		entityDatabaseRepository.createOrUpdate(request);


		assertThat(entityDatabaseRepository.pendingOrAcknowledgedInstallationProjectExistsBySiteIdAndProjectId(siteId, projectId)).isFalse();
	}

	@Test
	void shouldFindProjectInstallationJobStatsByCommunityId() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectInstallationJob request = ProjectInstallationJob.builder()
			.correlationId(correlationId)
			.siteId(siteId)
			.projectId(projectId)
			.status(PENDING)
			.build();

		//when
		entityDatabaseRepository.createOrUpdate(request);

		//then
		Set<ProjectInstallationJobStatus> statuses = entityDatabaseRepository.findAllByCommunityId(communityId);
		assertThat(statuses.size()).isEqualTo(1);
		ProjectInstallationJobStatus status = statuses.iterator().next();
		assertThat(status.status).isEqualTo(PENDING);
		assertThat(status.projectId).isEqualTo(projectId);
		assertThat(status.siteId).isEqualTo(siteId);
	}

	@Test
	void shouldFindProjectInstallationJobStatsByProjectId() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectInstallationJob request = ProjectInstallationJob.builder()
			.correlationId(correlationId)
			.siteId(siteId)
			.projectId(projectId)
			.status(PENDING)
			.build();

		//when
		entityDatabaseRepository.createOrUpdate(request);

		//then
		Set<ProjectInstallationJobStatus> statuses = entityDatabaseRepository.findAllByProjectId(projectId);
		assertThat(statuses.size()).isEqualTo(1);
		ProjectInstallationJobStatus status = statuses.iterator().next();
		assertThat(status.status).isEqualTo(PENDING);
		assertThat(status.projectId).isEqualTo(projectId);
		assertThat(status.siteId).isEqualTo(siteId);
	}

	@Test
	void shouldUpdateProjectInstallationJob() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectInstallationJob request = ProjectInstallationJob.builder()
				.id(UUID.randomUUID().toString())
				.correlationId(correlationId)
				.siteId(siteId)
				.projectId(projectId)
				.status(PENDING)
				.build();

		//when
		entityDatabaseRepository.createOrUpdate(request);
		UUID id = installationRepository.findAll().iterator().next().getId();
		entityDatabaseRepository.update(new ProjectInstallationId(id), new ProjectInstallationResult(Map.of("gid",
			"gid"), INSTALLED,
			new Error(null, null)));

		//then
		Optional<ProjectInstallationJobEntity> byId = installationRepository.findById(id);
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(id);
		assertThat(byId.get().correlationId.toString()).isEqualTo(correlationId.id);
		assertThat(byId.get().status).isEqualTo(INSTALLED.getPersistentId());
		assertThat(byId.get().gid).isEqualTo("gid");
	}

	@Test
	void shouldUpdateProjectInstallationJobWhenAttributesAreNull() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectInstallationJob request = ProjectInstallationJob.builder()
			.id(UUID.randomUUID().toString())
			.correlationId(correlationId)
			.siteId(siteId)
			.projectId(projectId)
			.status(PENDING)
			.build();

		//when
		entityDatabaseRepository.createOrUpdate(request);
		UUID id = installationRepository.findAll().iterator().next().getId();
		entityDatabaseRepository.update(new ProjectInstallationId(id), new ProjectInstallationResult(null, INSTALLED,
			new Error(null, null)));

		//then
		Optional<ProjectInstallationJobEntity> byId = installationRepository.findById(id);
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(id);
		assertThat(byId.get().correlationId.toString()).isEqualTo(correlationId.id);
		assertThat(byId.get().status).isEqualTo(INSTALLED.getPersistentId());
		assertThat(byId.get().gid).isEqualTo(null);
	}

	@Test
	void shouldCreateProjectUpdateJob() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectUpdateJob request = ProjectUpdateJob.builder()
			.correlationId(correlationId)
			.siteId(siteId)
			.projectId(projectId)
			.status(ProjectUpdateStatus.PENDING)
			.build();

		//when
		entityDatabaseRepository.createOrUpdate(request);
		UUID id = updateRepository.findAll().iterator().next().getId();

		//then
		Optional<ProjectUpdateJobEntity> byId = updateRepository.findById(id);
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(id);
		assertThat(byId.get().correlationId.toString()).isEqualTo(correlationId.id);
		assertThat(byId.get().status).isEqualTo(PENDING.getPersistentId());
	}

	@Test
	void shouldFindProjectUpdateJobByCommunityId() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectUpdateJob request = ProjectUpdateJob.builder()
			.correlationId(correlationId)
			.siteId(siteId)
			.projectId(projectId)
			.status(ProjectUpdateStatus.PENDING)
			.build();

		//when
		entityDatabaseRepository.createOrUpdate(request);

		//then
		Set<ProjectUpdateJobStatus> statuses = entityDatabaseRepository.findAllUpdatesByCommunityId(communityId);

		ProjectUpdateJobStatus status = statuses.iterator().next();
		assertThat(status.status).isEqualTo(ProjectUpdateStatus.PENDING);
		assertThat(status.projectId).isEqualTo(projectId);
		assertThat(status.siteId).isEqualTo(siteId);
	}

	@Test
	void shouldFindProjectUpdateJobByProjectId() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectUpdateJob request = ProjectUpdateJob.builder()
			.correlationId(correlationId)
			.siteId(siteId)
			.projectId(projectId)
			.status(ProjectUpdateStatus.PENDING)
			.build();

		//when
		entityDatabaseRepository.createOrUpdate(request);

		//then
		Set<ProjectUpdateJobStatus> statuses = entityDatabaseRepository.findAllUpdatesByProjectId(projectId);

		ProjectUpdateJobStatus status = statuses.iterator().next();
		assertThat(status.status).isEqualTo(ProjectUpdateStatus.PENDING);
		assertThat(status.projectId).isEqualTo(projectId);
		assertThat(status.siteId).isEqualTo(siteId);
	}

	@Test
	void shouldUpdateProjectUpdateJob() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectUpdateJob request = ProjectUpdateJob.builder()
			.id(UUID.randomUUID().toString())
			.correlationId(correlationId)
			.siteId(siteId)
			.projectId(projectId)
			.status(ProjectUpdateStatus.PENDING)
			.build();

		//when
		entityDatabaseRepository.createOrUpdate(request);
		UUID id = updateRepository.findAll().iterator().next().getId();
		entityDatabaseRepository.update(new ProjectUpdateId(id), new ProjectUpdateResult(ProjectUpdateStatus.UPDATED,
			new Error(null,
			null)));

		//then
		Optional<ProjectUpdateJobEntity> byId = updateRepository.findById(id);
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(id);
		assertThat(byId.get().correlationId.toString()).isEqualTo(correlationId.id);
		assertThat(byId.get().status).isEqualTo(ProjectUpdateStatus.UPDATED.getPersistentId());
	}
}