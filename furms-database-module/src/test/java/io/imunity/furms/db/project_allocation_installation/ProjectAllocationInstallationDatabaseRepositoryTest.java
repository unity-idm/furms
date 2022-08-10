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
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationId;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationId;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationStatus;
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
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.project_allocation_installation.ProjectAllocationInstallationRepository;
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
import java.util.UUID;

import static io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus.INSTALLATION_ACKNOWLEDGED;
import static io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus.PROVISIONING_PROJECT;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProjectAllocationInstallationDatabaseRepositoryTest extends DBIntegrationTest {
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
	private ProjectAllocationInstallationRepository allocationRepository;
	@Autowired
	private ProjectDeallocationEntityRepository deallocationRepository;
	@Autowired
	private ProjectAllocationChunkEntityRepository chunkRepository;

	@Autowired
	private ProjectAllocationInstallationDatabaseRepository entityDatabaseRepository;

	private SiteId siteId;

	private ProjectId projectId;

	private ProjectAllocationId projectAllocationId;

	@BeforeEach
	void init() {
		Site site = Site.builder()
			.name("name")
			.build();

		siteId = siteRepository.create(site, new SiteExternalId("id"));

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
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();

		projectId = projectRepository.create(project);

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

		projectAllocationId = projectAllocationRepository.create(
			ProjectAllocation.builder()
				.projectId(projectId)
				.communityAllocationId(communityAllocationId)
				.name("anem")
				.amount(new BigDecimal(5))
				.build()
		);
	}

	@AfterEach
	void clean(){
		allocationRepository.deleteAll();
	}

	@Test
	void shouldCreateProjectAllocationInstallation() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectAllocationInstallation request = ProjectAllocationInstallation.builder()
				.correlationId(new CorrelationId(correlationId.id))
				.siteId(siteId)
				.projectAllocationId(projectAllocationId)
				.status(PROVISIONING_PROJECT)
				.build();

		//when
		ProjectAllocationInstallationId id = entityDatabaseRepository.create(request);

		//then
		ProjectAllocationInstallation allocationInstallation = allocationRepository.findAll(projectId).iterator().next();
		assertThat(allocationInstallation.id).isEqualTo(id);
		assertThat(allocationInstallation.correlationId.id).isEqualTo(correlationId.id);
		assertThat(allocationInstallation.status).isEqualTo(PROVISIONING_PROJECT);
	}

	@Test
	void shouldCreateProjectDeallocation() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectDeallocation request = ProjectDeallocation.builder()
			.correlationId(new CorrelationId(correlationId.id))
			.siteId(siteId)
			.projectAllocationId(projectAllocationId)
			.status(ProjectDeallocationStatus.PENDING)
			.build();

		//when
		ProjectDeallocationId id = entityDatabaseRepository.create(request);

		//then
		ProjectDeallocationEntity deallocation = deallocationRepository.findAll().iterator().next();
		assertThat(deallocation.getId()).isEqualTo(id.id);
		assertThat(deallocation.correlationId.toString()).isEqualTo(correlationId.id);
		assertThat(deallocation.status).isEqualTo(ProjectDeallocationStatus.PENDING.getPersistentId());
	}

	@Test
	void shouldUpdateProjectAllocation() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectAllocationInstallation request = ProjectAllocationInstallation.builder()
				.id(UUID.randomUUID().toString())
				.correlationId(new CorrelationId(correlationId.id))
				.siteId(siteId)
				.projectAllocationId(projectAllocationId)
				.status(PROVISIONING_PROJECT)
				.build();

		//when
		ProjectAllocationInstallationId id = entityDatabaseRepository.create(request);
		entityDatabaseRepository.update(correlationId, INSTALLATION_ACKNOWLEDGED, Optional.empty());

		//then
		ProjectAllocationInstallation allocationInstallation = allocationRepository.findAll(projectId).iterator().next();
		assertThat(allocationInstallation.id).isEqualTo(id);
		assertThat(allocationInstallation.correlationId.id).isEqualTo(correlationId.id);
		assertThat(allocationInstallation.status).isEqualTo(INSTALLATION_ACKNOWLEDGED);
	}

	@Test
	void shouldUpdateProjectDeallocation() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectDeallocation request = ProjectDeallocation.builder()
			.id(UUID.randomUUID().toString())
			.correlationId(new CorrelationId(correlationId.id))
			.siteId(siteId)
			.projectAllocationId(projectAllocationId)
			.status(ProjectDeallocationStatus.PENDING)
			.build();

		//when
		ProjectDeallocationId id = entityDatabaseRepository.create(request);
		entityDatabaseRepository.update(correlationId, ProjectDeallocationStatus.ACKNOWLEDGED, Optional.empty());

		//then
		ProjectDeallocationEntity projectDeallocationEntity = deallocationRepository.findAll().iterator().next();
		assertThat(projectDeallocationEntity.getId()).isEqualTo(id.id);
		assertThat(projectDeallocationEntity.correlationId.toString()).isEqualTo(correlationId.id);
		assertThat(projectDeallocationEntity.status).isEqualTo(ProjectDeallocationStatus.ACKNOWLEDGED.getPersistentId());
	}

	@Test
	void shouldRemoveProjectAllocationInstallation(){
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectAllocationInstallation request = ProjectAllocationInstallation.builder()
				.correlationId(new CorrelationId(correlationId.id))
				.siteId(siteId)
				.projectAllocationId(projectAllocationId)
				.status(PROVISIONING_PROJECT)
				.build();

		//when
		ProjectAllocationInstallationId id = entityDatabaseRepository.create(request);
		entityDatabaseRepository.deleteBy(id);

		//then
		assertThat(allocationRepository.findAll(projectId)).isEmpty();
	}

	@Test
	void shouldUpdateChunk(){
		//given
		ProjectAllocationChunkEntity request = ProjectAllocationChunkEntity.builder()
			.projectAllocationId(projectAllocationId.id)
			.chunkId("id")
			.amount(BigDecimal.ONE)
			.validTo(LocalDateTime.now().minusDays(3))
			.validFrom(LocalDateTime.now().plusDays(3))
			.build();
		ProjectAllocationChunkEntity savedEntity = chunkRepository.save(request);

		//when
		ProjectAllocationChunk chunk = ProjectAllocationChunk.builder()
			.projectAllocationId(projectAllocationId)
			.chunkId("id")
			.amount(BigDecimal.TEN)
			.validTo(LocalDateTime.now().minusDays(2))
			.validFrom(LocalDateTime.now().plusDays(2))
			.build();
		entityDatabaseRepository.update(chunk);

		//then
		Optional<ProjectAllocationChunkEntity> updatedEntity = chunkRepository.findById(savedEntity.getId());
		assertThat(updatedEntity).isPresent();
		assertThat(updatedEntity.get().amount).isEqualTo(chunk.amount);
		assertThat(updatedEntity.get().validFrom).isEqualToIgnoringNanos(chunk.validFrom);
		assertThat(updatedEntity.get().validTo).isEqualToIgnoringNanos(chunk.validTo);
	}

}