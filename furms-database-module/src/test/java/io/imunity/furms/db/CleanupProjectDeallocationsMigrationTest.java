/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db;

import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationStatus;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_types.ResourceMeasureType;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.project_allocation_installation.ProjectAllocationInstallationRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.services.InfraServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationStatus.ACKNOWLEDGED;
import static io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationStatus.FAILED;
import static io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class CleanupProjectDeallocationsMigrationTest extends DBIntegrationTest {

	private final static Integer CLEANUP_MIGRATION_VERSION = 40;

	@Autowired
	private Flyway flyway;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private CommunityRepository communityRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private InfraServiceRepository infraServiceRepository;

	@Autowired
	private ResourceTypeRepository resourceTypeRepository;

	@Autowired
	private ResourceCreditRepository resourceCreditRepository;

	@Autowired
	private CommunityAllocationRepository communityAllocationRepository;

	@Autowired
	private ProjectAllocationRepository projectAllocationRepository;

	@Autowired
	private ProjectAllocationInstallationRepository projectAllocationInstallationRepository;

	private String projectId;
	private String multipleOnlyPending;
	private String multipleWithAllThreeStatuses;
	private String multipleWithPendingAndFailed;
	private String singlePending;

	@AfterEach
	void tearDown() {
		flyway.clean();
		flyway.migrate();
	}

	@Test
	void shouldCleanupProjectDeallocationTableFromIllegalRecords() {
		initializeFlywayWithVersionBeforeMigration();
		loadRecords();

		final Set<ProjectDeallocation> beforeMigrationResults = projectAllocationInstallationRepository.findAllDeallocation(projectId);
		assertThat(beforeMigrationResults).hasSize(9);

		runMigration();

		final Set<ProjectDeallocation> results = projectAllocationInstallationRepository.findAllDeallocation(projectId);
		assertThat(results).hasSize(6);
		assertThat(results.stream()
				.allMatch(deallocation ->
					assertDeallocationForSpecificAllocationIsInStatus(deallocation, multipleOnlyPending, PENDING)
					|| assertDeallocationForSpecificAllocationIsInStatus(deallocation, multipleWithAllThreeStatuses, ACKNOWLEDGED, FAILED)
					|| assertDeallocationForSpecificAllocationIsInStatus(deallocation, multipleWithPendingAndFailed, PENDING, FAILED)
					|| assertDeallocationForSpecificAllocationIsInStatus(deallocation, singlePending, PENDING)))
				.isTrue();
	}

	private void initializeFlywayWithVersionBeforeMigration() {
		flyway.clean();
		Flyway.configure()
				.configuration(flyway.getConfiguration())
				.target(Integer.toString(CLEANUP_MIGRATION_VERSION - 1))
				.load()
				.migrate();
	}

	private void runMigration() {
		Flyway.configure()
				.configuration(flyway.getConfiguration())
				.target(CLEANUP_MIGRATION_VERSION.toString())
				.load()
				.migrate();
	}

	private boolean assertDeallocationForSpecificAllocationIsInStatus(ProjectDeallocation deallocation,
	                                                                  String expectedProjectAllocationId,
	                                                                  ProjectDeallocationStatus... statuses) {
		return deallocation.projectAllocationId.equals(expectedProjectAllocationId)
				&& Stream.of(statuses)
				.anyMatch(status -> status == deallocation.status);
	}

	private void loadRecords() {
		Site site = Site.builder()
				.name("siteName")
				.build();
		final String siteId = siteRepository.create(site, new SiteExternalId("id"));
		final String serviceId = infraServiceRepository.create(InfraService.builder()
				.siteId(siteId)
				.name("name")
				.build());
		final String resourceTypeId = resourceTypeRepository.create(ResourceType.builder()
				.siteId(siteId)
				.serviceId(serviceId)
				.name("name")
				.type(ResourceMeasureType.FLOATING_POINT)
				.unit(ResourceMeasureUnit.KILO)
				.build());
		final String resourceCreditId = resourceCreditRepository.create(ResourceCredit.builder()
				.siteId(siteId)
				.resourceTypeId(resourceTypeId)
				.name("name")
				.splittable(true)
				.amount(new BigDecimal(100))
				.utcCreateTime(LocalDateTime.now())
				.utcStartTime(LocalDateTime.now().plusDays(1))
				.utcEndTime(LocalDateTime.now().plusDays(3))
				.build());

		final String communityId = communityRepository.create(Community.builder()
				.name("communityName")
				.logo(FurmsImage.empty())
				.build());

		projectId = projectRepository.create(Project.builder()
				.communityId(communityId)
				.name("projectName")
				.description("new_description")
				.logo(FurmsImage.empty())
				.acronym("acronym")
				.researchField("research filed")
				.utcStartTime(LocalDateTime.now().minusMinutes(1))
				.utcEndTime(LocalDateTime.now().minusMinutes(1))
				.build());

		final String communityAllocationId = communityAllocationRepository.create(CommunityAllocation.builder()
				.communityId(communityId)
				.resourceCreditId(resourceCreditId)
				.name("name")
				.amount(new BigDecimal(10))
				.build());

		multipleOnlyPending = createProjectAllocation(projectId, communityAllocationId);
		multipleWithAllThreeStatuses = createProjectAllocation(projectId, communityAllocationId);
		multipleWithPendingAndFailed = createProjectAllocation(projectId, communityAllocationId);
		singlePending = createProjectAllocation(projectId, communityAllocationId);

		createProjectDeallocation(siteId, multipleOnlyPending, PENDING);
		createProjectDeallocation(siteId, multipleOnlyPending, PENDING);
		createProjectDeallocation(siteId, multipleOnlyPending, PENDING);
		createProjectDeallocation(siteId, multipleWithAllThreeStatuses, PENDING);
		createProjectDeallocation(siteId, multipleWithAllThreeStatuses, ACKNOWLEDGED);
		createProjectDeallocation(siteId, multipleWithAllThreeStatuses, FAILED);
		createProjectDeallocation(siteId, multipleWithPendingAndFailed, PENDING);
		createProjectDeallocation(siteId, multipleWithPendingAndFailed, FAILED);
		createProjectDeallocation(siteId, singlePending, PENDING);
	}

	private String createProjectAllocation(final String projectId, final String communityAllocationId) {
		return projectAllocationRepository.create(ProjectAllocation.builder()
				.projectId(projectId)
				.communityAllocationId(communityAllocationId)
				.name(UUID.randomUUID().toString())
				.amount(new BigDecimal(1))
				.build());
	}

	private void createProjectDeallocation(final String siteId,
	                                                      final String projectAllocationId,
	                                                      final ProjectDeallocationStatus status) {
		projectAllocationInstallationRepository.create(ProjectDeallocation.builder()
				.correlationId(new CorrelationId(UUID.randomUUID().toString()))
				.siteId(siteId)
				.projectAllocationId(projectAllocationId)
				.status(status)
				.build());
	}
}
