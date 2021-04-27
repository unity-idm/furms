/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_installation;


import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.INSTALLED;
import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.SENT;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.project_installation.ProjectInstallationJob;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.sites.SiteRepository;

@SpringBootTest
class ProjectInstallationDatabaseRepositoryTest extends DBIntegrationTest {
	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private CommunityRepository communityRepository;
	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ProjectInstallationJobEntityRepository entityRepository;

	@Autowired
	private ProjectInstallationJobDatabaseRepository entityDatabaseRepository;

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

	@AfterEach
	void clean(){
		entityRepository.deleteAll();
	}

	@Test
	void shouldCreateProjectAllocation() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectInstallationJob request = ProjectInstallationJob.builder()
				.correlationId(correlationId)
				.siteId(siteId.toString())
				.projectId(projectId.toString())
				.status(SENT)
				.build();

		//when
		String id = entityDatabaseRepository.create(request);

		//then
		Optional<ProjectInstallationJobEntity> byId = entityRepository.findById(UUID.fromString(id));
		assertThat(byId).isPresent();
		assertThat(byId.get().getId().toString()).isEqualTo(id);
		assertThat(byId.get().correlationId.toString()).isEqualTo(correlationId.id);
		assertThat(byId.get().status).isEqualTo(SENT);
	}

	@Test
	void shouldUpdateProjectAllocation() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectInstallationJob request = ProjectInstallationJob.builder()
				.id("id")
				.correlationId(correlationId)
				.siteId(siteId.toString())
				.projectId(projectId.toString())
				.status(SENT)
				.build();

		//when
		String id = entityDatabaseRepository.create(request);
		entityDatabaseRepository.update(id, INSTALLED);

		//then
		Optional<ProjectInstallationJobEntity> byId = entityRepository.findById(UUID.fromString(id));
		assertThat(byId).isPresent();
		assertThat(byId.get().getId().toString()).isEqualTo(id);
		assertThat(byId.get().correlationId.toString()).isEqualTo(correlationId.id);
		assertThat(byId.get().status).isEqualTo(INSTALLED);
	}

	@Test
	void shouldRemoveProjectInstallationJob(){
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectInstallationJob request = ProjectInstallationJob.builder()
				.correlationId(correlationId)
				.siteId(siteId.toString())
				.projectId(projectId.toString())
				.status(SENT)
				.build();

		//when
		String id = entityDatabaseRepository.create(request);
		entityDatabaseRepository.delete(id);

		//then
		assertThat(entityRepository.findById(UUID.fromString(id))).isEmpty();
	}

}