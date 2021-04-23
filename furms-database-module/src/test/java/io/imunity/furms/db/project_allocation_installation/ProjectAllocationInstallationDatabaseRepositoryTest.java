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
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
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
import java.util.UUID;

import static io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus.INSTALLED;
import static io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus.SEND;
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
	private ProjectAllocationInstallationRepository entityRepository;

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
		entityRepository.deleteAll();
	}

	@Test
	void shouldCreateProjectAllocation() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectAllocationInstallation request = ProjectAllocationInstallation.builder()
				.correlationId(correlationId.id)
				.siteId(siteId.toString())
				.projectAllocationId(projectAllocationId.toString())
				.status(SEND)
				.build();

		//when
		String id = entityDatabaseRepository.create(request);

		//then
		ProjectAllocationInstallation allocationInstallation = entityRepository.findAll(projectId.toString()).iterator().next();
		assertThat(allocationInstallation.id).isEqualTo(id);
		assertThat(allocationInstallation.correlationId).isEqualTo(correlationId.id);
		assertThat(allocationInstallation.status).isEqualTo(SEND);
	}

	@Test
	void shouldUpdateProjectAllocation() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectAllocationInstallation request = ProjectAllocationInstallation.builder()
				.id("id")
				.correlationId(correlationId.id)
				.siteId(siteId.toString())
				.projectAllocationId(projectAllocationId.toString())
				.status(SEND)
				.build();

		//when
		String id = entityDatabaseRepository.create(request);
		entityDatabaseRepository.update(id, INSTALLED);

		//then
		ProjectAllocationInstallation allocationInstallation = entityRepository.findAll(projectId.toString()).iterator().next();
		assertThat(allocationInstallation.id.toString()).isEqualTo(id);
		assertThat(allocationInstallation.correlationId.toString()).isEqualTo(correlationId.id);
		assertThat(allocationInstallation.status).isEqualTo(INSTALLED);
	}

	@Test
	void shouldRemoveProjectAllocationInstallation(){
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectAllocationInstallation request = ProjectAllocationInstallation.builder()
				.correlationId(correlationId.id)
				.siteId(siteId.toString())
				.projectAllocationId(projectAllocationId.toString())
				.status(SEND)
				.build();

		//when
		String id = entityDatabaseRepository.create(request);
		entityDatabaseRepository.delete(id);

		//then
		assertThat(entityRepository.findAll(projectId.toString())).isEmpty();
	}

}