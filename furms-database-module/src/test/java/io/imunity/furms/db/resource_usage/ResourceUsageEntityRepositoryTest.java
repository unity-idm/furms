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

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ResourceUsageEntityRepositoryTest extends DBIntegrationTest {

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
	private ResourceUsageEntityRepository entityRepository;

	private UUID projectId;
	private UUID projectId2;

	private UUID projectAllocationId;
	private UUID projectAllocationId2;

	@BeforeEach
	void init() throws IOException {
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

		UUID communityAllocationId = UUID.fromString(communityAllocationRepository.create(
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
		ResourceUsageEntity saveEntity = entityRepository.save(
			ResourceUsageEntity.builder()
				.projectId(projectId)
				.projectAllocationId(projectAllocationId)
				.cumulativeConsumption(BigDecimal.ONE)
				.probedAt(LocalDateTime.now().minusMinutes(5))
				.build()
		);

		Optional<ResourceUsageEntity> resourceUsageEntities = entityRepository.findById(saveEntity.getId());
		assertTrue(resourceUsageEntities.isPresent());
		assertEquals(saveEntity.getId(), resourceUsageEntities.get().getId());
		assertEquals(saveEntity.cumulativeConsumption, resourceUsageEntities.get().cumulativeConsumption);
		assertEquals(saveEntity.projectAllocationId, resourceUsageEntities.get().projectAllocationId);
		assertEquals(saveEntity.projectId, resourceUsageEntities.get().projectId);
	}

	@Test
	void shouldUpdate() {
		ResourceUsageEntity savedEntity = entityRepository.save(
			ResourceUsageEntity.builder()
				.projectId(projectId)
				.projectAllocationId(projectAllocationId)
				.cumulativeConsumption(BigDecimal.TEN)
				.probedAt(LocalDateTime.now().minusMinutes(5))
				.build()
		);
		ResourceUsageEntity updatedEntity = entityRepository.save(
			ResourceUsageEntity.builder()
				.id(savedEntity.getId())
				.projectId(projectId)
				.projectAllocationId(projectAllocationId)
				.cumulativeConsumption(BigDecimal.ONE)
				.probedAt(LocalDateTime.now().minusMinutes(5))
				.build()
		);

		Optional<ResourceUsageEntity> resourceUsageEntities = entityRepository.findById(updatedEntity.getId());
		assertTrue(resourceUsageEntities.isPresent());
		assertEquals(updatedEntity.getId(), resourceUsageEntities.get().getId());
		assertEquals(updatedEntity.cumulativeConsumption, resourceUsageEntities.get().cumulativeConsumption);
		assertEquals(updatedEntity.projectAllocationId, resourceUsageEntities.get().projectAllocationId);
		assertEquals(updatedEntity.projectId, resourceUsageEntities.get().projectId);
	}

	@Test
	void shouldDelete() {
		ResourceUsageEntity savedEntity = entityRepository.save(
			ResourceUsageEntity.builder()
				.projectId(projectId)
				.projectAllocationId(projectAllocationId)
				.cumulativeConsumption(BigDecimal.TEN)
				.probedAt(LocalDateTime.now().minusMinutes(5))
				.build()
		);

		entityRepository.deleteById(savedEntity.getId());
		Optional<ResourceUsageEntity> resourceUsageEntities = entityRepository.findById(savedEntity.getId());

		assertFalse(resourceUsageEntities.isPresent());
	}

	@Test
	void shouldFindByProjectAllocationId() {
		ResourceUsageEntity saveEntity = entityRepository.save(
			ResourceUsageEntity.builder()
				.projectId(projectId)
				.projectAllocationId(projectAllocationId)
				.cumulativeConsumption(BigDecimal.ONE)
				.probedAt(LocalDateTime.now().minusMinutes(5))
				.build()
		);
		entityRepository.save(
			ResourceUsageEntity.builder()
				.projectId(projectId2)
				.projectAllocationId(projectAllocationId2)
				.cumulativeConsumption(BigDecimal.ONE)
				.probedAt(LocalDateTime.now().minusMinutes(5))
				.build()
		);

		Optional<ResourceUsageEntity> resourceUsageEntities = entityRepository.findByProjectAllocationId(saveEntity.projectAllocationId);
		assertTrue(resourceUsageEntities.isPresent());
		assertEquals(saveEntity.getId(), resourceUsageEntities.get().getId());
		assertEquals(saveEntity.cumulativeConsumption, resourceUsageEntities.get().cumulativeConsumption);
		assertEquals(saveEntity.projectAllocationId, resourceUsageEntities.get().projectAllocationId);
		assertEquals(saveEntity.projectId, resourceUsageEntities.get().projectId);
	}

	@Test
	void shouldFindAllByProjectId() {
		ResourceUsageEntity saveEntity = entityRepository.save(
			ResourceUsageEntity.builder()
				.projectId(projectId)
				.projectAllocationId(projectAllocationId)
				.cumulativeConsumption(BigDecimal.ONE)
				.probedAt(LocalDateTime.now().minusMinutes(5))
				.build()
		);
		entityRepository.save(
			ResourceUsageEntity.builder()
				.projectId(projectId2)
				.projectAllocationId(projectAllocationId2)
				.cumulativeConsumption(BigDecimal.ONE)
				.probedAt(LocalDateTime.now().minusMinutes(5))
				.build()
		);

		Set<ResourceUsageEntity> resourceUsageEntities = entityRepository.findAllByProjectId(saveEntity.projectId);
		assertEquals(1, resourceUsageEntities.size());
		ResourceUsageEntity entity = resourceUsageEntities.iterator().next();
		assertEquals(saveEntity.getId(), entity.getId());
		assertEquals(saveEntity.cumulativeConsumption, entity.cumulativeConsumption);
		assertEquals(saveEntity.projectAllocationId, entity.projectAllocationId);
		assertEquals(saveEntity.projectId, entity.projectId);
	}
}