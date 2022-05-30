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
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import io.imunity.furms.spi.services.InfraServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.db.id.uuid.UUIDIdUtils.generateId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
class ProjectAllocationDatabaseRepositoryTest extends DBIntegrationTest {

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
	private ProjectAllocationDatabaseRepository entityDatabaseRepository;

	@MockBean
	private ResourceUsageRepository resourceUsageRepository;

	private SiteId siteId;
	private SiteId siteId2;

	private CommunityId communityId;

	private ProjectId projectId;
	private ProjectId projectId2;

	private CommunityAllocationId communityAllocationId;
	private CommunityAllocationId communityAllocationId2;
	private CommunityAllocationId communityAllocationId3;

	@BeforeEach
	void init() {
		Site site = Site.builder()
			.name("name")
			.connectionInfo("alala")
			.build();
		siteId = siteRepository.create(site, new SiteExternalId("id"));
		Site site2 = Site.builder()
				.name("name2")
				.connectionInfo("alala")
				.build();
		siteId2 = siteRepository.create(site2, new SiteExternalId("id2"));

		Community community = Community.builder()
			.name("name")
			.logo(FurmsImage.empty())
			.build();
		communityId = communityRepository.create(community);

		Community community2 = Community.builder()
			.name("name2")
			.logo(FurmsImage.empty())
			.build();
		CommunityId communityId2 = communityRepository.create(community2);

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
		Project project2 = Project.builder()
			.communityId(communityId2)
			.name("name2")
			.logo(FurmsImage.empty())
			.description("new_description")
			.acronym("acronym")
			.researchField("research filed")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
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
			.unit(ResourceMeasureUnit.TERA)
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
		ResourceCreditId resourceCreditId2 = resourceCreditRepository.create(ResourceCredit.builder()
			.siteId(siteId2)
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
				.communityId(communityId2)
				.resourceCreditId(resourceCreditId)
				.name("anem2")
				.amount(new BigDecimal(30))
				.build()
		);
		communityAllocationId3 = communityAllocationRepository.create(
			CommunityAllocation.builder()
				.communityId(communityId2)
				.resourceCreditId(resourceCreditId2)
				.name("anem3")
				.amount(new BigDecimal(30))
				.build()
		);
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

		Optional<ProjectAllocationResolved> entity =
			entityDatabaseRepository.findByIdWithRelatedObjects(new ProjectAllocationId(save.getId()));
		assertThat(entity).isPresent();
		assertThat(entity.get().name).isEqualTo("anem");
		assertThat(entity.get().amount).isEqualTo(new BigDecimal(10));
		assertThat(entity.get().site.getName()).isEqualTo("name");
		assertThat(entity.get().resourceType.type).isEqualTo(ResourceMeasureType.FLOATING_POINT);
		assertThat(entity.get().resourceType.unit).isEqualTo(ResourceMeasureUnit.TERA);
		assertThat(entity.get().resourceCredit.name).isEqualTo("name");
		assertThat(entity.get().resourceCredit.splittable).isEqualTo(true);
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

		Set<ProjectAllocationResolved> entities = entityDatabaseRepository.findAllWithRelatedObjects(projectId);
		assertThat(entities.size()).isEqualTo(1);
		ProjectAllocationResolved entity = entities.iterator().next();
		assertThat(entity.name).isEqualTo("anem");
		assertThat(entity.amount).isEqualTo(new BigDecimal(10));
		assertThat(entity.site.getName()).isEqualTo("name");
		assertThat(entity.resourceType.type).isEqualTo(ResourceMeasureType.FLOATING_POINT);
		assertThat(entity.resourceType.unit).isEqualTo(ResourceMeasureUnit.TERA);
		assertThat(entity.resourceCredit.name).isEqualTo("name");
		assertThat(entity.resourceCredit.splittable).isEqualTo(true);
		assertThat(entity.resourceCredit.amount).isEqualTo(new BigDecimal(100));
	}

	@Test
	void shouldReturnAllocationsWithRelatedObjectsBySiteId() {
		entityRepository.save(
				ProjectAllocationEntity.builder()
						.projectId(projectId.id)
						.communityAllocationId(communityAllocationId.id)
						.name("test1")
						.amount(new BigDecimal(10))
						.build());
		entityRepository.save(
				ProjectAllocationEntity.builder()
						.projectId(projectId.id)
						.communityAllocationId(communityAllocationId3.id)
						.name("test2")
						.amount(new BigDecimal(10))
						.build());
		entityRepository.save(
				ProjectAllocationEntity.builder()
						.projectId(projectId.id)
						.communityAllocationId(communityAllocationId3.id)
						.name("test3")
						.amount(new BigDecimal(10))
						.build());

		Set<ProjectAllocationResolved> entities = entityDatabaseRepository.findAllWithRelatedObjectsBySiteId(siteId2);
		assertThat(entities.size()).isEqualTo(2);
	}

