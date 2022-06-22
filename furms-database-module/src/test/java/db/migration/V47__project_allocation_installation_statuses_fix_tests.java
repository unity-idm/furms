/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package db.migration;

import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_credits.ResourceCreditId;
import io.imunity.furms.domain.resource_types.ResourceMeasureType;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.services.InfraServiceId;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus.ACKNOWLEDGED;
import static io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus.INSTALLED;
import static io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus.UPDATING;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class V47__project_allocation_installation_statuses_fix_tests {

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
	private ProjectAllocationInstallationRepository allocationRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private Flyway flyway;

	private SiteId siteId;
	private ProjectAllocationId projectAllocationId;

	@BeforeEach
	void setUp() {
		flyway.clean();
		Flyway.configure()
				.configuration(flyway.getConfiguration())
				.target("46")
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

		ProjectId projectId = projectRepository.create(project);

		InfraService service = InfraService.builder()
			.siteId(siteId)
			.name("name")
			.build();

		InfraServiceId serviceId = infraServiceRepository.create(service);

		ResourceType resourceType = ResourceType.builder()
			.siteId(siteId)
			.serviceId(serviceId)
			.name("name")
			.type(ResourceMeasureType.FLOATING_POINT)
			.unit(ResourceMeasureUnit.KILO)
			.build();
		ResourceTypeId resourceTypeId = resourceTypeRepository.create(resourceType);

		ResourceCreditId resourceCreditId = resourceCreditRepository.create(ResourceCredit.builder()
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
			.name("name")
			.splittable(true)
			.amount(new BigDecimal(100))
			.utcCreateTime(LocalDateTime.now())
			.utcStartTime(LocalDateTime.now().plusDays(1))
			.utcEndTime(LocalDateTime.now().plusDays(3))
			.build());

		CommunityAllocationId communityAllocationId = communityAllocationRepository.create(
			CommunityAllocation.builder()
				.communityId(communityId)
				.resourceCreditId(resourceCreditId)
				.name("anem")
				.amount(new BigDecimal(10))
				.build()
		);

		projectAllocationId = projectAllocationRepository.create(
			ProjectAllocation.builder()
				.projectId(projectId)
				.communityAllocationId(communityAllocationId)
				.name("anem")
				.amount(new BigDecimal(5))
				.build()
		);
	}

	@AfterEach
	void tearDown() {
		flyway.clean();
		flyway.migrate();
	}

	@Test
	void shouldNotMigrateStatusAcknowledgedToInstallIfAllocationHasNoChunks() {
		ProjectAllocationInstallation request = ProjectAllocationInstallation.builder()
			.correlationId(CorrelationId.randomID())
			.siteId(siteId)
			.projectAllocationId(projectAllocationId)
			.status(ACKNOWLEDGED)
			.build();

		allocationRepository.create(request);

		V47__project_allocation_installation_statuses_fix.migrate(jdbcTemplate);

		assertEquals(allocationRepository.findByProjectAllocationId(projectAllocationId).status,
			ACKNOWLEDGED);
	}

	@Test
	void shouldNotMigrateStatusUpdatingToInstall() {
		ProjectAllocationInstallation request = ProjectAllocationInstallation.builder()
			.correlationId(CorrelationId.randomID())
			.siteId(siteId)
			.projectAllocationId(projectAllocationId)
			.status(UPDATING)
			.build();
		ProjectAllocationChunk chunk = ProjectAllocationChunk.builder()
			.projectAllocationId(projectAllocationId)
			.chunkId("id")
			.amount(BigDecimal.TEN)
			.validTo(LocalDateTime.now().minusDays(2))
			.validFrom(LocalDateTime.now().plusDays(2))
			.build();

		allocationRepository.create(request);
		allocationRepository.create(chunk);

		V47__project_allocation_installation_statuses_fix.migrate(jdbcTemplate);

		assertEquals(allocationRepository.findByProjectAllocationId(projectAllocationId).status,
			UPDATING);
	}

	@Test
	void shouldMigrateStatusAcknowledgedToInstallIfAllocationHasOneChunks() {
		ProjectAllocationInstallation request = ProjectAllocationInstallation.builder()
			.correlationId(CorrelationId.randomID())
			.siteId(siteId)
			.projectAllocationId(projectAllocationId)
			.status(ACKNOWLEDGED)
			.build();
		ProjectAllocationChunk chunk = ProjectAllocationChunk.builder()
			.projectAllocationId(projectAllocationId)
			.chunkId("id")
			.amount(BigDecimal.TEN)
			.validTo(LocalDateTime.now().minusDays(2))
			.validFrom(LocalDateTime.now().plusDays(2))
			.build();

		allocationRepository.create(request);
		allocationRepository.create(chunk);

		V47__project_allocation_installation_statuses_fix.migrate(jdbcTemplate);

		assertEquals(allocationRepository.findByProjectAllocationId(projectAllocationId).status,
			INSTALLED);
	}

	@Test
	void shouldMigrateStatusAcknowledgedToInstallIfAllocationHasTwoChunks() {
		ProjectAllocationInstallation request = ProjectAllocationInstallation.builder()
			.correlationId(CorrelationId.randomID())
			.siteId(siteId)
			.projectAllocationId(projectAllocationId)
			.status(ACKNOWLEDGED)
			.build();
		ProjectAllocationChunk chunk = ProjectAllocationChunk.builder()
			.projectAllocationId(projectAllocationId)
			.chunkId("id")
			.amount(BigDecimal.TEN)
			.validTo(LocalDateTime.now().minusDays(2))
			.validFrom(LocalDateTime.now().plusDays(2))
			.build();
		ProjectAllocationChunk chunk2 = ProjectAllocationChunk.builder()
			.projectAllocationId(projectAllocationId)
			.chunkId("id2")
			.amount(BigDecimal.TEN)
			.validTo(LocalDateTime.now().minusDays(2))
			.validFrom(LocalDateTime.now().plusDays(2))
			.build();

		allocationRepository.create(request);
		allocationRepository.create(chunk);
		allocationRepository.create(chunk2);

		V47__project_allocation_installation_statuses_fix.migrate(jdbcTemplate);

		assertEquals(allocationRepository.findByProjectAllocationId(projectAllocationId).status,
			INSTALLED);
	}
}