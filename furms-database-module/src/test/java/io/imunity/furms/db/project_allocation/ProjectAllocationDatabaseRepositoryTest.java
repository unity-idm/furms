/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_allocation;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_types.ResourceMeasureType;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
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

	private UUID siteId;
	private UUID siteId2;

	private UUID communityId;

	private UUID projectId;
	private UUID projectId2;

	private UUID resourceTypeId;

	private UUID resourceCreditId;
	private UUID resourceCreditId2;

	private UUID communityAllocationId;
	private UUID communityAllocationId2;
	private UUID communityAllocationId3;

	@BeforeEach
	void init() {
		Site site = Site.builder()
			.name("name")
			.connectionInfo("alala")
			.build();
		siteId = UUID.fromString(siteRepository.create(site, new SiteExternalId("id")));
		Site site2 = Site.builder()
				.name("name2")
				.connectionInfo("alala")
				.build();
		siteId2 = UUID.fromString(siteRepository.create(site2, new SiteExternalId("id2")));

		Community community = Community.builder()
			.name("name")
			.logo(FurmsImage.empty())
			.build();
		communityId = UUID.fromString(communityRepository.create(community));

		Community community2 = Community.builder()
			.name("name2")
			.logo(FurmsImage.empty())
			.build();
		UUID communityId2 = UUID.fromString(communityRepository.create(community2));

		Project project = Project.builder()
			.communityId(communityId.toString())
			.name("name")
			.description("new_description")
			.logo(FurmsImage.empty())
			.acronym("acronym")
			.researchField("research filed")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();
		Project project2 = Project.builder()
			.communityId(communityId2.toString())
			.name("name2")
			.logo(FurmsImage.empty())
			.description("new_description")
			.acronym("acronym")
			.researchField("research filed")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
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
			.unit(ResourceMeasureUnit.TERA)
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
		resourceCreditId2 = UUID.fromString(resourceCreditRepository.create(ResourceCredit.builder()
			.siteId(siteId2.toString())
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
				.communityId(communityId2.toString())
				.resourceCreditId(resourceCreditId.toString())
				.name("anem2")
				.amount(new BigDecimal(30))
				.build()
		));
		communityAllocationId3 = UUID.fromString(communityAllocationRepository.create(
			CommunityAllocation.builder()
				.communityId(communityId2.toString())
				.resourceCreditId(resourceCreditId2.toString())
				.name("anem3")
				.amount(new BigDecimal(30))
				.build()
		));
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

		Optional<ProjectAllocationResolved> entity = entityDatabaseRepository.findByIdWithRelatedObjects(save.getId().toString());
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
				.projectId(projectId)
				.communityAllocationId(communityAllocationId)
				.name("anem")
				.amount(new BigDecimal(10))
				.build()
		);

		Set<ProjectAllocationResolved> entities = entityDatabaseRepository.findAllWithRelatedObjects(projectId.toString());
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
						.projectId(projectId)
						.communityAllocationId(communityAllocationId)
						.name("test1")
						.amount(new BigDecimal(10))
						.build());
		entityRepository.save(
				ProjectAllocationEntity.builder()
						.projectId(projectId)
						.communityAllocationId(communityAllocationId3)
						.name("test2")
						.amount(new BigDecimal(10))
						.build());
		entityRepository.save(
				ProjectAllocationEntity.builder()
						.projectId(projectId)
						.communityAllocationId(communityAllocationId3)
						.name("test3")
						.amount(new BigDecimal(10))
						.build());

		Set<ProjectAllocationResolved> entities = entityDatabaseRepository.findAllWithRelatedObjectsBySiteId(siteId2.toString());
		assertThat(entities.size()).isEqualTo(2);
	}

	@Test
	void shouldFindCreatedService() {
		//given
		ProjectAllocationEntity entity = entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("name")
			.amount(new BigDecimal(10))
			.build()
		);

		//when
		Optional<ProjectAllocation> byId = entityDatabaseRepository.findById(entity.getId().toString());

		//then
		assertThat(byId).isPresent();
		ProjectAllocation allocation = byId.get();
		assertThat(allocation.id).isEqualTo(entity.getId().toString());
		assertThat(allocation.projectId).isEqualTo(entity.projectId.toString());
		assertThat(allocation.communityAllocationId).isEqualTo(entity.communityAllocationId.toString());
		assertThat(allocation.name).isEqualTo(entity.name);
		assertThat(byId.get().amount).isEqualTo(new BigDecimal(10));
	}

	@Test
	void shouldNotFindByIdIfDoesntExist() {
		//given
		UUID wrongId = generateId();
		entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("name")
			.amount(new BigDecimal(10))
			.build()
		);

		//when
		Optional<ProjectAllocation> byId = entityDatabaseRepository.findById(wrongId.toString());

		//then
		assertThat(byId).isEmpty();
	}

	@Test
	void shouldFindAllProjectAllocations() {
		//given
		entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("name")
			.amount(new BigDecimal(10))
			.build()
		);
		entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId)
			.communityAllocationId(communityAllocationId2)
			.name("name2")
			.amount(new BigDecimal(10))
			.build()
		);

		//when
		Set<ProjectAllocation> all = entityDatabaseRepository.findAll(projectId.toString());

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void shouldCreateProjectAllocation() {
		//given
		ProjectAllocation request = ProjectAllocation.builder()
			.projectId(projectId.toString())
			.communityAllocationId(communityAllocationId.toString())
			.name("name")
			.amount(new BigDecimal(10))
			.build();

		//when
		String newProjectAllocationId = entityDatabaseRepository.create(request);

		//then
		Optional<ProjectAllocation> byId = entityDatabaseRepository.findById(newProjectAllocationId);
		assertThat(byId).isPresent();
		assertThat(byId.get().id).isEqualTo(newProjectAllocationId);
		assertThat(byId.get().projectId).isEqualTo(projectId.toString());
		assertThat(byId.get().communityAllocationId).isEqualTo(communityAllocationId.toString());
		assertThat(byId.get().name).isEqualTo("name");
		assertThat(byId.get().amount).isEqualTo(new BigDecimal(10));
	}

	@Test
	void shouldUpdateProjectAllocation() {
		//given
		ProjectAllocationEntity old = entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("name")
			.amount(new BigDecimal(10))
			.build()
		);
		ProjectAllocation requestToUpdate = ProjectAllocation.builder()
			.id(old.getId().toString())
			.projectId(projectId.toString())
			.communityAllocationId(communityAllocationId.toString())
			.name("new_name")
			.amount(new BigDecimal(101))
			.build();

		//when
		entityDatabaseRepository.update(requestToUpdate);

		//then
		Optional<ProjectAllocation> byId = entityDatabaseRepository.findById(old.getId().toString());
		assertThat(byId).isPresent();
		assertThat(byId.get().name).isEqualTo("new_name");
		assertThat(byId.get().projectId).isEqualTo(projectId.toString());
		assertThat(byId.get().communityAllocationId).isEqualTo(communityAllocationId.toString());
		assertThat(byId.get().amount).isEqualTo(new BigDecimal(101));
	}

	@Test
	void savedProjectAllocationExists() {
		//given
		ProjectAllocationEntity entity = entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("name")
			.amount(new BigDecimal(10))
			.build()
		);

		//when + then
		assertThat(entityDatabaseRepository.exists(entity.getId().toString())).isTrue();
	}

	@Test
	void shouldNotExistsDueToEmptyOrWrongId() {
		//given
		String nonExistedId = generateId().toString();

		//when + then
		assertThat(entityDatabaseRepository.exists(nonExistedId)).isFalse();
		assertThat(entityDatabaseRepository.exists(null)).isFalse();
		assertThat(entityDatabaseRepository.exists("")).isFalse();
	}

	@Test
	void shouldReturnTrueForUniqueName() {
		//given
		entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("name")
			.amount(new BigDecimal(10))
			.build()
		);
		String uniqueName = "unique_name";

		//when + then
		assertThat(entityDatabaseRepository.isNamePresent(communityId.toString(), uniqueName)).isTrue();
	}

	@Test
	void shouldReturnTrueForUniqueNameInCommunityScope() {
		//given
		entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("name")
			.amount(new BigDecimal(10))
			.build()
		);
		entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId2)
			.communityAllocationId(communityAllocationId2)
			.name("unique_name")
			.amount(new BigDecimal(10))
			.build()
		);
		String uniqueName = "unique_name";

		//when + then
		assertThat(entityDatabaseRepository.isNamePresent(communityId.toString(), uniqueName)).isTrue();
	}

	@Test
	void shouldReturnFalseForNonUniqueNameInCommunityScope() {
		//given
		ProjectAllocationEntity existedProjectAllocation = entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("name")
			.amount(new BigDecimal(10))
			.build());

		entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId2)
			.communityAllocationId(communityAllocationId)
			.name("name")
			.amount(new BigDecimal(10))
			.build()
		);

		//when + then
		assertThat(entityDatabaseRepository.isNamePresent(communityId.toString(), existedProjectAllocation.name)).isFalse();
	}

	@Test
	void shouldReturnFalseForNonUniqueName() {
		//given
		ProjectAllocationEntity existedProjectAllocation = entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
			.name("name")
			.amount(new BigDecimal(10))
			.build());

		//when + then
		assertThat(entityDatabaseRepository.isNamePresent(communityId.toString(), existedProjectAllocation.name)).isFalse();
	}

	@Test
	void shouldReturnTrueForExistingCommunityAllocationId() {
		//given
		ProjectAllocationEntity existedResourceCredit = entityRepository.save(ProjectAllocationEntity.builder()
			.projectId(projectId)
			.communityAllocationId(communityAllocationId)
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
				.projectId(projectId)
				.communityAllocationId(communityAllocationId)
				.name("name")
				.amount(new BigDecimal(10))
				.build());
		final ProjectAllocationEntity allocation2 = entityRepository.save(ProjectAllocationEntity.builder()
				.projectId(projectId)
				.communityAllocationId(communityAllocationId)
				.name("name2")
				.amount(new BigDecimal(10))
				.build());
		when(resourceUsageRepository.findCurrentResourceUsages(projectId.toString())).thenReturn(Set.of(
				ResourceUsage.builder()
						.projectId(projectId.toString())
						.projectAllocationId(allocation1.getId().toString())
						.cumulativeConsumption(BigDecimal.ONE)
						.probedAt(LocalDateTime.now().minusMinutes(1))
						.build(),
				ResourceUsage.builder()
						.projectId(projectId.toString())
						.projectAllocationId(allocation2.getId().toString())
						.cumulativeConsumption(BigDecimal.ONE)
						.probedAt(LocalDateTime.now().minusMinutes(1))
						.build()
		));
		entityDatabaseRepository.findAllWithRelatedObjectsBySiteId(siteId.toString());
	}

}