	@Test
	void shouldFindCreatedService() {
		//given
		ProjectAllocationEntity entity = entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId.id)
			.communityAllocationId(communityAllocationId.id)
			.name("name")
			.amount(new BigDecimal(10))
			.build()
		);

		//when
		Optional<ProjectAllocation> byId = entityDatabaseRepository.findById(new ProjectAllocationId(entity.getId()));

		//then
		assertThat(byId).isPresent();
		ProjectAllocation allocation = byId.get();
		assertThat(allocation.id.id).isEqualTo(entity.getId());
		assertThat(allocation.projectId.id).isEqualTo(entity.projectId);
		assertThat(allocation.communityAllocationId.id).isEqualTo(entity.communityAllocationId);
		assertThat(allocation.name).isEqualTo(entity.name);
		assertThat(byId.get().amount).isEqualTo(new BigDecimal(10));
	}

	@Test
	void shouldNotFindByIdIfDoesntExist() {
		//given
		UUID wrongId = generateId();
		entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId.id)
			.communityAllocationId(communityAllocationId.id)
			.name("name")
			.amount(new BigDecimal(10))
			.build()
		);

		//when
		Optional<ProjectAllocation> byId = entityDatabaseRepository.findById(new ProjectAllocationId(wrongId));

		//then
		assertThat(byId).isEmpty();
	}

	@Test
	void shouldFindAllProjectAllocations() {
		//given
		entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId.id)
			.communityAllocationId(communityAllocationId.id)
			.name("name")
			.amount(new BigDecimal(10))
			.build()
		);
		entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId.id)
			.communityAllocationId(communityAllocationId2.id)
			.name("name2")
			.amount(new BigDecimal(10))
			.build()
		);

		//when
		Set<ProjectAllocation> all = entityDatabaseRepository.findAll(projectId);

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void shouldCreateProjectAllocation() {
		//given
		ProjectAllocation request = ProjectAllocation.builder()
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("name")
			.amount(new BigDecimal(10))
			.build();

		//when
		ProjectAllocationId newProjectAllocationId = entityDatabaseRepository.create(request);

		//then
		Optional<ProjectAllocation> byId = entityDatabaseRepository.findById(newProjectAllocationId);
		assertThat(byId).isPresent();
		assertThat(byId.get().id).isEqualTo(newProjectAllocationId);
		assertThat(byId.get().projectId).isEqualTo(projectId);
		assertThat(byId.get().communityAllocationId).isEqualTo(communityAllocationId);
		assertThat(byId.get().name).isEqualTo("name");
		assertThat(byId.get().amount).isEqualTo(new BigDecimal(10));
	}

	@Test
	void shouldUpdateProjectAllocation() {
		//given
		ProjectAllocationEntity old = entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId.id)
			.communityAllocationId(communityAllocationId.id)
			.name("name")
			.amount(new BigDecimal(10))
			.build()
		);
		ProjectAllocation requestToUpdate = ProjectAllocation.builder()
			.id(old.getId().toString())
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("new_name")
			.amount(new BigDecimal(101))
			.build();

		//when
		entityDatabaseRepository.update(requestToUpdate);

		//then
		Optional<ProjectAllocation> byId = entityDatabaseRepository.findById(new ProjectAllocationId(old.getId()));
		assertThat(byId).isPresent();
		assertThat(byId.get().name).isEqualTo("new_name");
		assertThat(byId.get().projectId).isEqualTo(projectId);
		assertThat(byId.get().communityAllocationId).isEqualTo(communityAllocationId);
		assertThat(byId.get().amount).isEqualTo(new BigDecimal(101));
	}

	@Test
	void savedProjectAllocationExists() {
		//given
		ProjectAllocationEntity entity = entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId.id)
			.communityAllocationId(communityAllocationId.id)
			.name("name")
			.amount(new BigDecimal(10))
			.build()
		);

		//when + then
		assertThat(entityDatabaseRepository.exists(new ProjectAllocationId(entity.getId()))).isTrue();
	}

	@Test
	void shouldNotExistsDueToEmptyOrWrongId() {
		//given
		ProjectAllocationId nonExistedId = new ProjectAllocationId(generateId());

		//when + then
		assertThat(entityDatabaseRepository.exists(nonExistedId)).isFalse();
		assertThat(entityDatabaseRepository.exists(null)).isFalse();
		assertThat(entityDatabaseRepository.exists(new ProjectAllocationId((UUID) null))).isFalse();
	}

	@Test
	void shouldReturnTrueForUniqueName() {
		//given
		entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId.id)
			.communityAllocationId(communityAllocationId.id)
			.name("name")
			.amount(new BigDecimal(10))
			.build()
		);
		String uniqueName = "unique_name";

		//when + then
		assertThat(entityDatabaseRepository.isNamePresent(communityId, uniqueName)).isTrue();
	}

	@Test
	void shouldReturnTrueForUniqueNameInCommunityScope() {
		//given
		entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId.id)
			.communityAllocationId(communityAllocationId.id)
			.name("name")
			.amount(new BigDecimal(10))
			.build()
		);
		entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId2.id)
			.communityAllocationId(communityAllocationId2.id)
			.name("unique_name")
			.amount(new BigDecimal(10))
			.build()
		);
		String uniqueName = "unique_name";

		//when + then
		assertThat(entityDatabaseRepository.isNamePresent(communityId, uniqueName)).isTrue();
	}

	@Test
	void shouldReturnFalseForNonUniqueNameInCommunityScope() {
		//given
		ProjectAllocationEntity existedProjectAllocation = entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId.id)
			.communityAllocationId(communityAllocationId.id)
			.name("name")
			.amount(new BigDecimal(10))
			.build());

		entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId2.id)
			.communityAllocationId(communityAllocationId.id)
			.name("name")
			.amount(new BigDecimal(10))
			.build()
		);

		//when + then
		assertThat(entityDatabaseRepository.isNamePresent(communityId, existedProjectAllocation.name)).isFalse();
	}

	@Test
	void shouldReturnFalseForNonUniqueName() {
		//given
		ProjectAllocationEntity existedProjectAllocation = entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId.id)
			.communityAllocationId(communityAllocationId.id)
			.name("name")
			.amount(new BigDecimal(10))
			.build());

		//when + then
		assertThat(entityDatabaseRepository.isNamePresent(communityId, existedProjectAllocation.name)).isFalse();
	}

	@Test
	void shouldReturnTrueForExistingCommunityAllocationId() {
		//given
		ProjectAllocationEntity existedResourceCredit = entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId.id)
			.communityAllocationId(communityAllocationId.id)
			.name("name")
			.amount(new BigDecimal(10))
			.build());

		//when + then
		assertThat(entityRepository.existsByCommunityAllocationId(existedResourceCredit.communityAllocationId)).isTrue();
	}

	@Test
	void shouldReturnFalseForNonExistingCommunityAllocationId() {
		//when + then
		assertThat(entityRepository.existsByCommunityAllocationId(UUID.randomUUID())).isFalse();
	}

	@Test
	void shouldNotThrowDuplicateKeyExceptionForManyResourceUsagesForTheSameProjectId() {
		final ProjectAllocationEntity allocation1 = entityRepository.save(ProjectAllocationEntity.builder()
				.projectId(projectId.id)
				.communityAllocationId(communityAllocationId.id)
				.name("name")
				.amount(new BigDecimal(10))
				.build());
		final ProjectAllocationEntity allocation2 = entityRepository.save(ProjectAllocationEntity.builder()
				.projectId(projectId.id)
				.communityAllocationId(communityAllocationId.id)
				.name("name2")
				.amount(new BigDecimal(10))
				.build());
		when(resourceUsageRepository.findCurrentResourceUsages(projectId)).thenReturn(Set.of(
				ResourceUsage.builder()
						.projectId(projectId)
						.projectAllocationId(allocation1.getId().toString())
						.cumulativeConsumption(BigDecimal.ONE)
						.probedAt(LocalDateTime.now().minusMinutes(1))
						.build(),
				ResourceUsage.builder()
						.projectId(projectId)
						.projectAllocationId(allocation2.getId().toString())
						.cumulativeConsumption(BigDecimal.ONE)
						.probedAt(LocalDateTime.now().minusMinutes(1))
						.build()
		));
		entityDatabaseRepository.findAllWithRelatedObjectsBySiteId(siteId);
	}

}