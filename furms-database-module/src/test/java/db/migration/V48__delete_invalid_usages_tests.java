/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package db.migration;

import io.imunity.furms.db.resource_usage.ResourceUsageDatabaseRepository;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_credits.ResourceCreditId;
import io.imunity.furms.domain.resource_types.ResourceMeasureType;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.services.InfraServiceId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
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
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class V48__delete_invalid_usages_tests {

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
	private ResourceUsageDatabaseRepository resourceUsageDatabaseRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private Flyway flyway;

	private SiteId siteId;
	private CommunityId communityId;
	private ProjectId projectId;
	private CommunityAllocationId communityAllocationId;
	private ResourceCreditId resourceCreditId;
	private ProjectAllocationId projectAllocationId;

	@BeforeEach
	void setUp() {
		flyway.clean();
		Flyway.configure()
				.configuration(flyway.getConfiguration())
				.target("48")
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

		resourceCreditId = resourceCreditRepository.create(ResourceCredit.builder()
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
			.name("name")
			.splittable(true)
			.amount(new BigDecimal(100))
			.utcCreateTime(LocalDateTime.now())
			.utcStartTime(LocalDateTime.now().plusDays(1))
			.utcEndTime(LocalDateTime.now().plusDays(3))
			.build());

		communityAllocationId = communityAllocationRepository.create(
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
	void shouldCleanUsagesWithNullCumulativeConsumption() {
		ProjectAllocationResolved projectAllocationResolved = ProjectAllocationResolved.builder()
			.site(Site.builder().id(siteId).build())
			.communityAllocation(CommunityAllocation.builder().id(communityAllocationId).communityId(communityId).build())
			.resourceCredit(ResourceCredit.builder().id(resourceCreditId).build())
			.build();
		resourceUsageDatabaseRepository.create(
			ResourceUsage.builder()
				.projectId(projectId)
				.projectAllocationId(projectAllocationId)
				.cumulativeConsumption(null)
				.probedAt(LocalDateTime.now().minusMinutes(5))
				.build(),
			projectAllocationResolved
		);

		V48__delete_invalid_usages_fix.migrate(jdbcTemplate);

		Set<ResourceUsage> resourceUsagesHistory =
			resourceUsageDatabaseRepository.findResourceUsagesHistory(projectAllocationId);
		assertThat(resourceUsagesHistory).isEmpty();

		Optional<ResourceUsage> currentResourceUsage =
			resourceUsageDatabaseRepository.findCurrentResourceUsage(projectAllocationId);
		assertThat(currentResourceUsage).isEmpty();
	}


	@Test
	void shouldCleanUserUsagesWithNullCumulativeConsumption() {
		FenixUserId userId = new FenixUserId("userId");
		resourceUsageDatabaseRepository.create(
			UserResourceUsage.builder()
				.projectId(projectId)
				.projectAllocationId(projectAllocationId)
				.fenixUserId(userId)
				.cumulativeConsumption(null)
				.consumedUntil(LocalDateTime.now().minusMinutes(5))
				.build()
		);

		V48__delete_invalid_usages_fix.migrate(jdbcTemplate);


		Set<UserResourceUsage> userResourceUsagesHistory =
			resourceUsageDatabaseRepository.findUserResourceUsagesHistory(projectAllocationId);
		assertThat(userResourceUsagesHistory).isEmpty();
	}
}