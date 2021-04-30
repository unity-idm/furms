/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_installation;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.project_installation.ProjectRemovalStatus;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.ACKNOWLEDGED;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProjectRemovalEntityRepositoryTest extends DBIntegrationTest {

	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private CommunityRepository communityRepository;
	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ProjectRemovalJobEntityRepository entityRepository;

	private UUID siteId;
	private UUID siteId2;

	private UUID projectId;
	private UUID projectId2;

	@BeforeEach
	void init() throws IOException {
		Site site = Site.builder()
			.name("name")
			.build();
		Site site1 = Site.builder()
			.name("name2")
			.build();
		siteId = UUID.fromString(siteRepository.create(site, new SiteExternalId("id")));
		siteId2 = UUID.fromString(siteRepository.create(site1, new SiteExternalId("id2")));

		Community community = Community.builder()
			.name("name")
			.logo(FurmsImage.empty())
			.build();
		Community community2 = Community.builder()
			.name("name1")
			.logo(FurmsImage.empty())
			.build();
		UUID communityId = UUID.fromString(communityRepository.create(community));
		UUID communityId2 = UUID.fromString(communityRepository.create(community2));

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
		Project project2 = Project.builder()
			.communityId(communityId2.toString())
			.name("name2")
			.logo(FurmsImage.empty())
			.description("new_description")
			.acronym("acronym")
			.researchField("research filed")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();

		projectId = UUID.fromString(projectRepository.create(project));
		projectId2 = UUID.fromString(projectRepository.create(project2));
	}

	@AfterEach
	void clean(){
		entityRepository.deleteAll();
	}

	@Test
	void shouldCreateProjectRemovalJob() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectRemovalJobEntity entityToSave = ProjectRemovalJobEntity.builder()
				.correlationId(correlationId)
				.siteId(siteId)
				.projectId(projectId)
				.status(ProjectRemovalStatus.PENDING)
				.build();

		//when
		ProjectRemovalJobEntity saved = entityRepository.save(entityToSave);

		//then
		assertThat(entityRepository.findAll()).hasSize(1);
		Optional<ProjectRemovalJobEntity> byId = entityRepository.findById(saved.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(saved.getId());
		assertThat(byId.get().status).isEqualTo(ProjectRemovalStatus.PENDING.getValue());
		assertThat(byId.get().correlationId).isEqualTo(correlationId);
	}

	@Test
	void shouldUpdateProjectRemovalJob() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectRemovalJobEntity entityToSave = ProjectRemovalJobEntity.builder()
				.correlationId(correlationId)
				.siteId(siteId)
				.projectId(projectId)
				.status(ProjectRemovalStatus.PENDING)
				.build();

		//when
		ProjectRemovalJobEntity save = entityRepository.save(entityToSave);

		ProjectRemovalJobEntity entityToUpdate = ProjectRemovalJobEntity.builder()
			.id(save.getId())
			.correlationId(save.correlationId)
			.siteId(save.siteId)
			.projectId(save.projectId)
			.status(ProjectRemovalStatus.ACKNOWLEDGED)
			.build();

		entityRepository.save(entityToUpdate);

		//then
		Optional<ProjectRemovalJobEntity> byId = entityRepository.findById(entityToSave.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(save.getId());
		assertThat(byId.get().status).isEqualTo(ACKNOWLEDGED.getValue());
		assertThat(byId.get().correlationId).isEqualTo(correlationId);
	}

	@Test
	void shouldFindCreatedProjectRemovalJob() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectRemovalJobEntity toSave = ProjectRemovalJobEntity.builder()
				.correlationId(correlationId)
				.siteId(siteId)
				.projectId(projectId)
				.status(ProjectRemovalStatus.PENDING)
				.build();

		entityRepository.save(toSave);

		//when
		Optional<ProjectRemovalJobEntity> byId = entityRepository.findById(toSave.getId());

		//then
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindCreatedProjectRemovalJobByCorrelationId() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectRemovalJobEntity toFind = ProjectRemovalJobEntity.builder()
				.correlationId(correlationId)
				.siteId(siteId)
				.projectId(projectId)
				.status(ProjectRemovalStatus.PENDING)
				.build();

		entityRepository.save(toFind);
		ProjectRemovalJobEntity findById = entityRepository.findByCorrelationId(correlationId);

		//when
		Optional<ProjectRemovalJobEntity> byId = entityRepository.findById(findById.getId());

		//then
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindAllAvailableProjectRemovalJob() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectRemovalJobEntity toSave = ProjectRemovalJobEntity.builder()
				.correlationId(correlationId)
				.siteId(siteId)
				.projectId(projectId)
				.status(ProjectRemovalStatus.PENDING)
				.build();
		UUID correlationId1 = UUID.randomUUID();
		ProjectRemovalJobEntity toSave1 = ProjectRemovalJobEntity.builder()
			.correlationId(correlationId1)
			.siteId(siteId2)
			.projectId(projectId2)
			.status(ProjectRemovalStatus.ACKNOWLEDGED)
			.build();

		entityRepository.save(toSave);
		entityRepository.save(toSave1);

		//when
		Iterable<ProjectRemovalJobEntity> all = entityRepository.findAll();

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void shouldDeleteProjectRemovalJob() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectRemovalJobEntity toSave = ProjectRemovalJobEntity.builder()
				.correlationId(correlationId)
				.siteId(siteId2)
				.projectId(projectId2)
				.status(ProjectRemovalStatus.PENDING)
				.build();

		//when
		entityRepository.save(toSave);
		entityRepository.deleteById(toSave.getId());

		//then
		assertThat(entityRepository.findById(toSave.getId())).isEmpty();
	}

	@Test
	void shouldDeleteAllProjectRemovalJobs() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectRemovalJobEntity toSave = ProjectRemovalJobEntity.builder()
				.correlationId(correlationId)
				.siteId(siteId)
				.projectId(projectId)
				.status(ProjectRemovalStatus.PENDING)
				.build();
		UUID correlationId1 = UUID.randomUUID();
		ProjectRemovalJobEntity toSave1 = ProjectRemovalJobEntity.builder()
			.correlationId(correlationId1)
			.siteId(siteId2)
			.projectId(projectId2)
			.status(ProjectRemovalStatus.ACKNOWLEDGED)
			.build();

		//when
		entityRepository.save(toSave);
		entityRepository.save(toSave1);
		entityRepository.deleteAll();

		//then
		assertThat(entityRepository.findAll()).hasSize(0);
	}

}