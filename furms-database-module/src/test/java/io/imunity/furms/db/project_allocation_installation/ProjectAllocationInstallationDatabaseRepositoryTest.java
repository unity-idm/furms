/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_allocation_installation;


import static io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus.INSTALLED;
import static io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus.PROVISIONING_PROJECT;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationStatus;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_types.ResourceMeasureType;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus.ACKNOWLEDGED;
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
	private ProjectAllocationInstallationDatabaseRepository entityDatabaseRepository;

	private UUID siteId;

	private UUID projectId;

	private UUID projectAllocationId;

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

		UUID communityId = UUID.fromString(communityRepository.create(community));

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

		projectId = UUID.fromString(projectRepository.create(project));

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

		projectAllocationId = UUID.fromString(projectAllocationRepository.create(
			ProjectAllocation.builder()
				.projectId(projectId.toString())
				.communityAllocationId(communityAllocationId.toString())
				.name("anem")
				.amount(new BigDecimal(5))
				.build()
		));
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
				.siteId(siteId.toString())
				.projectAllocationId(projectAllocationId.toString())
				.status(PROVISIONING_PROJECT)
				.build();

		//when
		String id = entityDatabaseRepository.create(request);

		//then
		ProjectAllocationInstallation allocationInstallation = allocationRepository.findAll(projectId.toString()).iterator().next();
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
			.siteId(siteId.toString())
			.projectAllocationId(projectAllocationId.toString())
			.status(ProjectDeallocationStatus.PENDING)
			.build();

		//when
		String id = entityDatabaseRepository.create(request);

		//then
		ProjectDeallocationEntity deallocation = deallocationRepository.findAll().iterator().next();
		assertThat(deallocation.getId().toString()).isEqualTo(id);
		assertThat(deallocation.correlationId.toString()).isEqualTo(correlationId.id);
		assertThat(deallocation.status).isEqualTo(ProjectDeallocationStatus.PENDING.getPersistentId());
	}

	@Test
	void shouldUpdateProjectAllocation() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectAllocationInstallation request = ProjectAllocationInstallation.builder()
				.id("id")
				.correlationId(new CorrelationId(correlationId.id))
				.siteId(siteId.toString())
				.projectAllocationId(projectAllocationId.toString())
				.status(PROVISIONING_PROJECT)
				.build();

		//when
		String id = entityDatabaseRepository.create(request);
		entityDatabaseRepository.update(correlationId.id, ACKNOWLEDGED, Optional.empty());

		//then
		ProjectAllocationInstallation allocationInstallation = allocationRepository.findAll(projectId.toString()).iterator().next();
		assertThat(allocationInstallation.id).isEqualTo(id);
		assertThat(allocationInstallation.correlationId.id).isEqualTo(correlationId.id);
		assertThat(allocationInstallation.status).isEqualTo(ACKNOWLEDGED);
	}

	@Test
	void shouldUpdateProjectDeallocation() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectDeallocation request = ProjectDeallocation.builder()
			.id("id")
			.correlationId(new CorrelationId(correlationId.id))
			.siteId(siteId.toString())
			.projectAllocationId(projectAllocationId.toString())
			.status(ProjectDeallocationStatus.PENDING)
			.build();

		//when
		String id = entityDatabaseRepository.create(request);
		entityDatabaseRepository.update(correlationId.id, ProjectDeallocationStatus.ACKNOWLEDGED, Optional.empty());

		//then
		ProjectDeallocationEntity projectDeallocationEntity = deallocationRepository.findAll().iterator().next();
		assertThat(projectDeallocationEntity.getId().toString()).isEqualTo(id);
		assertThat(projectDeallocationEntity.correlationId.toString()).isEqualTo(correlationId.id);
		assertThat(projectDeallocationEntity.status).isEqualTo(ProjectDeallocationStatus.ACKNOWLEDGED.getPersistentId());
	}

	@Test
	void shouldRemoveProjectAllocationInstallation(){
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectAllocationInstallation request = ProjectAllocationInstallation.builder()
				.correlationId(new CorrelationId(correlationId.id))
				.siteId(siteId.toString())
				.projectAllocationId(projectAllocationId.toString())
				.status(PROVISIONING_PROJECT)
				.build();

		//when
		String id = entityDatabaseRepository.create(request);
		entityDatabaseRepository.deleteBy(id);

		//then
		assertThat(allocationRepository.findAll(projectId.toString())).isEmpty();
	}

}