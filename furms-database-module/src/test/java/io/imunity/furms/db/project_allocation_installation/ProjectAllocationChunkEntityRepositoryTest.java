/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_allocation_installation;


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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProjectAllocationChunkEntityRepositoryTest extends DBIntegrationTest {

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
	private ProjectAllocationChunkEntityRepository entityRepository;

	private ProjectId projectId;

	private ProjectAllocationId projectAllocationId;
	private ProjectAllocationId projectAllocationId2;

	@BeforeEach
	void init() {
		Site site = Site.builder()
			.name("name")
			.build();
		Site site1 = Site.builder()
			.name("name2")
			.build();
		SiteId siteId = siteRepository.create(site, new SiteExternalId("id"));
		siteRepository.create(site1, new SiteExternalId("id2"));

		Community community = Community.builder()
			.name("name")
			.logo(FurmsImage.empty())
			.build();
		Community community2 = Community.builder()
			.name("name1")
			.logo(FurmsImage.empty())
			.build();
		CommunityId communityId = communityRepository.create(community);
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
		ProjectId projectId2 = projectRepository.create(project2);

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

	@AfterEach
	void clean(){
		entityRepository.deleteAll();
	}

	@Test
	void shouldCreateProjectAllocationChunk() {
		//given
		ProjectAllocationChunkEntity entityToSave = ProjectAllocationChunkEntity.builder()
			.projectAllocationId(projectAllocationId.id)
			.amount(BigDecimal.TEN)
			.receivedTime(LocalDateTime.now())
			.validFrom(LocalDateTime.now())
			.validTo(LocalDateTime.now())
			.build();

		//when
		ProjectAllocationChunkEntity saved = entityRepository.save(entityToSave);

		//then
		assertThat(entityRepository.findAll()).hasSize(1);
		Optional<ProjectAllocationChunkEntity> byId = entityRepository.findById(saved.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(saved.getId());
		assertThat(byId.get().amount.compareTo(new BigDecimal(10))).isEqualTo(0);
		assertThat(byId.get().chunkId).isEqualTo(saved.chunkId);
	}

	@Test
	void shouldUpdateProjectAllocationChunk() {
		//given
		ProjectAllocationChunkEntity entityToSave = ProjectAllocationChunkEntity.builder()
			.projectAllocationId(projectAllocationId.id)
			.chunkId("1")
			.amount(BigDecimal.TEN)
			.receivedTime(LocalDateTime.now())
			.validFrom(LocalDateTime.now())
			.validTo(LocalDateTime.now())
			.build();

		//when
		ProjectAllocationChunkEntity save = entityRepository.save(entityToSave);

		ProjectAllocationChunkEntity entityToUpdate = ProjectAllocationChunkEntity.builder()
			.id(save.getId())
			.chunkId("1")
			.projectAllocationId(projectAllocationId.id)
			.amount(BigDecimal.ONE)
			.receivedTime(LocalDateTime.now())
			.validFrom(LocalDateTime.now())
			.validTo(LocalDateTime.now())
			.build();

		entityRepository.save(entityToUpdate);

		//then
		Optional<ProjectAllocationChunkEntity> byId = entityRepository.findById(entityToSave.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(save.getId());
		assertThat(byId.get().amount).isEqualTo(entityToUpdate.amount);
		assertThat(byId.get().chunkId).isEqualTo(entityToUpdate.chunkId);
	}

	@Test
	void shouldFindCreatedProjectAllocationChunk() {
		//given
		ProjectAllocationChunkEntity toSave = ProjectAllocationChunkEntity.builder()
			.projectAllocationId(projectAllocationId.id)
			.amount(BigDecimal.TEN)
			.receivedTime(LocalDateTime.now())
			.validFrom(LocalDateTime.now())
			.validTo(LocalDateTime.now())
			.build();

		entityRepository.save(toSave);

		//when
		Optional<ProjectAllocationChunkEntity> byId = entityRepository.findById(toSave.getId());

		//then
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindAllProjectAllocationChunksByProjectId() {
		//given
		ProjectAllocationChunkEntity toNotFind = ProjectAllocationChunkEntity.builder()
			.projectAllocationId(projectAllocationId2.id)
			.amount(BigDecimal.TEN)
			.receivedTime(LocalDateTime.now())
			.validFrom(LocalDateTime.now())
			.validTo(LocalDateTime.now())
			.build();

		ProjectAllocationChunkEntity toFind = ProjectAllocationChunkEntity.builder()
			.projectAllocationId(projectAllocationId.id)
			.amount(BigDecimal.TEN)
			.receivedTime(LocalDateTime.now())
			.validFrom(LocalDateTime.now())
			.validTo(LocalDateTime.now())
			.build();

		entityRepository.save(toNotFind);
		ProjectAllocationChunkEntity save = entityRepository.save(toFind);

		//when
		Set<ProjectAllocationChunkEntity> chunks = entityRepository.findAllByProjectId(projectId.id);

		//then
		assertThat(chunks.size()).isEqualTo(1);
		assertThat(chunks.iterator().next().getId()).isEqualTo(save.getId());
	}

	@Test
	void shouldFindAllAvailableProjectAllocationChunk() {
		//given
		ProjectAllocationChunkEntity toSave = ProjectAllocationChunkEntity.builder()
			.projectAllocationId(projectAllocationId.id)
			.amount(BigDecimal.TEN)
			.receivedTime(LocalDateTime.now())
			.validFrom(LocalDateTime.now())
			.validTo(LocalDateTime.now())
			.build();
		ProjectAllocationChunkEntity toSave1 = ProjectAllocationChunkEntity.builder()
			.projectAllocationId(projectAllocationId2.id)
			.amount(BigDecimal.ZERO)
			.receivedTime(LocalDateTime.now())
			.validFrom(LocalDateTime.now())
			.validTo(LocalDateTime.now())
			.build();

		entityRepository.save(toSave);
		entityRepository.save(toSave1);

		//when
		Iterable<ProjectAllocationChunkEntity> all = entityRepository.findAll();

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void shouldDeleteProjectAllocationChunk() {
		//given
		ProjectAllocationChunkEntity toSave = ProjectAllocationChunkEntity.builder()
			.projectAllocationId(projectAllocationId2.id)
			.amount(BigDecimal.ZERO)
			.receivedTime(LocalDateTime.now())
			.validFrom(LocalDateTime.now())
			.validTo(LocalDateTime.now())
			.build();

		//when
		entityRepository.save(toSave);
		entityRepository.deleteById(toSave.getId());

		//then
		assertThat(entityRepository.findById(toSave.getId())).isEmpty();
	}

	@Test
	void shouldDeleteAllProjectAllocationChunks() {
		//given
		ProjectAllocationChunkEntity toSave = ProjectAllocationChunkEntity.builder()
			.projectAllocationId(projectAllocationId.id)
			.amount(BigDecimal.TEN)
			.receivedTime(LocalDateTime.now())
			.validFrom(LocalDateTime.now())
			.validTo(LocalDateTime.now())
			.build();
		ProjectAllocationChunkEntity toSave1 = ProjectAllocationChunkEntity.builder()
			.projectAllocationId(projectAllocationId2.id)
			.amount(BigDecimal.ZERO)
			.receivedTime(LocalDateTime.now())
			.validFrom(LocalDateTime.now())
			.validTo(LocalDateTime.now())
			.build();

		//when
		entityRepository.save(toSave);
		entityRepository.save(toSave1);
		entityRepository.deleteAll();

		//then
		assertThat(entityRepository.findAll()).hasSize(0);
	}

}