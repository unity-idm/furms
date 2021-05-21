/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.sites;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.services.InfraServiceRepository;

@SpringBootTest
class SiteEntityCustomQueryRepositoryTest extends DBIntegrationTest {

	@Autowired
	private SiteEntityRepository siteRepository;

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

	private String siteId;
	private String siteId2;
	private String projectId;
	private String projectId2;

	@BeforeEach
	void init() {
		SiteEntity site = SiteEntity.builder()
			.name("name")
			.externalId("id")
			.connectionInfo("alala")
			.build();
		siteId = siteRepository.save(site).getId().toString();

		SiteEntity site2 = SiteEntity.builder()
			.name("name2")
			.externalId("id2")
			.connectionInfo("alala")
			.build();
		siteId2 = siteRepository.save(site2).getId().toString();

		SiteEntity.builder()
			.name("name3")
			.externalId("id4")
			.connectionInfo("alala")
			.build();
		siteRepository.save(site2);

		Community community = Community.builder()
			.name("name")
			.logo(FurmsImage.empty())
			.build();
		String communityId = UUID.fromString(communityRepository.create(community)).toString();

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
			.communityId(communityId)
			.name("name2")
			.description("new_description")
			.logo(FurmsImage.empty())
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

		UUID serviceId = UUID.fromString(infraServiceRepository.create(service));

		ResourceType resourceType = ResourceType.builder()
			.siteId(siteId)
			.serviceId(serviceId.toString())
			.name("name")
			.type(ResourceMeasureType.FLOATING_POINT)
			.unit(ResourceMeasureUnit.SiUnit.tera)
			.build();
		String resourceTypeId = resourceTypeRepository.create(resourceType);

		String resourceCreditId = resourceCreditRepository.create(ResourceCredit.builder()
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
			.name("name")
			.split(true)
			.access(true)
			.amount(new BigDecimal(100))
			.utcCreateTime(LocalDateTime.now())
			.utcStartTime(LocalDateTime.now().plusDays(1))
			.utcEndTime(LocalDateTime.now().plusDays(3))
			.build());

		String resourceCreditId2 = resourceCreditRepository.create(ResourceCredit.builder()
			.siteId(siteId2)
			.resourceTypeId(resourceTypeId)
			.name("name2")
			.split(true)
			.access(true)
			.amount(new BigDecimal(100))
			.utcCreateTime(LocalDateTime.now())
			.utcStartTime(LocalDateTime.now().plusDays(1))
			.utcEndTime(LocalDateTime.now().plusDays(3))
			.build());

		String communityAllocationId = communityAllocationRepository.create(
			CommunityAllocation.builder()
				.communityId(communityId)
				.resourceCreditId(resourceCreditId)
				.name("anem")
				.amount(new BigDecimal(10))
				.build()
		);

		String communityAllocationId2 = communityAllocationRepository.create(
			CommunityAllocation.builder()
				.communityId(communityId)
				.resourceCreditId(resourceCreditId2)
				.name("anem2")
				.amount(new BigDecimal(30))
				.build()
		);

		projectAllocationRepository.create(
			ProjectAllocation.builder()
				.projectId(projectId)
				.communityAllocationId(communityAllocationId)
				.name("anem")
				.amount(new BigDecimal(5))
				.build()
		);

		projectAllocationRepository.create(
			ProjectAllocation.builder()
				.projectId(projectId)
				.communityAllocationId(communityAllocationId2)
				.name("anem2")
				.amount(new BigDecimal(5))
				.build()
		);
	}

	@Test
	void shouldReturnSites() {
		Set<SiteEntity> relatedSites = siteRepository.findRelatedSites(UUID.fromString(projectId));

		assertThat(relatedSites.size()).isEqualTo(2);
		Set<String> ids = relatedSites.stream()
			.map(site -> site.getId().toString())
			.collect(Collectors.toSet());
		assertThat(ids).contains(siteId);
		assertThat(ids).contains(siteId2);
	}

	@Test
	void shouldNotReturnSites() {
		Set<SiteEntity> relatedSites = siteRepository.findRelatedSites(UUID.fromString(projectId2));

		assertThat(relatedSites.size()).isEqualTo(0);
	}

}