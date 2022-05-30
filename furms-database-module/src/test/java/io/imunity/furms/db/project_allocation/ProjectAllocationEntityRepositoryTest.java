/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_allocation;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.images.FurmsImage;
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

	private CommunityId communityId;

	private ProjectId projectId;
	private ProjectId projectId2;

	private CommunityAllocationId communityAllocationId;
	private CommunityAllocationId communityAllocationId2;

	private final LocalDateTime startTime = LocalDateTime.of(2020, 5, 20, 5, 12, 16);
	private final LocalDateTime endTime = LocalDateTime.of(2021, 6, 21, 4, 18, 4);
	private final LocalDateTime newStartTime = LocalDateTime.of(2020, 8, 3, 4, 7, 5);
	private final LocalDateTime newEndTime = LocalDateTime.of(2021, 9, 13, 3, 35, 33);

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
		communityId = communityRepository.create(community);

		Project project = Project.builder()
			.communityId(communityId)
			.name("name")
			.description("new_description")
			.logo(FurmsImage.empty())
			.acronym("acronym")
			.researchField("research filed")
			.utcStartTime(startTime)
			.utcEndTime(endTime)
			.build();
		Project project2 = Project.builder()
			.communityId(communityId)
			.name("name2")
			.logo(FurmsImage.empty())
			.description("new_description")
			.acronym("acronym")
			.researchField("research filed")
			.utcStartTime(newStartTime)
			.utcEndTime(newEndTime)
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
		communityAllocationId2 = communityAllocationRepository.create(
			CommunityAllocation.builder()
				.communityId(communityId)
				.resourceCreditId(resourceCreditId)
				.name("anem2")
				.amount(new BigDecimal(30))
				.build()
		);
	}

	@Test
	void shouldReturnAvailableAmountWhenProjectAllocationsDoesntExist() {
		BigDecimal sum = entityReadRepository.calculateAvailableAmount(communityAllocationId.id).getAmount();
		assertThat(sum).isEqualTo(new BigDecimal(10));
	}

	@Test
	void shouldReturnAvailableAmount() {
		entityRepository.save(
			ProjectAllocationEntity.builder()
				.projectId(projectId.id)
				.communityAllocationId(communityAllocationId.id)
				.name("anem")
				.amount(new BigDecimal(5))
				.build()
		);
		entityRepository.save(
			ProjectAllocationEntity.builder()
				.projectId(projectId.id)
				.communityAllocationId(communityAllocationId2.id)
				.name("anem2")
				.amount(new BigDecimal(30))
				.build()
		);

		BigDecimal sum = entityReadRepository.calculateAvailableAmount(communityAllocationId.id).getAmount();
		assertThat(sum).isEqualTo(new BigDecimal(5));
	}

	@Test
	void shouldReturnAllocationWithRelatedObjects() {
		ProjectAllocationEntity save = entityRepository.save(
			ProjectAllocationEntity.builder()
				.projectId(projectId.id)
				.communityAllocationId(communityAllocationId.id)
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
				.projectId(projectId.id)
				.communityAllocationId(communityAllocationId.id)
				.name("anem")
				.amount(new BigDecimal(10))
				.build()
		);

		Set<ProjectAllocationReadEntity> entities = entityReadRepository.findAllByProjectId(projectId.id);
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
			.projectId(projectId.id)
			.communityAllocationId(communityAllocationId.id)
			.name("name")
			.amount(new BigDecimal(10))
			.build();

		//when
		ProjectAllocationEntity saved = entityRepository.save(entityToSave);

		//then
		assertThat(entityRepository.findAll()).hasSize(1);
		Optional<ProjectAllocationEntity> byId = entityRepository.findById(saved.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().projectId).isEqualTo(projectId.id);
		assertThat(byId.get().communityAllocationId).isEqualTo(communityAllocationId.id);
		assertThat(byId.get().name).isEqualTo("name");
		assertThat(byId.get().amount).isEqualTo(new BigDecimal(10));
	}

	@Test
	void shouldUpdateProjectAllocation() {
		//given
		ProjectAllocationEntity old = ProjectAllocationEntity.builder()
			.projectId(projectId.id)
			.communityAllocationId(communityAllocationId.id)
			.name("anem")
			.amount(new BigDecimal(10))
			.build();
		entityRepository.save(old);
		ProjectAllocationEntity toUpdate = ProjectAllocationEntity.builder()
			.projectId(projectId.id)
			.communityAllocationId(communityAllocationId.id)
			.name("anem2")
			.amount(new BigDecimal(102))
			.build();

		//when
		entityRepository.save(toUpdate);

		//then
		Optional<ProjectAllocationEntity> byId = entityRepository.findById(toUpdate.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().projectId).isEqualTo(projectId.id);
		assertThat(byId.get().communityAllocationId).isEqualTo(communityAllocationId.id);
		assertThat(byId.get().name).isEqualTo("anem2");
		assertThat(byId.get().amount).isEqualTo(new BigDecimal(102));
	}

	@Test
	void shouldFindCreatedProjectAllocations() {
		//given
		ProjectAllocationEntity toFind = ProjectAllocationEntity.builder()
			.projectId(projectId.id)
			.communityAllocationId(communityAllocationId.id)
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
			.projectId(projectId.id)
			.communityAllocationId(communityAllocationId.id)
			.name("anem")
			.amount(new BigDecimal(10))
			.build()
		);
		entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId2.id)
			.communityAllocationId(communityAllocationId2.id)
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
			.projectId(projectId.id)
			.communityAllocationId(communityAllocationId.id)
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
			.projectId(projectId.id)
			.communityAllocationId(communityAllocationId.id)
			.name("anem")
			.amount(new BigDecimal(10))
			.build());

		//when
		boolean exists = entityReadRepository.existsByCommunityIdAndName(communityId.id, service.name);

		//then
		assertThat(exists).isTrue();
	}

	@Test
	void savedProjectAllocationDoesNotExistByName() {
		//given
		entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId.id)
			.communityAllocationId(communityAllocationId.id)
			.name("anem")
			.amount(new BigDecimal(10))
			.build());

		//when
		boolean nonExists = entityReadRepository.existsByCommunityIdAndName(communityId.id, "wrong_name");

		//then
		assertThat(nonExists).isFalse();
	}

	@Test
	void shouldDeleteService() {
		//given
		ProjectAllocationEntity entityToRemove = entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId.id)
			.communityAllocationId(communityAllocationId.id)
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
			.projectId(projectId.id)
			.communityAllocationId(communityAllocationId.id)
			.name("anem")
			.amount(new BigDecimal(10))
			.build());
		entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId2.id)
			.communityAllocationId(communityAllocationId2.id)
			.name("anem2")
			.amount(new BigDecimal(10))
			.build());

		//when
		entityRepository.deleteAll();

		//then
		assertThat(entityRepository.findAll()).hasSize(0);
	}

}