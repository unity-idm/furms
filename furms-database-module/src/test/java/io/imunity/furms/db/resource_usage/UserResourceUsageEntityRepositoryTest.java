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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class UserResourceUsageEntityRepositoryTest extends DBIntegrationTest {

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
	private UserResourceUsageEntityRepository entityRepository;

	private ProjectId projectId;
	private ProjectId projectId2;

	private ProjectAllocationId projectAllocationId;
	private ProjectAllocationId projectAllocationId2;

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

		CommunityAllocationId communityAllocationId = communityAllocationRepository.create(
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
		UserResourceUsageEntity saveEntity = entityRepository.save(
			UserResourceUsageEntity.builder()
				.projectId(projectId.id)
				.projectAllocationId(projectAllocationId.id)
				.fenixUserId("userId")
				.cumulativeConsumption(BigDecimal.ONE)
				.consumedUntil(LocalDateTime.now().minusMinutes(5))
				.build()
		);

		Optional<UserResourceUsageEntity> resourceUsageEntities = entityRepository.findById(saveEntity.id);
		assertTrue(resourceUsageEntities.isPresent());
		assertEquals(saveEntity.id, resourceUsageEntities.get().id);
		assertEquals(saveEntity.cumulativeConsumption, resourceUsageEntities.get().cumulativeConsumption);
		assertEquals(saveEntity.projectAllocationId, resourceUsageEntities.get().projectAllocationId);
		assertEquals(saveEntity.projectId, resourceUsageEntities.get().projectId);
	}

	@Test
	void shouldUpdate() {
		UserResourceUsageEntity savedEntity = entityRepository.save(
			UserResourceUsageEntity.builder()
				.projectId(projectId.id)
				.projectAllocationId(projectAllocationId.id)
				.fenixUserId("userId")
				.cumulativeConsumption(BigDecimal.TEN)
				.consumedUntil(LocalDateTime.now().minusMinutes(5))
				.build()
		);
		UserResourceUsageEntity updatedEntity = entityRepository.save(
			UserResourceUsageEntity.builder()
				.id(savedEntity.id)
				.projectId(projectId.id)
				.projectAllocationId(projectAllocationId.id)
				.fenixUserId("userId")
				.cumulativeConsumption(BigDecimal.ONE)
				.consumedUntil(LocalDateTime.now().minusMinutes(5))
				.build()
		);

		Optional<UserResourceUsageEntity> resourceUsageEntities = entityRepository.findById(updatedEntity.id);
		assertTrue(resourceUsageEntities.isPresent());
		assertEquals(updatedEntity.id, resourceUsageEntities.get().id);
		assertEquals(updatedEntity.cumulativeConsumption, resourceUsageEntities.get().cumulativeConsumption);
		assertEquals(updatedEntity.projectAllocationId, resourceUsageEntities.get().projectAllocationId);
		assertEquals(updatedEntity.projectId, resourceUsageEntities.get().projectId);
	}

	@Test
	void shouldDelete() {
		UserResourceUsageEntity savedEntity = entityRepository.save(
			UserResourceUsageEntity.builder()
				.projectId(projectId.id)
				.projectAllocationId(projectAllocationId.id)
				.fenixUserId("userId")
				.cumulativeConsumption(BigDecimal.TEN)
				.consumedUntil(LocalDateTime.now().minusMinutes(5))
				.build()
		);

		entityRepository.deleteById(savedEntity.id);
		Optional<UserResourceUsageEntity> resourceUsageEntities = entityRepository.findById(savedEntity.id);

		assertFalse(resourceUsageEntities.isPresent());
	}

	@Test
	void shouldFindByProjectAllocationId() {
		UserResourceUsageEntity saveEntity = entityRepository.save(
			UserResourceUsageEntity.builder()
				.projectId(projectId.id)
				.projectAllocationId(projectAllocationId.id)
				.fenixUserId("userId")
				.cumulativeConsumption(BigDecimal.ONE)
				.consumedUntil(LocalDateTime.now().minusMinutes(5))
				.build()
		);
		entityRepository.save(
			UserResourceUsageEntity.builder()
				.projectId(projectId2.id)
				.projectAllocationId(projectAllocationId2.id)
				.fenixUserId("userId")
				.cumulativeConsumption(BigDecimal.ONE)
				.consumedUntil(LocalDateTime.now().minusMinutes(5))
				.build()
		);

		Optional<UserResourceUsageEntity> resourceUsageEntities = entityRepository.findByProjectAllocationId(saveEntity.projectAllocationId);
		assertTrue(resourceUsageEntities.isPresent());
		assertEquals(saveEntity.id, resourceUsageEntities.get().id);
		assertEquals(saveEntity.cumulativeConsumption, resourceUsageEntities.get().cumulativeConsumption);
		assertEquals(saveEntity.projectAllocationId, resourceUsageEntities.get().projectAllocationId);
		assertEquals(saveEntity.projectId, resourceUsageEntities.get().projectId);
	}
}