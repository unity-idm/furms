/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_access;

import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.resource_access.AccessStatus;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserGrantEntityRepositoryTest extends DBIntegrationTest {

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
	private UserGrantEntityRepository userGrantEntityRepository;
	@Autowired
	private UserGrantJobEntityRepository userGrantJobEntityRepository;

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
			.unit(ResourceMeasureUnit.KILO)
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

	@Test
	void shouldFindByUserIdAndProjectAllocationId(){
		UserGrantEntity userAllocation = userGrantEntityRepository.save(
			UserGrantEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.projectAllocationId(projectAllocationId)
				.userId("userId")
				.build()
		);
		UserGrantJobEntity userGrantJobEntity = UserGrantJobEntity.builder()
			.userAllocationId(userAllocation.getId())
			.status(AccessStatus.GRANTED)
			.correlationId(UUID.randomUUID())
			.build();
		UserGrantJobEntity userAdditionSaveEntity = userGrantJobEntityRepository.save(userGrantJobEntity);

		Optional<UserGrantResolved> userAllocationResolved = userGrantEntityRepository.findByUserIdAndProjectAllocationId("userId", projectAllocationId);
		assertThat(userAllocationResolved).isPresent();
		assertThat(userAllocationResolved.get().allocation).isEqualTo(userAllocation);
		assertThat(userAllocationResolved.get().job).isEqualTo(userAdditionSaveEntity);
	}

	@Test
	void shouldFindByCorrelationId(){
		UserGrantEntity userAllocation = userGrantEntityRepository.save(
			UserGrantEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.projectAllocationId(projectAllocationId)
				.userId("userId")
				.build()
		);
		UUID correlationId = UUID.randomUUID();
		UserGrantJobEntity userGrantJobEntity = UserGrantJobEntity.builder()
			.userAllocationId(userAllocation.getId())
			.status(AccessStatus.GRANTED)
			.correlationId(correlationId)
			.build();
		userGrantJobEntityRepository.save(userGrantJobEntity);

		Optional<ProjectUserGrantEntity> userAllocationResolved = userGrantEntityRepository.findByCorrelationId(correlationId);
		assertThat(userAllocationResolved).isPresent();
		assertThat(userAllocationResolved.get().userId).isEqualTo("userId");
		assertThat(userAllocationResolved.get().projectId).isEqualTo(projectId.toString());
	}

	@Test
	void shouldFindAllByProjectId(){
		UserGrantEntity userAllocation = userGrantEntityRepository.save(
			UserGrantEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.projectAllocationId(projectAllocationId)
				.userId("userId")
				.build()
		);
		UserGrantJobEntity userGrantJobEntity = UserGrantJobEntity.builder()
			.userAllocationId(userAllocation.getId())
			.status(AccessStatus.GRANTED)
			.correlationId(UUID.randomUUID())
			.build();
		UserGrantJobEntity userAdditionSaveEntity = userGrantJobEntityRepository.save(userGrantJobEntity);

		Set<UserGrantResolved> userAllocationsResolved = userGrantEntityRepository.findAll(projectId);
		assertThat(userAllocationsResolved.size()).isEqualTo(1);
		UserGrantResolved userGrantResolved = userAllocationsResolved.iterator().next();
		assertThat(userGrantResolved.allocation).isEqualTo(userAllocation);
		assertThat(userGrantResolved.job).isEqualTo(userAdditionSaveEntity);
	}

	@Test
	void shouldFindAllByProjectIdAndUserId(){
		UserGrantEntity userAllocation = userGrantEntityRepository.save(
			UserGrantEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.projectAllocationId(projectAllocationId)
				.userId("userId")
				.build()
		);
		userGrantEntityRepository.save(
			UserGrantEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.projectAllocationId(projectAllocationId)
				.userId("userId2")
				.build()
		);
		UserGrantJobEntity userGrantJobEntity = UserGrantJobEntity.builder()
			.userAllocationId(userAllocation.getId())
			.status(AccessStatus.GRANTED)
			.correlationId(UUID.randomUUID())
			.build();
		UserGrantJobEntity userAdditionSaveEntity = userGrantJobEntityRepository.save(userGrantJobEntity);

		Set<UserGrantResolved> userAllocationsResolved = userGrantEntityRepository.findAll(projectId, "userId");
		assertThat(userAllocationsResolved.size()).isEqualTo(1);
		UserGrantResolved userGrantResolved = userAllocationsResolved.iterator().next();
		assertThat(userGrantResolved.allocation).isEqualTo(userAllocation);
		assertThat(userGrantResolved.job).isEqualTo(userAdditionSaveEntity);
	}

	@Test
	void shouldCreate(){
		UserGrantEntity userAdditionSaveEntity = userGrantEntityRepository.save(
			UserGrantEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.projectAllocationId(projectAllocationId)
				.userId("userId")
				.build()
		);

		Optional<UserGrantEntity> byId = userGrantEntityRepository.findById(userAdditionSaveEntity.getId());
		assertThat(byId).isPresent();
	}

	@Test
	void shouldDelete(){
		UserGrantEntity userAdditionSaveEntity = userGrantEntityRepository.save(
			UserGrantEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.projectAllocationId(projectAllocationId)
				.userId("userId")
				.build()
		);

		userGrantEntityRepository.deleteById(userAdditionSaveEntity.getId());
		Optional<UserGrantEntity> byId = userGrantEntityRepository.findById(userAdditionSaveEntity.getId());

		assertThat(byId).isEmpty();
	}

	@Test
	void shouldUpdate(){
		UserGrantEntity userAdditionSaveEntity = userGrantEntityRepository.save(
			UserGrantEntity.builder()
				.siteId(siteId)
				.projectId(projectId)
				.projectAllocationId(projectAllocationId)
				.userId("userId")
				.build()
		);
		userGrantEntityRepository.save(
			UserGrantEntity.builder()
				.id(userAdditionSaveEntity.getId())
				.siteId(siteId)
				.projectId(projectId)
				.projectAllocationId(projectAllocationId)
				.userId("userId2")
				.build()
		);
		Optional<UserGrantEntity> byId = userGrantEntityRepository.findById(userAdditionSaveEntity.getId());

		assertThat(byId).isPresent();
		assertThat(byId.get().userId).isEqualTo("userId2");
	}
}