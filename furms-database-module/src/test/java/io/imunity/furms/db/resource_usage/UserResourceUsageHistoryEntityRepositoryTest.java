/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_usage;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_types.ResourceMeasureType;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
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
import java.util.UUID;

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

	private UUID projectId;
	private UUID projectId2;

	private UUID projectAllocationId;
	private UUID projectAllocationId2;
	private UUID communityAllocationId;

	@BeforeEach
	void init() {
		Site site = Site.builder()
			.name("name")
			.build();
		UUID siteId = UUID.fromString(siteRepository.create(site, new SiteExternalId("id")));

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
			.utcStartTime(LocalDateTime.now().minusDays(1))
			.utcEndTime(LocalDateTime.now().plusDays(1))
			.build();
		Project project2 = Project.builder()
			.communityId(communityId.toString())
			.name("name2")
			.logo(FurmsImage.empty())
			.description("new_description")
			.acronym("acronym")
			.researchField("research filed")
			.utcStartTime(LocalDateTime.now().minusDays(1))
			.utcEndTime(LocalDateTime.now().plusDays(1))
			.build();

		projectId = UUID.fromString(projectRepository.create(project));
		projectId2 = UUID.fromString(projectRepository.create(project2));

		InfraService service = InfraService.builder()
			.siteId(siteId.toString())
			.name("name")
			.build();

		UUID serviceId = UUID.fromString(infraServiceRepository.create(service));

		ResourceType resourceType = ResourceType.builder()
			.siteId(siteId.toString())
			.serviceId(serviceId.toString())
			.name("name")
			.type(ResourceMeasureType.FLOATING_POINT)
			.unit(ResourceMeasureUnit.KILO)
			.build();
		UUID resourceTypeId = UUID.fromString(resourceTypeRepository.create(resourceType));

		UUID resourceCreditId = UUID.fromString(resourceCreditRepository.create(ResourceCredit.builder()
			.siteId(siteId.toString())
			.resourceTypeId(resourceTypeId.toString())
			.name("name")
			.splittable(true)
			.amount(new BigDecimal(100))
			.utcCreateTime(LocalDateTime.now())
			.utcStartTime(LocalDateTime.now().plusDays(1))
			.utcEndTime(LocalDateTime.now().plusDays(3))
			.build()));

		communityAllocationId = UUID.fromString(communityAllocationRepository.create(
			CommunityAllocation.builder()
				.communityId(communityId.toString())
				.resourceCreditId(resourceCreditId.toString())
				.name("anem")
				.amount(new BigDecimal(10))
				.build()
		));
		UUID communityAllocationId2 = UUID.fromString(communityAllocationRepository.create(
			CommunityAllocation.builder()
				.communityId(communityId.toString())
				.resourceCreditId(resourceCreditId.toString())
				.name("anem2")
				.amount(new BigDecimal(30))
				.build()
		));

		projectAllocationId = UUID.fromString(projectAllocationRepository.create(
			ProjectAllocation.builder()
				.projectId(projectId.toString())
				.communityAllocationId(communityAllocationId.toString())
				.name("anem")
				.amount(new BigDecimal(5))
				.build()
		));
		projectAllocationId2 = UUID.fromString(projectAllocationRepository.create(
			ProjectAllocation.builder()
				.projectId(projectId2.toString())
				.communityAllocationId(communityAllocationId2.toString())
				.name("anem2")
				.amount(new BigDecimal(30))
				.build()
		));
	}

	@Test
	void shouldCreate() {
		UserResourceUsageHistoryEntity saveEntity = entityRepository.save(
			UserResourceUsageHistoryEntity.builder()
				.projectId(projectId)
				.projectAllocationId(projectAllocationId)
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
				.projectId(projectId)
				.projectAllocationId(projectAllocationId)
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
		final UUID wrongAllocationId = UUID.fromString(projectAllocationRepository.create(
				ProjectAllocation.builder()
						.projectId(projectId2.toString())
						.communityAllocationId(communityAllocationId.toString())
						.name("anem2")
						.amount(new BigDecimal(30))
						.build()));
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
				.findAllByProjectAllocationIdInAndInPeriod(Set.of(projectAllocationId, projectAllocationId2), from, until);

		//then
		assertThat(userUsageHistory).hasSize(6);
		assertThat(userUsageHistory.stream()
				.allMatch(history -> List.of(projectAllocationId, projectAllocationId2).contains(history.projectAllocationId)))
				.isTrue();
		assertThat(userUsageHistory.stream()
				.allMatch(history -> !history.consumedUntil.isBefore(from) && !history.consumedUntil.isAfter(until)))
				.isTrue();

	}

	private void createEntity(String userId, UUID allocationId, LocalDateTime consumedUntil) {
		entityRepository.save(UserResourceUsageHistoryEntity.builder()
			.projectId(projectId)
			.projectAllocationId(allocationId)
			.fenixUserId(userId)
			.cumulativeConsumption(BigDecimal.TEN)
			.consumedUntil(consumedUntil)
			.build());
	}

}