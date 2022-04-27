/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package db.migration;

import io.imunity.furms.db.project_installation.ProjectInstallationJobEntity;
import io.imunity.furms.db.project_installation.ProjectInstallationJobEntityRepository;
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
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class V34__project_installation_duplicates_clearing_migration_tests {

	@Autowired
	private ProjectInstallationJobEntityRepository repository;
	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private CommunityRepository communityRepository;
	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private Flyway flyway;

	private SiteId siteId;
	private ProjectId projectId;

	private SiteId siteId1;
	private ProjectId projectId1;

	@BeforeEach
	void setUp() {
		repository.deleteAll();
		siteRepository.deleteAll();
		projectRepository.deleteAll();
		communityRepository.deleteAll();
		flyway.clean();
		Flyway.configure()
				.configuration(flyway.getConfiguration())
				.target("33")
				.load()
				.migrate();

		Site site = Site.builder()
			.name("name")
			.build();

		siteId = siteRepository.create(site, new SiteExternalId("id"));

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
		projectId = projectRepository.create(project);


		Site site1 = Site.builder()
			.name("name1")
			.build();

		siteId1 = siteRepository.create(site1, new SiteExternalId("id1"));

		Community community1 = Community.builder()
			.name("name1")
			.logo(FurmsImage.empty())
			.build();
		CommunityId communityId1 = communityRepository.create(community1);

		Project project1 = Project.builder()
			.communityId(communityId1)
			.name("name1")
			.description("new_description")
			.logo(FurmsImage.empty())
			.acronym("acronym")
			.researchField("research filed")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();
		projectId1 = projectRepository.create(project1);
	}

	@AfterEach
	void tearDown() {
		flyway.clean();
		flyway.migrate();
	}

	@Test
	void shouldReduceProjectInstallationsToUniqueSiteIdAndProjectId() {
		repository.save(
			ProjectInstallationJobEntity.builder()
				.correlationId(UUID.randomUUID())
				.siteId(siteId.id)
				.projectId(projectId.id)
				.status(2)
				.build()
		);
		repository.save(
			ProjectInstallationJobEntity.builder()
				.correlationId(UUID.randomUUID())
				.siteId(siteId.id)
				.projectId(projectId.id)
				.status(2)
				.build()
		);
		repository.save(
			ProjectInstallationJobEntity.builder()
				.correlationId(UUID.randomUUID())
				.siteId(siteId.id)
				.projectId(projectId.id)
				.status(2)
				.build()
		);
		repository.save(
			ProjectInstallationJobEntity.builder()
				.correlationId(UUID.randomUUID())
				.siteId(siteId.id)
				.projectId(projectId1.id)
				.status(2)
				.build()
		);
		repository.save(
			ProjectInstallationJobEntity.builder()
				.correlationId(UUID.randomUUID())
				.siteId(siteId.id)
				.projectId(projectId1.id)
				.status(2)
				.build()
		);
		repository.save(
			ProjectInstallationJobEntity.builder()
				.correlationId(UUID.randomUUID())
				.siteId(siteId1.id)
				.projectId(projectId.id)
				.status(2)
				.build()
		);
		repository.save(
			ProjectInstallationJobEntity.builder()
				.correlationId(UUID.randomUUID())
				.siteId(siteId1.id)
				.projectId(projectId.id)
				.status(2)
				.build()
		);
		repository.save(
			ProjectInstallationJobEntity.builder()
				.correlationId(UUID.randomUUID())
				.siteId(siteId1.id)
				.projectId(projectId1.id)
				.status(2)
				.build()
		);
		repository.save(
			ProjectInstallationJobEntity.builder()
				.correlationId(UUID.randomUUID())
				.siteId(siteId1.id)
				.projectId(projectId1.id)
				.status(2)
				.build()
		);

		V34__project_installation_duplicates_clearing.migrate(jdbcTemplate);

		Iterable<ProjectInstallationJobEntity> all = repository.findAll();
		assertEquals(all.spliterator().getExactSizeIfKnown(), 4);
		Set<Pair<UUID, UUID>> siteIdAndProjectIdPairs = stream(all.spliterator(), false)
			.map(entity -> Pair.of(entity.siteId, entity.projectId))
			.collect(Collectors.toSet());
		assertEquals(
			Set.of(
				Pair.of(siteId.id, projectId.id),
				Pair.of(siteId.id, projectId1.id),
				Pair.of(siteId1.id, projectId.id),
				Pair.of(siteId1.id, projectId1.id)
			),
			siteIdAndProjectIdPairs
		);
	}

	@Test
	void shouldReduceProjectInstallationsAndLeaveThoseWithBiggerStatus() {
		repository.save(
			ProjectInstallationJobEntity.builder()
				.correlationId(UUID.randomUUID())
				.siteId(siteId.id)
				.projectId(projectId.id)
				.status(2)
				.build()
		);
		ProjectInstallationJobEntity toNotRemove = repository.save(
			ProjectInstallationJobEntity.builder()
				.correlationId(UUID.randomUUID())
				.siteId(siteId.id)
				.projectId(projectId.id)
				.status(6)
				.build()
		);
		repository.save(
			ProjectInstallationJobEntity.builder()
				.correlationId(UUID.randomUUID())
				.siteId(siteId1.id)
				.projectId(projectId1.id)
				.status(3)
				.build()
		);
		ProjectInstallationJobEntity toNotRemove1 = repository.save(ProjectInstallationJobEntity.builder()
			.correlationId(UUID.randomUUID())
			.siteId(siteId1.id)
			.projectId(projectId1.id)
			.status(4)
			.build()
		);

		V34__project_installation_duplicates_clearing.migrate(jdbcTemplate);

		Iterable<ProjectInstallationJobEntity> all = repository.findAll();
		assertEquals(all.spliterator().getExactSizeIfKnown(), 2);
		Set<ProjectInstallationJobEntity> notRemoved = stream(all.spliterator(), false).collect(toSet());
		assertEquals(Set.of(toNotRemove, toNotRemove1), notRemoved);
	}
}