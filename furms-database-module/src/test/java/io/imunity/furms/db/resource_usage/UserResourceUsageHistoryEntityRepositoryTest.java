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
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class UserResourceUsageHistoryEntityRepositoryTest extends DBIntegrationTest {

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
	private UserResourceUsageHistoryEntityRepository entityRepository;

	private ProjectId projectId;
	private ProjectId projectId2;

	private ProjectAllocationId projectAllocationId;
	private ProjectAllocationId projectAllocationId2;
	private CommunityAllocationId communityAllocationId;

	@BeforeEach
	void init() {
		Site site = Site.builder()
			.name("name")
			.build();
		SiteId siteId = siteRepository.create(site, new SiteExternalId("id"));

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

		communityAllocationId = communityAllocationRepository.create(
			CommunityAllocation.builder()
				.communityId(communityId)
				.resourceCreditId(resourceCreditId)
				.name("anem")
				.amount(new BigDecimal(10))
				.build()
		);
		CommunityAllocationId communityAllocationId2 = communityAllocationRepository.create(
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
	void shouldCreate() {
		UserResourceUsageHistoryEntity saveEntity = entityRepository.save(
			UserResourceUsageHistoryEntity.builder()
				.projectId(projectId.id)
				.projectAllocationId(projectAllocationId.id)
				.fenixUserId("userId")
				.cumulativeConsumption(BigDecimal.ONE)
				.consumedUntil(LocalDateTime.now().minusMinutes(5))
				.build()
		);

		Optional<UserResourceUsageHistoryEntity> resourceUsageEntities = entityRepository.findById(saveEntity.id);
		assertTrue(resourceUsageEntities.isPresent());
		assertEquals(saveEntity.id, resourceUsageEntities.get().id);
		assertEquals(saveEntity.cumulativeConsumption, resourceUsageEntities.get().cumulativeConsumption);
		assertEquals(saveEntity.projectAllocationId, resourceUsageEntities.get().projectAllocationId);
		assertEquals(saveEntity.projectId, resourceUsageEntities.get().projectId);
	}

	@Test
	void shouldDelete() {
		UserResourceUsageHistoryEntity savedEntity = entityRepository.save(
			UserResourceUsageHistoryEntity.builder()
				.projectId(projectId.id)
				.projectAllocationId(projectAllocationId.id)
				.fenixUserId("userId")
				.cumulativeConsumption(BigDecimal.TEN)
				.consumedUntil(LocalDateTime.now().minusMinutes(5))
				.build());

		entityRepository.deleteById(savedEntity.id);
		Optional<UserResourceUsageHistoryEntity> resourceUsageEntities = entityRepository.findById(savedEntity.id);

		assertFalse(resourceUsageEntities.isPresent());
	}

	@Test
	void shouldReturnUserResourceUsagesByAllocationIdAndOnlyInSpecificPeriod() {
		//given
		final ProjectAllocationId wrongAllocationId = projectAllocationRepository.create(
				ProjectAllocation.builder()
						.projectId(projectId2)
						.communityAllocationId(communityAllocationId)
						.name("anem2")
						.amount(new BigDecimal(30))
						.build());
		final String userId = "userId";
		final LocalDateTime from = LocalDateTime.of(2000, 10, 5, 0, 0);
		final LocalDateTime until = LocalDateTime.of(2000, 10, 10, 23, 59);

		createEntity(userId, projectAllocationId, from.minusMinutes(1));
		createEntity(userId, wrongAllocationId, from.minusMinutes(1));
		createEntity(userId, projectAllocationId, from);
		createEntity(userId, wrongAllocationId, from);
		createEntity(userId, projectAllocationId, from.plusDays(1));
		createEntity(userId, projectAllocationId2, from.plusDays(1));
		createEntity(userId, wrongAllocationId, from.plusDays(1));
		createEntity(userId, projectAllocationId, until.minusMinutes(1));
		createEntity(userId, projectAllocationId2, until.minusMinutes(1));
		createEntity(userId, wrongAllocationId, until.minusMinutes(1));
		createEntity(userId, projectAllocationId, until);
		createEntity(userId, wrongAllocationId, until);
		createEntity(userId, projectAllocationId, until.plusMinutes(1));
		createEntity(userId, projectAllocationId2, until.plusMinutes(1));
		createEntity(userId, wrongAllocationId, until.plusMinutes(1));

		//when
		final Set<UserResourceUsageHistoryEntity> userUsageHistory = entityRepository
				.findAllByProjectAllocationIdInAndInPeriod(Set.of(projectAllocationId.id, projectAllocationId2.id), from,
					until);

		//then
		assertThat(userUsageHistory).hasSize(6);
		assertThat(userUsageHistory.stream()
				.allMatch(history -> List.of(projectAllocationId.id, projectAllocationId2.id).contains(history.projectAllocationId)))
				.isTrue();
		assertThat(userUsageHistory.stream()
				.allMatch(history -> !history.consumedUntil.isBefore(from) && !history.consumedUntil.isAfter(until)))
				.isTrue();

	}

	private void createEntity(String userId, ProjectAllocationId allocationId, LocalDateTime consumedUntil) {
		entityRepository.save(UserResourceUsageHistoryEntity.builder()
			.projectId(projectId.id)
			.projectAllocationId(allocationId.id)
			.fenixUserId(userId)
			.cumulativeConsumption(BigDecimal.TEN)
			.consumedUntil(consumedUntil)
			.build());
	}

}