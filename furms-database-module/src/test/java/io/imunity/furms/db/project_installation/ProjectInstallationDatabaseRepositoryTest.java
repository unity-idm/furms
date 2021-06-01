/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_installation;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.project_installation.*;
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
import java.util.Optional;
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
		entityDatabaseRepository.update(id, INSTALLED, "gid");

		//then
		Optional<ProjectInstallationJobEntity> byId = installationRepository.findById(UUID.fromString(id));
		assertThat(byId).isPresent();
		assertThat(byId.get().getId().toString()).isEqualTo(id);
		assertThat(byId.get().correlationId.toString()).isEqualTo(correlationId.id);
		assertThat(byId.get().status).isEqualTo(INSTALLED.getPersistentId());
		assertThat(byId.get().gid).isEqualTo("gid");
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
		String id = entityDatabaseRepository.create(request);

		//then
		Optional<ProjectUpdateJobEntity> byId = updateRepository.findById(UUID.fromString(id));
		assertThat(byId).isPresent();
		assertThat(byId.get().getId().toString()).isEqualTo(id);
		assertThat(byId.get().correlationId.toString()).isEqualTo(correlationId.id);
		assertThat(byId.get().status).isEqualTo(PENDING.getPersistentId());
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
		String id = entityDatabaseRepository.create(request);
		entityDatabaseRepository.update(id, ProjectUpdateStatus.UPDATED);

		//then
		Optional<ProjectUpdateJobEntity> byId = updateRepository.findById(UUID.fromString(id));
		assertThat(byId).isPresent();
		assertThat(byId.get().getId().toString()).isEqualTo(id);
		assertThat(byId.get().correlationId.toString()).isEqualTo(correlationId.id);
		assertThat(byId.get().status).isEqualTo(ProjectUpdateStatus.UPDATED.getPersistentId());
	}
}