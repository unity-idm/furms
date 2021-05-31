/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_allocation;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.images.FurmsImage;
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

import static io.imunity.furms.db.id.uuid.UUIDIdUtils.generateId;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProjectAllocationEntityRepositoryTest extends DBIntegrationTest {

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
	private ProjectAllocationEntityRepository entityRepository;
	@Autowired
	private ProjectAllocationReadEntityRepository entityReadRepository;

	private UUID siteId;

	private UUID communityId;

	private UUID projectId;
	private UUID projectId2;

	private UUID resourceTypeId;

	private UUID resourceCreditId;

	private UUID communityAllocationId;
	private UUID communityAllocationId2;

	private LocalDateTime startTime = LocalDateTime.of(2020, 5, 20, 5, 12, 16);
	private LocalDateTime endTime = LocalDateTime.of(2021, 6, 21, 4, 18, 4);
	private LocalDateTime newStartTime = LocalDateTime.of(2020, 8, 3, 4, 7, 5);
	private LocalDateTime newEndTime = LocalDateTime.of(2021, 9, 13, 3, 35, 33);

	@BeforeEach
	void init() throws IOException {
		Site site = Site.builder()
			.name("name")
			.build();
		siteId = UUID.fromString(siteRepository.create(site, new SiteExternalId("id")));

		Community community = Community.builder()
			.name("name")
			.logo(FurmsImage.empty())
			.build();
		communityId = UUID.fromString(communityRepository.create(community));

		Project project = Project.builder()
			.communityId(communityId.toString())
			.name("name")
			.description("new_description")
			.logo(FurmsImage.empty())
			.acronym("acronym")
			.researchField("research filed")
			.utcStartTime(startTime)
			.utcEndTime(endTime)
			.build();
		Project project2 = Project.builder()
			.communityId(communityId.toString())
			.name("name2")
			.logo(FurmsImage.empty())
			.description("new_description")
			.acronym("acronym")
			.researchField("research filed")
			.utcStartTime(newStartTime)
			.utcEndTime(newEndTime)
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
		resourceTypeId = UUID.fromString(resourceTypeRepository.create(resourceType));

		resourceCreditId = UUID.fromString(resourceCreditRepository.create(ResourceCredit.builder()
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
		communityAllocationId2 = UUID.fromString(communityAllocationRepository.create(
			CommunityAllocation.builder()
				.communityId(communityId.toString())
				.resourceCreditId(resourceCreditId.toString())
				.name("anem2")
				.amount(new BigDecimal(30))
				.build()
		));
	}

	@Test
	void shouldReturnAvailableAmountWhenProjectAllocationsDoesntExist() {
		BigDecimal sum = entityReadRepository.calculateAvailableAmount(communityAllocationId).getAmount();
		assertThat(sum).isEqualTo(new BigDecimal(10));
	}

	@Test
	void shouldReturnAvailableAmount() {
		entityRepository.save(
			ProjectAllocationEntity.builder()
				.projectId(projectId)
				.communityAllocationId(communityAllocationId)
				.name("anem")
				.amount(new BigDecimal(5))
				.build()
		);
		entityRepository.save(
			ProjectAllocationEntity.builder()
				.projectId(projectId)
				.communityAllocationId(communityAllocationId2)
				.name("anem2")
				.amount(new BigDecimal(30))
				.build()
		);

		BigDecimal sum = entityReadRepository.calculateAvailableAmount(communityAllocationId).getAmount();
		assertThat(sum).isEqualTo(new BigDecimal(5));
	}

	@Test
	void shouldReturnAllocationWithRelatedObjects() {
		ProjectAllocationEntity save = entityRepository.save(
			ProjectAllocationEntity.builder()
				.projectId(projectId)
				.communityAllocationId(communityAllocationId)
				.name("anem")
				.amount(new BigDecimal(10))
				.build()
		);

		Optional<ProjectAllocationReadEntity> entity = entityReadRepository.findById(save.getId());
		assertThat(entity).isPresent();
		assertThat(entity.get().name).isEqualTo("anem");
		assertThat(entity.get().amount).isEqualTo(new BigDecimal(10));
		assertThat(entity.get().site.getName()).isEqualTo("name");
		assertThat(entity.get().resourceType.type).isEqualTo(ResourceMeasureType.FLOATING_POINT);
		assertThat(entity.get().resourceType.unit).isEqualTo(ResourceMeasureUnit.KILO);
		assertThat(entity.get().resourceCredit.name).isEqualTo("name");
		assertThat(entity.get().resourceCredit.split).isEqualTo(true);
		assertThat(entity.get().resourceCredit.amount).isEqualTo(new BigDecimal(100));
	}

	@Test
	void shouldReturnAllocationsWithRelatedObjects() {
		entityRepository.save(
			ProjectAllocationEntity.builder()
				.projectId(projectId)
				.communityAllocationId(communityAllocationId)
				.name("anem")
				.amount(new BigDecimal(10))
				.build()
		);

		Set<ProjectAllocationReadEntity> entities = entityReadRepository.findAllByProjectId(projectId);
		assertThat(entities.size()).isEqualTo(1);
		ProjectAllocationReadEntity entity = entities.iterator().next();
		assertThat(entity.name).isEqualTo("anem");
		assertThat(entity.amount).isEqualTo(new BigDecimal(10));
		assertThat(entity.site.getName()).isEqualTo("name");
		assertThat(entity.resourceType.type).isEqualTo(ResourceMeasureType.FLOATING_POINT);
		assertThat(entity.resourceType.unit).isEqualTo(ResourceMeasureUnit.KILO);
		assertThat(entity.resourceCredit.name).isEqualTo("name");
		assertThat(entity.resourceCredit.split).isEqualTo(true);
		assertThat(entity.resourceCredit.amount).isEqualTo(new BigDecimal(100));
	}


	@Test
	void shouldCreateResourceType() {
		//given
		ProjectAllocationEntity entityToSave = ProjectAllocationEntity.builder()
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("name")
			.amount(new BigDecimal(10))
			.build();

		//when
		ProjectAllocationEntity saved = entityRepository.save(entityToSave);

		//then
		assertThat(entityRepository.findAll()).hasSize(1);
		Optional<ProjectAllocationEntity> byId = entityRepository.findById(saved.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().projectId).isEqualTo(projectId);
		assertThat(byId.get().communityAllocationId).isEqualTo(communityAllocationId);
		assertThat(byId.get().name).isEqualTo("name");
		assertThat(byId.get().amount).isEqualTo(new BigDecimal(10));
	}

	@Test
	void shouldUpdateProjectAllocation() {
		//given
		ProjectAllocationEntity old = ProjectAllocationEntity.builder()
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("anem")
			.amount(new BigDecimal(10))
			.build();
		entityRepository.save(old);
		ProjectAllocationEntity toUpdate = ProjectAllocationEntity.builder()
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("anem2")
			.amount(new BigDecimal(102))
			.build();

		//when
		entityRepository.save(toUpdate);

		//then
		Optional<ProjectAllocationEntity> byId = entityRepository.findById(toUpdate.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().projectId).isEqualTo(projectId);
		assertThat(byId.get().communityAllocationId).isEqualTo(communityAllocationId);
		assertThat(byId.get().name).isEqualTo("anem2");
		assertThat(byId.get().amount).isEqualTo(new BigDecimal(102));
	}

	@Test
	void shouldFindCreatedProjectAllocations() {
		//given
		ProjectAllocationEntity toFind = ProjectAllocationEntity.builder()
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("anem")
			.amount(new BigDecimal(10))
			.build();
		entityRepository.save(toFind);

		//when
		Optional<ProjectAllocationEntity> byId = entityRepository.findById(toFind.getId());

		//then
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindAllAvailableProjectAllocations() {
		//given
		entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("anem")
			.amount(new BigDecimal(10))
			.build()
		);
		entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId2)
			.communityAllocationId(communityAllocationId2)
			.name("anem2")
			.amount(new BigDecimal(120))
			.build()
		);

		//when
		Iterable<ProjectAllocationEntity> all = entityRepository.findAll();

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void savedServiceExistsByProjectAllocationId() {
		//given
		ProjectAllocationEntity service = entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("anem")
			.amount(new BigDecimal(10))
			.build());

		//when + then
		assertThat(entityRepository.existsById(service.getId())).isTrue();
		assertThat(entityRepository.existsById(generateId())).isFalse();
	}

	@Test
	void savedProjectAllocationExistsByName() {
		//given
		ProjectAllocationEntity service = entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("anem")
			.amount(new BigDecimal(10))
			.build());

		//when
		boolean exists = entityReadRepository.existsByCommunityIdAndName(communityId, service.name);

		//then
		assertThat(exists).isTrue();
	}

	@Test
	void savedProjectAllocationDoesNotExistByName() {
		//given
		entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("anem")
			.amount(new BigDecimal(10))
			.build());

		//when
		boolean nonExists = entityReadRepository.existsByCommunityIdAndName(communityId, "wrong_name");

		//then
		assertThat(nonExists).isFalse();
	}

	@Test
	void shouldDeleteService() {
		//given
		ProjectAllocationEntity entityToRemove = entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("anem")
			.amount(new BigDecimal(10))
			.build());

		//when
		entityRepository.deleteById(entityToRemove.getId());

		//then
		assertThat(entityRepository.findAll()).hasSize(0);
	}

	@Test
	void shouldDeleteAllServices() {
		//given
		entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("anem")
			.amount(new BigDecimal(10))
			.build());
		entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId2)
			.communityAllocationId(communityAllocationId2)
			.name("anem2")
			.amount(new BigDecimal(10))
			.build());

		//when
		entityRepository.deleteAll();

		//then
		assertThat(entityRepository.findAll()).hasSize(0);
	}

}