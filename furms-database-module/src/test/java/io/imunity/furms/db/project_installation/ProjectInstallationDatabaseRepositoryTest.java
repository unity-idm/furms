/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_installation;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.project_installation.Error;
import io.imunity.furms.domain.project_installation.ProjectInstallationJob;
import io.imunity.furms.domain.project_installation.ProjectInstallationJobStatus;
import io.imunity.furms.domain.project_installation.ProjectInstallationResult;
import io.imunity.furms.domain.project_installation.ProjectUpdateJob;
import io.imunity.furms.domain.project_installation.ProjectUpdateJobStatus;
import io.imunity.furms.domain.project_installation.ProjectUpdateResult;
import io.imunity.furms.domain.project_installation.ProjectUpdateStatus;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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

	private UUID siteId;

	private UUID communityId;

	private UUID projectId;

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
		communityId = UUID.fromString(communityRepository.create(community));

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
	void shouldCreateProjectInstallationJob() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectInstallationJob request = ProjectInstallationJob.builder()
				.correlationId(correlationId)
				.siteId(siteId.toString())
				.projectId(projectId.toString())
				.status(PENDING)
				.build();

		//when
		String id = entityDatabaseRepository.create(request);

		//then
		Optional<ProjectInstallationJobEntity> byId = installationRepository.findById(UUID.fromString(id));
		assertThat(byId).isPresent();
		assertThat(byId.get().getId().toString()).isEqualTo(id);
		assertThat(byId.get().correlationId.toString()).isEqualTo(correlationId.id);
		assertThat(byId.get().status).isEqualTo(PENDING.getPersistentId());
	}

	@Test
	void shouldFindProjectInstallationJobStatsByCommunityId() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectInstallationJob request = ProjectInstallationJob.builder()
			.correlationId(correlationId)
			.siteId(siteId.toString())
			.projectId(projectId.toString())
			.status(PENDING)
			.build();

		//when
		String id = entityDatabaseRepository.create(request);

		//then
		Set<ProjectInstallationJobStatus> statuses = entityDatabaseRepository.findAllByCommunityId(communityId.toString());
		assertThat(statuses.size()).isEqualTo(1);
		ProjectInstallationJobStatus status = statuses.iterator().next();
		assertThat(status.status).isEqualTo(PENDING);
		assertThat(status.projectId).isEqualTo(projectId.toString());
		assertThat(status.siteId).isEqualTo(siteId.toString());
	}

	@Test
	void shouldFindProjectInstallationJobStatsByProjectId() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectInstallationJob request = ProjectInstallationJob.builder()
			.correlationId(correlationId)
			.siteId(siteId.toString())
			.projectId(projectId.toString())
			.status(PENDING)
			.build();

		//when
		String id = entityDatabaseRepository.create(request);

		//then
		Set<ProjectInstallationJobStatus> statuses = entityDatabaseRepository.findAllByProjectId(projectId.toString());
		assertThat(statuses.size()).isEqualTo(1);
		ProjectInstallationJobStatus status = statuses.iterator().next();
		assertThat(status.status).isEqualTo(PENDING);
		assertThat(status.projectId).isEqualTo(projectId.toString());
		assertThat(status.siteId).isEqualTo(siteId.toString());
	}

	@Test
	void shouldUpdateProjectInstallationJob() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectInstallationJob request = ProjectInstallationJob.builder()
				.id("id")
				.correlationId(correlationId)
				.siteId(siteId.toString())
				.projectId(projectId.toString())
				.status(PENDING)
				.build();

		//when
		String id = entityDatabaseRepository.create(request);
		entityDatabaseRepository.update(id, new ProjectInstallationResult(Map.of("gid", "gid"), INSTALLED, new Error(null, null)));

		//then
		Optional<ProjectInstallationJobEntity> byId = installationRepository.findById(UUID.fromString(id));
		assertThat(byId).isPresent();
		assertThat(byId.get().getId().toString()).isEqualTo(id);
		assertThat(byId.get().correlationId.toString()).isEqualTo(correlationId.id);
		assertThat(byId.get().status).isEqualTo(INSTALLED.getPersistentId());
		assertThat(byId.get().gid).isEqualTo("gid");
	}

	@Test
	void shouldUpdateProjectInstallationJobWhenAttributesAreNull() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectInstallationJob request = ProjectInstallationJob.builder()
			.id("id")
			.correlationId(correlationId)
			.siteId(siteId.toString())
			.projectId(projectId.toString())
			.status(PENDING)
			.build();

		//when
		String id = entityDatabaseRepository.create(request);
		entityDatabaseRepository.update(id, new ProjectInstallationResult(null, INSTALLED, new Error(null, null)));

		//then
		Optional<ProjectInstallationJobEntity> byId = installationRepository.findById(UUID.fromString(id));
		assertThat(byId).isPresent();
		assertThat(byId.get().getId().toString()).isEqualTo(id);
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
			.siteId(siteId.toString())
			.projectId(projectId.toString())
			.status(ProjectUpdateStatus.PENDING)
			.build();

		//when
		String id = entityDatabaseRepository.createOrUpdate(request);

		//then
		Optional<ProjectUpdateJobEntity> byId = updateRepository.findById(UUID.fromString(id));
		assertThat(byId).isPresent();
		assertThat(byId.get().getId().toString()).isEqualTo(id);
		assertThat(byId.get().correlationId.toString()).isEqualTo(correlationId.id);
		assertThat(byId.get().status).isEqualTo(PENDING.getPersistentId());
	}

	@Test
	void shouldFindProjectUpdateJobByCommunityId() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectUpdateJob request = ProjectUpdateJob.builder()
			.correlationId(correlationId)
			.siteId(siteId.toString())
			.projectId(projectId.toString())
			.status(ProjectUpdateStatus.PENDING)
			.build();

		//when
		String id = entityDatabaseRepository.createOrUpdate(request);

		//then
		Set<ProjectUpdateJobStatus> statuses = entityDatabaseRepository.findAllUpdatesByCommunityId(communityId.toString());

		ProjectUpdateJobStatus status = statuses.iterator().next();
		assertThat(status.status).isEqualTo(ProjectUpdateStatus.PENDING);
		assertThat(status.projectId).isEqualTo(projectId.toString());
		assertThat(status.siteId).isEqualTo(siteId.toString());
	}

	@Test
	void shouldFindProjectUpdateJobByProjectId() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectUpdateJob request = ProjectUpdateJob.builder()
			.correlationId(correlationId)
			.siteId(siteId.toString())
			.projectId(projectId.toString())
			.status(ProjectUpdateStatus.PENDING)
			.build();

		//when
		String id = entityDatabaseRepository.createOrUpdate(request);

		//then
		Set<ProjectUpdateJobStatus> statuses = entityDatabaseRepository.findAllUpdatesByProjectId(projectId.toString());

		ProjectUpdateJobStatus status = statuses.iterator().next();
		assertThat(status.status).isEqualTo(ProjectUpdateStatus.PENDING);
		assertThat(status.projectId).isEqualTo(projectId.toString());
		assertThat(status.siteId).isEqualTo(siteId.toString());
	}

	@Test
	void shouldUpdateProjectUpdateJob() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectUpdateJob request = ProjectUpdateJob.builder()
			.id("id")
			.correlationId(correlationId)
			.siteId(siteId.toString())
			.projectId(projectId.toString())
			.status(ProjectUpdateStatus.PENDING)
			.build();

		//when
		String id = entityDatabaseRepository.createOrUpdate(request);
		entityDatabaseRepository.update(id, new ProjectUpdateResult(ProjectUpdateStatus.UPDATED, new Error(null, null)));

		//then
		Optional<ProjectUpdateJobEntity> byId = updateRepository.findById(UUID.fromString(id));
		assertThat(byId).isPresent();
		assertThat(byId.get().getId().toString()).isEqualTo(id);
		assertThat(byId.get().correlationId.toString()).isEqualTo(correlationId.id);
		assertThat(byId.get().status).isEqualTo(ProjectUpdateStatus.UPDATED.getPersistentId());
	}
}