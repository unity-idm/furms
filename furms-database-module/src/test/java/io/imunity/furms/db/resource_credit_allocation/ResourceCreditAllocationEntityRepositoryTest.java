/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_credit_allocation;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_types.ResourceMeasureType;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.spi.communites.CommunityRepository;
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

import static io.imunity.furms.db.id.uuid.UUIDIdUtils.generateId;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ResourceCreditAllocationEntityRepositoryTest extends DBIntegrationTest {

	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private CommunityRepository communityRepository;
	@Autowired
	private InfraServiceRepository infraServiceRepository;
	@Autowired
	private ResourceTypeRepository resourceTypeRepository;
	@Autowired
	private ResourceCreditRepository resourceCreditRepository;
	@Autowired
	private ResourceCreditAllocationEntityRepository entityRepository;
	@Autowired
	private ResourceCreditAllocationReadEntityRepository entityReadRepository;

	private UUID siteId;
	private UUID siteId2;

	private UUID communityId;
	private UUID communityId2;

	private UUID resourceTypeId;
	private UUID resourceTypeId2;

	private UUID resourceCreditId;
	private UUID resourceCreditId2;

	private LocalDateTime startTime = LocalDateTime.of(2020, 5, 20, 5, 12, 16);
	private LocalDateTime endTime = LocalDateTime.of(2021, 6, 21, 4, 18, 4);
	private LocalDateTime newStartTime = LocalDateTime.of(2020, 8, 3, 4, 7, 5);
	private LocalDateTime newEndTime = LocalDateTime.of(2021, 9, 13, 3, 35, 33);
	private LocalDateTime createTime = LocalDateTime.of(2020, 1, 30, 5, 8, 8);
	private LocalDateTime createTime2 = LocalDateTime.of(2021, 8, 23, 8, 18, 18);

	@BeforeEach
	void init() throws IOException {
		Site site = Site.builder()
			.name("name")
			.build();
		Site site1 = Site.builder()
			.name("name2")
			.build();
		siteId = UUID.fromString(siteRepository.create(site));
		siteId2 = UUID.fromString(siteRepository.create(site1));

		Community community = Community.builder()
			.name("name")
			.logo(FurmsImage.empty())
			.build();
		Community community2 = Community.builder()
			.name("name1")
			.logo(FurmsImage.empty())
			.build();
		communityId = UUID.fromString(communityRepository.create(community));
		communityId2 = UUID.fromString(communityRepository.create(community2));

		InfraService service = InfraService.builder()
			.siteId(siteId.toString())
			.name("name")
			.build();
		InfraService service1 = InfraService.builder()
			.siteId(siteId2.toString())
			.name("name1")
			.build();

		UUID serviceId = UUID.fromString(infraServiceRepository.create(service));
		UUID serviceId2 = UUID.fromString(infraServiceRepository.create(service1));


		ResourceType resourceType = ResourceType.builder()
			.siteId(siteId.toString())
			.serviceId(serviceId.toString())
			.name("name")
			.type(ResourceMeasureType.FLOATING_POINT)
			.unit(ResourceMeasureUnit.SiUnit.kilo)
			.build();
		ResourceType resourceType2 = ResourceType.builder()
			.siteId(siteId2.toString())
			.serviceId(serviceId2.toString())
			.name("name2")
			.type(ResourceMeasureType.DATA)
			.unit(ResourceMeasureUnit.DataUnit.MB)
			.build();

		resourceTypeId = UUID.fromString(resourceTypeRepository.create(resourceType));
		resourceTypeId2 = UUID.fromString(resourceTypeRepository.create(resourceType2));

		resourceCreditId = UUID.fromString(resourceCreditRepository.create(ResourceCredit.builder()
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

		resourceCreditId2 = UUID.fromString(resourceCreditRepository.create(ResourceCredit.builder()
			.siteId(siteId2.toString())
			.resourceTypeId(resourceTypeId2.toString())
			.name("name2")
			.split(true)
			.access(true)
			.amount(new BigDecimal(100))
			.utcCreateTime(LocalDateTime.now())
			.utcStartTime(LocalDateTime.now().plusDays(1))
			.utcEndTime(LocalDateTime.now().plusDays(3))
			.build()));
	}


	@Test
	void shouldReturnAllocationWithRelatedObjects() {
		ResourceCreditAllocationEntity save = entityRepository.save(
			ResourceCreditAllocationEntity.builder()
				.siteId(siteId)
				.communityId(communityId)
				.resourceTypeId(resourceTypeId)
				.resourceCreditId(resourceCreditId)
				.name("anem")
				.amount(new BigDecimal(10))
				.build()
		);

		Optional<ResourceCreditAllocationReadEntity> entity = entityReadRepository.findById(save.getId());
		assertThat(entity).isPresent();
		assertThat(entity.get().name).isEqualTo("anem");
		assertThat(entity.get().amount).isEqualTo(new BigDecimal(10));
		assertThat(entity.get().site.getName()).isEqualTo("name");
		assertThat(entity.get().resourceType.type).isEqualTo(ResourceMeasureType.FLOATING_POINT);
		assertThat(entity.get().resourceType.unit).isEqualTo(ResourceMeasureUnit.SiUnit.kilo);
		assertThat(entity.get().resourceCredit.name).isEqualTo("name");
		assertThat(entity.get().resourceCredit.split).isEqualTo(true);
		assertThat(entity.get().resourceCredit.access).isEqualTo(true);
		assertThat(entity.get().resourceCredit.amount).isEqualTo(new BigDecimal(100));
	}

	@Test
	void shouldReturnAllocationsWithRelatedObjects() {
		ResourceCreditAllocationEntity save = entityRepository.save(
			ResourceCreditAllocationEntity.builder()
				.siteId(siteId)
				.communityId(communityId)
				.resourceTypeId(resourceTypeId)
				.resourceCreditId(resourceCreditId)
				.name("anem")
				.amount(new BigDecimal(10))
				.build()
		);

		Set<ResourceCreditAllocationReadEntity> entities = entityReadRepository.findAllByCommunityId(communityId);
		assertThat(entities.size()).isEqualTo(1);
		ResourceCreditAllocationReadEntity entity = entities.iterator().next();
		assertThat(entity.name).isEqualTo("anem");
		assertThat(entity.amount).isEqualTo(new BigDecimal(10));
		assertThat(entity.site.getName()).isEqualTo("name");
		assertThat(entity.resourceType.type).isEqualTo(ResourceMeasureType.FLOATING_POINT);
		assertThat(entity.resourceType.unit).isEqualTo(ResourceMeasureUnit.SiUnit.kilo);
		assertThat(entity.resourceCredit.name).isEqualTo("name");
		assertThat(entity.resourceCredit.split).isEqualTo(true);
		assertThat(entity.resourceCredit.access).isEqualTo(true);
		assertThat(entity.resourceCredit.amount).isEqualTo(new BigDecimal(100));
	}


	@Test
	void shouldCreateResourceType() {
		//given
		ResourceCreditAllocationEntity entityToSave = ResourceCreditAllocationEntity.builder()
			.siteId(siteId)
			.communityId(communityId)
			.resourceTypeId(resourceTypeId)
			.resourceCreditId(resourceCreditId)
			.name("name")
			.amount(new BigDecimal(10))
			.build();

		//when
		ResourceCreditAllocationEntity saved = entityRepository.save(entityToSave);

		//then
		assertThat(entityRepository.findAll()).hasSize(1);
		Optional<ResourceCreditAllocationEntity> byId = entityRepository.findById(saved.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().siteId).isEqualTo(siteId);
		assertThat(byId.get().communityId).isEqualTo(communityId);
		assertThat(byId.get().resourceTypeId).isEqualTo(resourceTypeId);
		assertThat(byId.get().resourceCreditId).isEqualTo(resourceCreditId);
		assertThat(byId.get().name).isEqualTo("name");
		assertThat(byId.get().amount).isEqualTo(new BigDecimal(10));
	}

	@Test
	void shouldUpdateResourceCreditAllocation() {
		//given
		ResourceCreditAllocationEntity old = ResourceCreditAllocationEntity.builder()
			.siteId(siteId)
			.communityId(communityId)
			.resourceTypeId(resourceTypeId)
			.resourceCreditId(resourceCreditId)
			.name("anem")
			.amount(new BigDecimal(10))
			.build();
		entityRepository.save(old);
		ResourceCreditAllocationEntity toUpdate = ResourceCreditAllocationEntity.builder()
			.siteId(siteId)
			.communityId(communityId)
			.resourceTypeId(resourceTypeId)
			.resourceCreditId(resourceCreditId)
			.name("anem2")
			.amount(new BigDecimal(102))
			.build();

		//when
		entityRepository.save(toUpdate);

		//then
		Optional<ResourceCreditAllocationEntity> byId = entityRepository.findById(toUpdate.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().siteId).isEqualTo(siteId);
		assertThat(byId.get().communityId).isEqualTo(communityId);
		assertThat(byId.get().resourceTypeId).isEqualTo(resourceTypeId);
		assertThat(byId.get().resourceCreditId).isEqualTo(resourceCreditId);
		assertThat(byId.get().name).isEqualTo("anem2");
		assertThat(byId.get().amount).isEqualTo(new BigDecimal(102));
	}

	@Test
	void shouldFindCreatedResourceCreditAllocations() {
		//given
		ResourceCreditAllocationEntity toFind = ResourceCreditAllocationEntity.builder()
			.siteId(siteId)
			.communityId(communityId)
			.resourceTypeId(resourceTypeId)
			.resourceCreditId(resourceCreditId)
			.name("anem")
			.amount(new BigDecimal(10))
			.build();
		entityRepository.save(toFind);

		//when
		Optional<ResourceCreditAllocationEntity> byId = entityRepository.findById(toFind.getId());

		//then
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindAllAvailableResourceCreditAllocations() {
		//given
		entityRepository.save(ResourceCreditAllocationEntity.builder()
			.siteId(siteId)
			.communityId(communityId)
			.resourceTypeId(resourceTypeId)
			.resourceCreditId(resourceCreditId)
			.name("anem")
			.amount(new BigDecimal(10))
			.build()
		);
		entityRepository.save(ResourceCreditAllocationEntity.builder()
			.siteId(siteId2)
			.communityId(communityId2)
			.resourceTypeId(resourceTypeId2)
			.resourceCreditId(resourceCreditId2)
			.name("anem2")
			.amount(new BigDecimal(120))
			.build()
		);

		//when
		Iterable<ResourceCreditAllocationEntity> all = entityRepository.findAll();

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void savedServiceExistsByResourceCreditAllocationId() {
		//given
		ResourceCreditAllocationEntity service = entityRepository.save(ResourceCreditAllocationEntity.builder()
			.siteId(siteId)
			.communityId(communityId)
			.resourceTypeId(resourceTypeId)
			.resourceCreditId(resourceCreditId)
			.name("anem")
			.amount(new BigDecimal(10))
			.build());

		//when + then
		assertThat(entityRepository.existsById(service.getId())).isTrue();
		assertThat(entityRepository.existsById(generateId())).isFalse();
	}

	@Test
	void savedResourceCreditAllocationExistsByName() {
		//given
		ResourceCreditAllocationEntity service = entityRepository.save(ResourceCreditAllocationEntity.builder()
			.siteId(siteId)
			.communityId(communityId)
			.resourceTypeId(resourceTypeId)
			.resourceCreditId(resourceCreditId)
			.name("anem")
			.amount(new BigDecimal(10))
			.build());

		//when
		boolean exists = entityRepository.existsByName(service.name);

		//then
		assertThat(exists).isTrue();
	}

	@Test
	void savedResourceCreditAllocationDoesNotExistByName() {
		//given
		ResourceCreditAllocationEntity service = entityRepository.save(ResourceCreditAllocationEntity.builder()
			.siteId(siteId)
			.communityId(communityId)
			.resourceTypeId(resourceTypeId)
			.resourceCreditId(resourceCreditId)
			.name("anem")
			.amount(new BigDecimal(10))
			.build());

		//when
		boolean nonExists = entityRepository.existsByName("wrong_name");

		//then
		assertThat(nonExists).isFalse();
	}

	@Test
	void shouldDeleteService() {
		//given
		ResourceCreditAllocationEntity entityToRemove = entityRepository.save(ResourceCreditAllocationEntity.builder()
			.siteId(siteId)
			.communityId(communityId)
			.resourceTypeId(resourceTypeId)
			.resourceCreditId(resourceCreditId)
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
		entityRepository.save(ResourceCreditAllocationEntity.builder()
			.siteId(siteId)
			.communityId(communityId)
			.resourceTypeId(resourceTypeId)
			.resourceCreditId(resourceCreditId)
			.name("anem")
			.amount(new BigDecimal(10))
			.build());
		entityRepository.save(ResourceCreditAllocationEntity.builder()
			.siteId(siteId2)
			.communityId(communityId2)
			.resourceTypeId(resourceTypeId2)
			.resourceCreditId(resourceCreditId2)
			.name("anem2")
			.amount(new BigDecimal(10))
			.build());

		//when
		entityRepository.deleteAll();

		//then
		assertThat(entityRepository.findAll()).hasSize(0);
	}

}