/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.community_allocation;


import static io.imunity.furms.db.id.uuid.UUIDIdUtils.generateId;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_types.ResourceMeasureType;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.services.InfraServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;

@SpringBootTest
class CommunityAllocationEntityRepositoryTest extends DBIntegrationTest {

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
	private CommunityAllocationEntityRepository entityRepository;
	@Autowired
	private CommunityAllocationReadEntityRepository entityReadRepository;

	private UUID siteId;
	private UUID siteId2;

	private UUID communityId;
	private UUID communityId2;

	private UUID resourceTypeId;
	private UUID resourceTypeId2;

	private UUID resourceCreditId;
	private UUID resourceCreditId2;

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
			.splittable(true)
			.amount(new BigDecimal(100))
			.utcCreateTime(LocalDateTime.now())
			.utcStartTime(LocalDateTime.now().plusDays(1))
			.utcEndTime(LocalDateTime.now().plusDays(3))
			.build()));

		resourceCreditId2 = UUID.fromString(resourceCreditRepository.create(ResourceCredit.builder()
			.siteId(siteId2.toString())
			.resourceTypeId(resourceTypeId2.toString())
			.name("name2")
			.splittable(true)
			.amount(new BigDecimal(100))
			.utcCreateTime(LocalDateTime.now())
			.utcStartTime(LocalDateTime.now().plusDays(1))
			.utcEndTime(LocalDateTime.now().plusDays(3))
			.build()));
	}

	@Test
	void shouldReturnAvailableAmountWhenCommunityAllocationsDoesntExist() {
		BigDecimal sum = entityReadRepository.calculateAvailableAmount(resourceCreditId).getAmount();
		assertThat(sum).isEqualTo(new BigDecimal(100));
	}

	@Test
	void shouldReturnAvailableAmount() {
		entityRepository.save(
			CommunityAllocationEntity.builder()
				.communityId(communityId)
				.resourceCreditId(resourceCreditId)
				.name("anem")
				.amount(new BigDecimal(10))
				.build()
		);
		entityRepository.save(
			CommunityAllocationEntity.builder()
				.communityId(communityId)
				.resourceCreditId(resourceCreditId)
				.name("anem2")
				.amount(new BigDecimal(30))
				.build()
		);

		BigDecimal sum = entityReadRepository.calculateAvailableAmount(resourceCreditId).getAmount();
		assertThat(sum).isEqualTo(new BigDecimal(60));
	}

	@Test
	void shouldReturnAllocationWithRelatedObjects() {
		CommunityAllocationEntity save = entityRepository.save(
			CommunityAllocationEntity.builder()
				.communityId(communityId)
				.resourceCreditId(resourceCreditId)
				.name("anem")
				.amount(new BigDecimal(10))
				.build()
		);

		Optional<CommunityAllocationReadEntity> entity = entityReadRepository.findById(save.getId());
		assertThat(entity).isPresent();
		assertThat(entity.get().name).isEqualTo("anem");
		assertThat(entity.get().amount).isEqualTo(new BigDecimal(10));
		assertThat(entity.get().site.getName()).isEqualTo("name");
		assertThat(entity.get().resourceType.type).isEqualTo(ResourceMeasureType.FLOATING_POINT);
		assertThat(entity.get().resourceType.unit).isEqualTo(ResourceMeasureUnit.SiUnit.kilo);
		assertThat(entity.get().resourceCredit.name).isEqualTo("name");
		assertThat(entity.get().resourceCredit.split).isEqualTo(true);
		assertThat(entity.get().resourceCredit.amount).isEqualTo(new BigDecimal(100));
	}

	@Test
	void shouldReturnAllocationsWithRelatedObjects() {
		entityRepository.save(
			CommunityAllocationEntity.builder()
				.communityId(communityId)
				.resourceCreditId(resourceCreditId)
				.name("anem")
				.amount(new BigDecimal(10))
				.build()
		);

		Set<CommunityAllocationReadEntity> entities = entityReadRepository.findAllByCommunityId(communityId);
		assertThat(entities.size()).isEqualTo(1);
		CommunityAllocationReadEntity entity = entities.iterator().next();
		assertThat(entity.name).isEqualTo("anem");
		assertThat(entity.amount).isEqualTo(new BigDecimal(10));
		assertThat(entity.site.getName()).isEqualTo("name");
		assertThat(entity.resourceType.type).isEqualTo(ResourceMeasureType.FLOATING_POINT);
		assertThat(entity.resourceType.unit).isEqualTo(ResourceMeasureUnit.SiUnit.kilo);
		assertThat(entity.resourceCredit.name).isEqualTo("name");
		assertThat(entity.resourceCredit.split).isEqualTo(true);
		assertThat(entity.resourceCredit.amount).isEqualTo(new BigDecimal(100));
	}


	@Test
	void shouldCreateResourceType() {
		//given
		CommunityAllocationEntity entityToSave = CommunityAllocationEntity.builder()
			.communityId(communityId)
			.resourceCreditId(resourceCreditId)
			.name("name")
			.amount(new BigDecimal(10))
			.build();

		//when
		CommunityAllocationEntity saved = entityRepository.save(entityToSave);

		//then
		assertThat(entityRepository.findAll()).hasSize(1);
		Optional<CommunityAllocationEntity> byId = entityRepository.findById(saved.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().communityId).isEqualTo(communityId);
		assertThat(byId.get().resourceCreditId).isEqualTo(resourceCreditId);
		assertThat(byId.get().name).isEqualTo("name");
		assertThat(byId.get().amount).isEqualTo(new BigDecimal(10));
	}

	@Test
	void shouldUpdateCommunityAllocation() {
		//given
		CommunityAllocationEntity old = CommunityAllocationEntity.builder()
			.communityId(communityId)
			.resourceCreditId(resourceCreditId)
			.name("anem")
			.amount(new BigDecimal(10))
			.build();
		entityRepository.save(old);
		CommunityAllocationEntity toUpdate = CommunityAllocationEntity.builder()
			.communityId(communityId)
			.resourceCreditId(resourceCreditId)
			.name("anem2")
			.amount(new BigDecimal(102))
			.build();

		//when
		entityRepository.save(toUpdate);

		//then
		Optional<CommunityAllocationEntity> byId = entityRepository.findById(toUpdate.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().communityId).isEqualTo(communityId);
		assertThat(byId.get().resourceCreditId).isEqualTo(resourceCreditId);
		assertThat(byId.get().name).isEqualTo("anem2");
		assertThat(byId.get().amount).isEqualTo(new BigDecimal(102));
	}

	@Test
	void shouldFindCreatedCommunityAllocations() {
		//given
		CommunityAllocationEntity toFind = CommunityAllocationEntity.builder()
			.communityId(communityId)
			.resourceCreditId(resourceCreditId)
			.name("anem")
			.amount(new BigDecimal(10))
			.build();
		entityRepository.save(toFind);

		//when
		Optional<CommunityAllocationEntity> byId = entityRepository.findById(toFind.getId());

		//then
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindAllAvailableCommunityAllocations() {
		//given
		entityRepository.save(CommunityAllocationEntity.builder()
			.communityId(communityId)
			.resourceCreditId(resourceCreditId)
			.name("anem")
			.amount(new BigDecimal(10))
			.build()
		);
		entityRepository.save(CommunityAllocationEntity.builder()
			.communityId(communityId2)
			.resourceCreditId(resourceCreditId2)
			.name("anem2")
			.amount(new BigDecimal(120))
			.build()
		);

		//when
		Iterable<CommunityAllocationEntity> all = entityRepository.findAll();

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void savedServiceExistsByCommunityAllocationId() {
		//given
		CommunityAllocationEntity service = entityRepository.save(CommunityAllocationEntity.builder()
			.communityId(communityId)
			.resourceCreditId(resourceCreditId)
			.name("anem")
			.amount(new BigDecimal(10))
			.build());

		//when + then
		assertThat(entityRepository.existsById(service.getId())).isTrue();
		assertThat(entityRepository.existsById(generateId())).isFalse();
	}

	@Test
	void savedCommunityAllocationExistsByName() {
		//given
		CommunityAllocationEntity service = entityRepository.save(CommunityAllocationEntity.builder()
			.communityId(communityId)
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
	void savedCommunityAllocationDoesNotExistByName() {
		//given
		entityRepository.save(CommunityAllocationEntity.builder()
			.communityId(communityId)
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
		CommunityAllocationEntity entityToRemove = entityRepository.save(CommunityAllocationEntity.builder()
			.communityId(communityId)
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
		entityRepository.save(CommunityAllocationEntity.builder()
			.communityId(communityId)
			.resourceCreditId(resourceCreditId)
			.name("anem")
			.amount(new BigDecimal(10))
			.build());
		entityRepository.save(CommunityAllocationEntity.builder()
			.communityId(communityId2)
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