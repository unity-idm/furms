/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_usage;


import io.imunity.furms.db.DBIntegrationTest;
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
import io.imunity.furms.domain.resource_usage.ResourceUsageByCommunityAllocation;
import io.imunity.furms.domain.resource_usage.ResourceUsageByCredit;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ResourceUsageDatabaseRepositoryTest extends DBIntegrationTest {

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
	private ResourceUsageEntityRepository resourceUsageEntityRepository;
	@Autowired
	private ResourceUsageHistoryEntityRepository resourceUsageHistoryEntityRepository;
	@Autowired
	private UserResourceUsageEntityRepository userResourceUsageEntityRepository;
	@Autowired
	private UserResourceUsageHistoryEntityRepository userResourceUsageHistoryEntityRepository;
	@Autowired
	private ResourceUsageDatabaseRepository databaseRepository;

	private SiteId siteId;
	private CommunityId communityId;
	private ResourceCreditId resourceCreditId;
	private CommunityAllocationId communityAllocationId;
	private CommunityAllocationId communityAllocationId2;

	private ProjectId projectId;
	private ProjectId projectId2;

	private ProjectAllocationId projectAllocationId;
	private ProjectAllocationId projectAllocationId2;

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
			.utcStartTime(LocalDateTime.now().minusDays(1))
			.utcEndTime(LocalDateTime.now().plusDays(1))
			.build();
		Project project2 = Project.builder()
			.communityId(communityId)
			.name("name2")
			.logo(FurmsImage.empty())
			.description("new_description")
			.acronym("acronym")
			.researchField("research filed")
			.utcStartTime(LocalDateTime.now().minusDays(1))
			.utcEndTime(LocalDateTime.now().plusDays(1))
			.build();

		projectId = projectRepository.create(project);
		projectId2 = projectRepository.create(project2);

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
		communityAllocationId2 = communityAllocationRepository.create(
			CommunityAllocation.builder()
				.communityId(communityId)
				.resourceCreditId(resourceCreditId)
				.name("anem2")
				.amount(new BigDecimal(30))
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
		projectAllocationId2 = projectAllocationRepository.create(
			ProjectAllocation.builder()
				.projectId(projectId2)
				.communityAllocationId(communityAllocationId2)
				.name("anem2")
				.amount(new BigDecimal(30))
				.build()
		);
	}

	@Test
	void shouldCreateResourceUsage() {
		databaseRepository.create(
			ResourceUsage.builder()
				.projectId(projectId)
				.projectAllocationId(projectAllocationId)
				.cumulativeConsumption(BigDecimal.ONE)
				.probedAt(LocalDateTime.now().minusMinutes(5))
				.build(),
			ProjectAllocationResolved.builder()
				.site(Site.builder().id(siteId).build())
				.communityAllocation(CommunityAllocation.builder().id(communityAllocationId).communityId(communityId).build())
				.resourceCredit(ResourceCredit.builder().id(resourceCreditId).build())
				.build()
		);

		Optional<ResourceUsageEntity> resourceUsageEntity = resourceUsageEntityRepository.findByProjectAllocationId(projectAllocationId.id);
		assertTrue(resourceUsageEntity.isPresent());
		assertEquals(projectAllocationId.id, resourceUsageEntity.get().projectAllocationId);
		assertEquals(projectId.id, resourceUsageEntity.get().projectId);
		assertEquals(BigDecimal.ONE, resourceUsageEntity.get().cumulativeConsumption);

		Set<ResourceUsageHistoryEntity> resourceUsageHistoryEntities = resourceUsageHistoryEntityRepository.findAllByProjectAllocationId(projectAllocationId.id);
		assertEquals(1, resourceUsageHistoryEntities.size());
		ResourceUsageHistoryEntity resourceUsageHistoryEntity = resourceUsageHistoryEntities.iterator().next();
		assertEquals(projectAllocationId.id, resourceUsageHistoryEntity.projectAllocationId);
		assertEquals(projectId.id, resourceUsageHistoryEntity.projectId);
		assertEquals(BigDecimal.ONE, resourceUsageHistoryEntity.cumulativeConsumption);
	}

	@Test
	void shouldUpdateResourceUsage() {
		databaseRepository.create(
			ResourceUsage.builder()
				.projectId(projectId)
				.projectAllocationId(projectAllocationId)
				.cumulativeConsumption(BigDecimal.ONE)
				.probedAt(LocalDateTime.now().minusMinutes(5))
				.build(),
			ProjectAllocationResolved.builder()
				.site(Site.builder().id(siteId).build())
				.communityAllocation(CommunityAllocation.builder().id(communityAllocationId).communityId(communityId).build())
				.resourceCredit(ResourceCredit.builder().id(resourceCreditId).build())
				.build()

		);
		databaseRepository.create(
			ResourceUsage.builder()
				.projectId(projectId)
				.projectAllocationId(projectAllocationId)
				.cumulativeConsumption(BigDecimal.TEN)
				.probedAt(LocalDateTime.now().minusMinutes(1))
				.build(),
			ProjectAllocationResolved.builder()
				.site(Site.builder().id(siteId).build())
				.communityAllocation(CommunityAllocation.builder().id(communityAllocationId).communityId(communityId).build())
				.resourceCredit(ResourceCredit.builder().id(resourceCreditId).build())
				.build()
		);

		Optional<ResourceUsageEntity> resourceUsageEntity = resourceUsageEntityRepository.findByProjectAllocationId(projectAllocationId.id);
		assertTrue(resourceUsageEntity.isPresent());
		assertEquals(projectAllocationId.id, resourceUsageEntity.get().projectAllocationId);
		assertEquals(projectId.id, resourceUsageEntity.get().projectId);
		assertEquals(BigDecimal.TEN, resourceUsageEntity.get().cumulativeConsumption);

		Set<ResourceUsageHistoryEntity> resourceUsageHistoryEntities = resourceUsageHistoryEntityRepository.findAllByProjectAllocationId(projectAllocationId.id);
		assertEquals(2, resourceUsageHistoryEntities.size());
		Set<BigDecimal> resourceUsageHistoryAmounts = resourceUsageHistoryEntities.stream().map(x -> x.cumulativeConsumption).collect(toSet());
		assertTrue(resourceUsageHistoryAmounts.contains(BigDecimal.ONE));
		assertTrue(resourceUsageHistoryAmounts.contains(BigDecimal.TEN));

	}

	@Test
	void shouldCreateUserResourceUsage() {
		databaseRepository.create(
			UserResourceUsage.builder()
				.projectId(projectId)
				.projectAllocationId(projectAllocationId)
				.fenixUserId(new FenixUserId("userId"))
				.cumulativeConsumption(BigDecimal.ONE)
				.consumedUntil(LocalDateTime.now().minusMinutes(5))
				.build()
		);

		Optional<UserResourceUsageEntity> resourceUsageEntity = userResourceUsageEntityRepository.findByProjectAllocationId(projectAllocationId.id);
		assertTrue(resourceUsageEntity.isPresent());
		assertEquals(projectAllocationId.id, resourceUsageEntity.get().projectAllocationId);
		assertEquals(projectId.id, resourceUsageEntity.get().projectId);
		assertEquals("userId", resourceUsageEntity.get().fenixUserId);
		assertEquals(BigDecimal.ONE, resourceUsageEntity.get().cumulativeConsumption);

		Set<UserResourceUsageHistoryEntity> resourceUsageHistoryEntities = userResourceUsageHistoryEntityRepository.findAllByProjectAllocationId(projectAllocationId.id);
		assertEquals(1, resourceUsageHistoryEntities.size());
		UserResourceUsageHistoryEntity resourceUsageHistoryEntity = resourceUsageHistoryEntities.iterator().next();
		assertEquals(projectAllocationId.id, resourceUsageHistoryEntity.projectAllocationId);
		assertEquals(projectId.id, resourceUsageHistoryEntity.projectId);
		assertEquals("userId", resourceUsageHistoryEntity.fenixUserId);
		assertEquals(BigDecimal.ONE, resourceUsageHistoryEntity.cumulativeConsumption);
	}

	@Test
	void shouldUpdateUserResourceUsage() {
		databaseRepository.create(
			UserResourceUsage.builder()
				.projectId(projectId)
				.projectAllocationId(projectAllocationId)
				.fenixUserId(new FenixUserId("userId"))
				.cumulativeConsumption(BigDecimal.ONE)
				.consumedUntil(LocalDateTime.now().minusMinutes(5))
				.build()
		);
		databaseRepository.create(
			UserResourceUsage.builder()
				.projectId(projectId)
				.projectAllocationId(projectAllocationId)
				.fenixUserId(new FenixUserId("userId"))
				.cumulativeConsumption(BigDecimal.TEN)
				.consumedUntil(LocalDateTime.now().minusMinutes(5))
				.build()
		);

		Optional<UserResourceUsageEntity> resourceUsageEntity = userResourceUsageEntityRepository.findByProjectAllocationId(projectAllocationId.id);
		assertTrue(resourceUsageEntity.isPresent());
		assertEquals(projectAllocationId.id, resourceUsageEntity.get().projectAllocationId);
		assertEquals(projectId.id, resourceUsageEntity.get().projectId);
		assertEquals("userId", resourceUsageEntity.get().fenixUserId);
		assertEquals(BigDecimal.TEN, resourceUsageEntity.get().cumulativeConsumption);

		Set<UserResourceUsageHistoryEntity> resourceUsageHistoryEntities = userResourceUsageHistoryEntityRepository.findAllByProjectAllocationId(projectAllocationId.id);
		assertEquals(2, resourceUsageHistoryEntities.size());
		Set<BigDecimal> resourceUsageHistoryAmounts = resourceUsageHistoryEntities.stream().map(x -> x.cumulativeConsumption).collect(toSet());
		assertTrue(resourceUsageHistoryAmounts.contains(BigDecimal.ONE));
		assertTrue(resourceUsageHistoryAmounts.contains(BigDecimal.TEN));
	}

	@Test
	void shouldFindCurrentResourceUsages() {
		resourceUsageEntityRepository.save(
			ResourceUsageEntity.builder()
				.siteId(siteId.id)
				.communityId(communityId.id)
				.resourceCreditId(resourceCreditId.id)
				.communityAllocationId(communityAllocationId.id)
				.projectId(projectId.id)
				.projectAllocationId(projectAllocationId.id)
				.cumulativeConsumption(BigDecimal.ONE)
				.probedAt(LocalDateTime.now().minusMinutes(5))
				.build()
		);
		resourceUsageEntityRepository.save(
			ResourceUsageEntity.builder()
				.siteId(siteId.id)
				.communityId(communityId.id)
				.resourceCreditId(resourceCreditId.id)
				.communityAllocationId(communityAllocationId.id)
				.projectId(projectId.id)
				.projectAllocationId(projectAllocationId2.id)
				.cumulativeConsumption(BigDecimal.TEN)
				.probedAt(LocalDateTime.now().minusMinutes(5))
				.build()
		);

		Set<ResourceUsage> resourceUsageEntities = databaseRepository.findCurrentResourceUsages(projectId);
		assertEquals(2, resourceUsageEntities.size());
		Set<BigDecimal> resourceUsageHistoryAmounts = resourceUsageEntities.stream().map(x -> x.cumulativeConsumption).collect(toSet());
		assertTrue(resourceUsageHistoryAmounts.contains(BigDecimal.ONE));
		assertTrue(resourceUsageHistoryAmounts.contains(BigDecimal.TEN));
	}

	@Test
	void shouldFindCurrentResourceUsage() {
		ResourceUsageEntity saveEntity = resourceUsageEntityRepository.save(
			ResourceUsageEntity.builder()
				.siteId(siteId.id)
				.communityId(communityId.id)
				.resourceCreditId(resourceCreditId.id)
				.communityAllocationId(communityAllocationId.id)
				.projectId(projectId.id)
				.projectAllocationId(projectAllocationId.id)
				.cumulativeConsumption(BigDecimal.ONE)
				.probedAt(LocalDateTime.now().minusMinutes(5))
				.build()
		);

		Optional<ResourceUsage> resourceUsageEntities =
			databaseRepository.findCurrentResourceUsage(new ProjectAllocationId(saveEntity.projectAllocationId));
		assertTrue(resourceUsageEntities.isPresent());
		assertEquals(saveEntity.cumulativeConsumption, resourceUsageEntities.get().cumulativeConsumption);
		assertEquals(saveEntity.projectAllocationId, resourceUsageEntities.get().projectAllocationId.id);
		assertEquals(saveEntity.projectId, resourceUsageEntities.get().projectId.id);
	}

	@Test
	void shouldFindResourceUsagesSumGroupedByCommunityAllocationId() {
		resourceUsageEntityRepository.save(
			ResourceUsageEntity.builder()
				.siteId(siteId.id)
				.communityId(communityId.id)
				.resourceCreditId(resourceCreditId.id)
				.communityAllocationId(communityAllocationId.id)
				.projectId(projectId.id)
				.projectAllocationId(projectAllocationId.id)
				.cumulativeConsumption(BigDecimal.ONE)
				.probedAt(LocalDateTime.now().minusMinutes(5))
				.build()
		);
		resourceUsageEntityRepository.save(
			ResourceUsageEntity.builder()
				.siteId(siteId.id)
				.communityId(communityId.id)
				.resourceCreditId(resourceCreditId.id)
				.communityAllocationId(communityAllocationId.id)
				.projectId(projectId2.id)
				.projectAllocationId(projectAllocationId2.id)
				.cumulativeConsumption(BigDecimal.TEN)
				.probedAt(LocalDateTime.now().minusMinutes(5))
				.build()
		);

		ResourceUsageByCommunityAllocation resourceUsageSum = databaseRepository.findResourceUsagesSumsByCommunityId(communityId);
		assertEquals(BigDecimal.valueOf(11), resourceUsageSum.get(communityAllocationId));
	}

	@Test
	void shouldFindResourceUsagesSumGroupedByResourceCreditId() {
		resourceUsageEntityRepository.save(
			ResourceUsageEntity.builder()
				.siteId(siteId.id)
				.communityId(communityId.id)
				.resourceCreditId(resourceCreditId.id)
				.communityAllocationId(communityAllocationId.id)
				.projectId(projectId.id)
				.projectAllocationId(projectAllocationId.id)
				.cumulativeConsumption(BigDecimal.ONE)
				.probedAt(LocalDateTime.now().minusMinutes(5))
				.build()
		);
		resourceUsageEntityRepository.save(
			ResourceUsageEntity.builder()
				.siteId(siteId.id)
				.communityId(communityId.id)
				.resourceCreditId(resourceCreditId.id)
				.communityAllocationId(communityAllocationId2.id)
				.projectId(projectId2.id)
				.projectAllocationId(projectAllocationId2.id)
				.cumulativeConsumption(BigDecimal.TEN)
				.probedAt(LocalDateTime.now().minusMinutes(5))
				.build()
		);

		ResourceUsageByCredit resourceUsageSum = databaseRepository.findResourceUsagesSumsBySiteId(siteId);
		assertEquals(BigDecimal.valueOf(11), resourceUsageSum.get(resourceCreditId));
	}
}