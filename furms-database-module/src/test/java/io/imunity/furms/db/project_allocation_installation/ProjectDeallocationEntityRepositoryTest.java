/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_allocation_installation;


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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationStatus.ACKNOWLEDGED;
import static io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProjectDeallocationEntityRepositoryTest extends DBIntegrationTest {

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
	private ProjectDeallocationEntityRepository entityRepository;

	private UUID siteId;
	private UUID siteId2;

	private UUID projectAllocationId;
	private UUID projectAllocationId2;

	@BeforeEach
	void init() throws IOException {
		Site site = Site.builder()
			.name("name")
			.build();
		Site site1 = Site.builder()
			.name("name2")
			.build();
		siteId = UUID.fromString(siteRepository.create(site, new SiteExternalId("id")));
		siteId2 = UUID.fromString(siteRepository.create(site1, new SiteExternalId("id2")));

		Community community = Community.builder()
			.name("name")
			.logo(FurmsImage.empty())
			.build();
		Community community2 = Community.builder()
			.name("name1")
			.logo(FurmsImage.empty())
			.build();
		UUID communityId = UUID.fromString(communityRepository.create(community));
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

		UUID projectId = UUID.fromString(projectRepository.create(project));
		UUID projectId2 = UUID.fromString(projectRepository.create(project2));

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
			.unit(ResourceMeasureUnit.SiUnit.kilo)
			.build();
		UUID resourceTypeId = UUID.fromString(resourceTypeRepository.create(resourceType));

		UUID resourceCreditId = UUID.fromString(resourceCreditRepository.create(ResourceCredit.builder()
			.siteId(siteId.toString())
			.resourceTypeId(resourceTypeId.toString())
			.name("name")
			.split(true)
			.access(true)
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

	@AfterEach
	void clean(){
		entityRepository.deleteAll();
	}

	@Test
	void shouldCreateProjectDeallocation() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectDeallocationEntity entityToSave = ProjectDeallocationEntity.builder()
				.correlationId(correlationId)
				.siteId(siteId)
				.projectAllocationId(projectAllocationId)
				.status(PENDING)
				.build();

		//when
		ProjectDeallocationEntity saved = entityRepository.save(entityToSave);

		//then
		assertThat(entityRepository.findAll()).hasSize(1);
		Optional<ProjectDeallocationEntity> byId = entityRepository.findById(saved.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(saved.getId());
		assertThat(byId.get().status).isEqualTo(PENDING.getValue());
		assertThat(byId.get().correlationId).isEqualTo(correlationId);
	}

	@Test
	void shouldUpdateProjectDeallocation() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectDeallocationEntity entityToSave = ProjectDeallocationEntity.builder()
				.correlationId(correlationId)
				.siteId(siteId)
				.projectAllocationId(projectAllocationId)
				.status(PENDING)
				.build();

		//when
		ProjectDeallocationEntity save = entityRepository.save(entityToSave);

		ProjectDeallocationEntity entityToUpdate = ProjectDeallocationEntity.builder()
			.id(save.getId())
			.correlationId(save.correlationId)
			.siteId(save.siteId)
			.projectAllocationId(save.projectAllocationId)
			.status(ACKNOWLEDGED)
			.build();

		entityRepository.save(entityToUpdate);

		//then
		Optional<ProjectDeallocationEntity> byId = entityRepository.findById(entityToSave.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(save.getId());
		assertThat(byId.get().status).isEqualTo(ACKNOWLEDGED.getValue());
		assertThat(byId.get().correlationId).isEqualTo(correlationId);
	}

	@Test
	void shouldFindCreatedProjectDeallocation() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectDeallocationEntity toSave = ProjectDeallocationEntity.builder()
				.correlationId(correlationId)
				.siteId(siteId)
				.projectAllocationId(projectAllocationId)
				.status(PENDING)
				.build();

		entityRepository.save(toSave);

		//when
		Optional<ProjectDeallocationEntity> byId = entityRepository.findById(toSave.getId());

		//then
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindCreatedProjectDeallocationByCorrelationId() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectDeallocationEntity toFind = ProjectDeallocationEntity.builder()
				.correlationId(correlationId)
				.siteId(siteId)
				.projectAllocationId(projectAllocationId)
				.status(PENDING)
				.build();

		entityRepository.save(toFind);
		ProjectDeallocationEntity findById = entityRepository.findByCorrelationId(correlationId).get();

		//when
		Optional<ProjectDeallocationEntity> byId = entityRepository.findById(findById.getId());

		//then
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindAllAvailableProjectDeallocation() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectDeallocationEntity toSave = ProjectDeallocationEntity.builder()
				.correlationId(correlationId)
				.siteId(siteId)
				.projectAllocationId(projectAllocationId)
				.status(PENDING)
				.build();
		UUID correlationId1 = UUID.randomUUID();
		ProjectDeallocationEntity toSave1 = ProjectDeallocationEntity.builder()
			.correlationId(correlationId1)
			.siteId(siteId2)
			.projectAllocationId(projectAllocationId2)
			.status(ACKNOWLEDGED)
			.build();

		entityRepository.save(toSave);
		entityRepository.save(toSave1);

		//when
		Iterable<ProjectDeallocationEntity> all = entityRepository.findAll();

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void shouldDeleteProjectDeallocation() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectDeallocationEntity toSave = ProjectDeallocationEntity.builder()
				.correlationId(correlationId)
				.siteId(siteId2)
				.projectAllocationId(projectAllocationId2)
				.status(PENDING)
				.build();

		//when
		entityRepository.save(toSave);
		entityRepository.deleteById(toSave.getId());

		//then
		assertThat(entityRepository.findById(toSave.getId())).isEmpty();
	}

	@Test
	void shouldDeleteAllProjectDeallocations() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectDeallocationEntity toSave = ProjectDeallocationEntity.builder()
				.correlationId(correlationId)
				.siteId(siteId)
				.projectAllocationId(projectAllocationId)
				.status(PENDING)
				.build();
		UUID correlationId1 = UUID.randomUUID();
		ProjectDeallocationEntity toSave1 = ProjectDeallocationEntity.builder()
			.correlationId(correlationId1)
			.siteId(siteId2)
			.projectAllocationId(projectAllocationId2)
			.status(ACKNOWLEDGED)
			.build();

		//when
		entityRepository.save(toSave);
		entityRepository.save(toSave1);
		entityRepository.deleteAll();

		//then
		assertThat(entityRepository.findAll()).hasSize(0);
	}

}