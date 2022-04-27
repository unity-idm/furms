/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.community_allocation;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
import io.imunity.furms.domain.images.FurmsImage;
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
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.services.InfraServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.db.id.uuid.UUIDIdUtils.generateId;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CommunityAllocationDatabaseRepositoryTest extends DBIntegrationTest {

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
	private CommunityAllocationDatabaseRepository entityDatabaseRepository;

	private SiteId siteId;

	private CommunityId communityId;
	private CommunityId communityId2;

	private ResourceTypeId resourceTypeId;

	private ResourceCreditId resourceCreditId;
	private ResourceCreditId resourceCreditId2;

	@BeforeEach
	void init() {
		Site site = Site.builder()
			.name("name")
			.connectionInfo("alala")
			.build();
		Site site1 = Site.builder()
			.name("name2")
			.connectionInfo("alala")
			.build();
		siteId = siteRepository.create(site, new SiteExternalId("id"));
		SiteId siteId2 = siteRepository.create(site1, new SiteExternalId("id2"));

		Community community = Community.builder()
			.name("name")
			.logo(FurmsImage.empty())
			.build();
		Community community2 = Community.builder()
			.name("name1")
			.logo(FurmsImage.empty())
			.build();
		communityId = communityRepository.create(community);
		communityId2 = communityRepository.create(community2);

		InfraService service = InfraService.builder()
			.siteId(siteId)
			.name("name")
			.build();
		InfraService service1 = InfraService.builder()
			.siteId(siteId2)
			.name("name1")
			.build();

		InfraServiceId serviceId = infraServiceRepository.create(service);
		InfraServiceId serviceId2 = infraServiceRepository.create(service1);


		ResourceType resourceType = ResourceType.builder()
			.siteId(siteId)
			.serviceId(serviceId)
			.name("name")
			.type(ResourceMeasureType.FLOATING_POINT)
			.unit(ResourceMeasureUnit.TERA)
			.build();
		ResourceType resourceType2 = ResourceType.builder()
			.siteId(siteId2)
			.serviceId(serviceId2)
			.name("name2")
			.type(ResourceMeasureType.DATA)
			.unit(ResourceMeasureUnit.MB)
			.build();

		resourceTypeId = resourceTypeRepository.create(resourceType);
		ResourceTypeId resourceTypeId2 = resourceTypeRepository.create(resourceType2);

		resourceCreditId = resourceCreditRepository.create(ResourceCredit.builder()
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
			.name("name")
			.splittable(true)
			.amount(new BigDecimal(100))
			.utcCreateTime(LocalDateTime.now())
			.utcStartTime(LocalDateTime.now().plusDays(1))
			.utcEndTime(LocalDateTime.now().plusDays(3))
			.build());

		resourceCreditId2 = resourceCreditRepository.create(ResourceCredit.builder()
			.siteId(siteId2)
			.resourceTypeId(resourceTypeId2)
			.name("name2")
			.splittable(true)
			.amount(new BigDecimal(100))
			.utcCreateTime(LocalDateTime.now())
			.utcStartTime(LocalDateTime.now().plusDays(1))
			.utcEndTime(LocalDateTime.now().plusDays(3))
			.build());
	}

	@Test
	void shouldReturnAllocationWithRelatedObjects() {
		CommunityAllocationEntity save = entityRepository.save(
			CommunityAllocationEntity.builder()
				.communityId(communityId.id)
				.resourceCreditId(resourceCreditId.id)
				.name("anem")
				.amount(new BigDecimal(10))
				.build()
		);

		Optional<CommunityAllocationResolved> entity =
			entityDatabaseRepository.findByIdWithRelatedObjects(new CommunityAllocationId(save.getId()));
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
	void shouldReturnAllNonExpiredAllocationWithRelatedObjects() {
		entityRepository.save(
				CommunityAllocationEntity.builder()
						.communityId(communityId.id)
						.resourceCreditId(resourceCreditId.id)
						.name("non-expired")
						.amount(new BigDecimal(10))
						.build());

		entityRepository.save(
				CommunityAllocationEntity.builder()
						.communityId(communityId.id)
						.resourceCreditId(resourceCreditRepository.create(ResourceCredit.builder()
								.siteId(siteId)
								.resourceTypeId(resourceTypeId)
								.name("name2")
								.splittable(true)
								.amount(new BigDecimal(100))
								.utcCreateTime(LocalDateTime.now())
								.utcStartTime(LocalDateTime.now().minusDays(10))
								.utcEndTime(LocalDateTime.now().minusDays(3))
								.build()).id
						)
						.name("expired")
						.amount(new BigDecimal(10))
						.build());

		Set<CommunityAllocationResolved> allocations = entityDatabaseRepository
				.findAllNotExpiredByCommunityIdWithRelatedObjects(communityId);
		assertThat(allocations).hasSize(1);
		assertThat(allocations.stream().findFirst().get().name).isEqualTo("non-expired");
	}

	@Test
	void shouldReturnAllocationsWithRelatedObjects() {
		entityRepository.save(
				CommunityAllocationEntity.builder()
						.communityId(communityId.id)
						.resourceCreditId(resourceCreditId.id)
						.name("anem")
						.amount(new BigDecimal(10))
						.build()
		);

		Set<CommunityAllocationResolved> entities = entityDatabaseRepository.findAllByCommunityIdWithRelatedObjects(communityId);
		assertThat(entities.size()).isEqualTo(1);
		CommunityAllocationResolved entity = entities.iterator().next();
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
	void shouldReturnAllByCommunityIdAndNameAllocationsWithRelatedObjects() {
		//given
		final LocalDateTime now = LocalDateTime.now();
		final ResourceCreditId expiredResource = resourceCreditRepository.create(ResourceCredit.builder()
				.siteId(siteId)
				.resourceTypeId(resourceTypeId)
				.name("expiredRes")
				.splittable(true)
				.amount(new BigDecimal(100))
				.utcCreateTime(now)
				.utcStartTime(now.minusSeconds(2))
				.utcEndTime(now.minusSeconds(1))
				.build());
		entityRepository.save(
				CommunityAllocationEntity.builder()
						.communityId(communityId.id)
						.resourceCreditId(expiredResource.id)
						.name("nameToFind")
						.amount(new BigDecimal(10))
						.build()
		);
		entityRepository.save(
				CommunityAllocationEntity.builder()
						.communityId(communityId.id)
						.resourceCreditId(resourceCreditId.id)
						.name("other")
						.amount(new BigDecimal(10))
						.build()
		);


		final Set<CommunityAllocationResolved> all = entityDatabaseRepository
				.findAllByCommunityIdAndNameOrSiteNameWithRelatedObjects(communityId, "oFind");
		assertThat(all.size()).isEqualTo(1);
		final CommunityAllocationResolved entity = all.stream().findFirst().get();
		assertThat(entity.name).isEqualTo("nameToFind");
	}

	@Test
	void shouldReturnNotExpiredAllocationsWithRelatedObjects() {
		//given
		final LocalDateTime now = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
		final ResourceCreditId expiredResource = resourceCreditRepository.create(ResourceCredit.builder()
				.siteId(siteId)
				.resourceTypeId(resourceTypeId)
				.name("expiredRes")
				.splittable(true)
				.amount(new BigDecimal(100))
				.utcCreateTime(now)
				.utcStartTime(now.minusSeconds(2))
				.utcEndTime(now.minusSeconds(1))
				.build());
		entityRepository.save(
				CommunityAllocationEntity.builder()
						.communityId(communityId.id)
						.resourceCreditId(expiredResource.id)
						.name("expired")
						.amount(new BigDecimal(10))
						.build()
		);
		entityRepository.save(
				CommunityAllocationEntity.builder()
						.communityId(communityId.id)
						.resourceCreditId(resourceCreditId.id)
						.name("not-expired")
						.amount(new BigDecimal(10))
						.build()
		);


		final Set<CommunityAllocationResolved> all = entityDatabaseRepository
				.findAllNotExpiredByCommunityIdAndNameOrSiteNameWithRelatedObjects(communityId, "");
		assertThat(all.size()).isEqualTo(1);
		final CommunityAllocationResolved entity = all.stream().findFirst().get();
		assertThat(entity.name).isEqualTo("not-expired");
	}

	@Test
	void shouldReturnNotExpiredAllocationsWithRelatedObjectsFilteredByName() {
		//given
		final LocalDateTime now = LocalDateTime.now();
		final ResourceCreditId expiredResource = resourceCreditRepository.create(ResourceCredit.builder()
				.siteId(siteId)
				.resourceTypeId(resourceTypeId)
				.name("expiredRes")
				.splittable(true)
				.amount(new BigDecimal(100))
				.utcCreateTime(now)
				.utcStartTime(now.minusSeconds(2))
				.utcEndTime(now.minusSeconds(1))
				.build());
		entityRepository.save(
				CommunityAllocationEntity.builder()
						.communityId(communityId.id)
						.resourceCreditId(expiredResource.id)
						.name("unnecessary-expired")
						.amount(new BigDecimal(10))
						.build()
		);
		entityRepository.save(
				CommunityAllocationEntity.builder()
						.communityId(communityId.id)
						.resourceCreditId(resourceCreditId.id)
						.name("toFindAll")
						.amount(new BigDecimal(10))
						.build()
		);
		entityRepository.save(
				CommunityAllocationEntity.builder()
						.communityId(communityId.id)
						.resourceCreditId(resourceCreditId.id)
						.name("unnecessary")
						.amount(new BigDecimal(10))
						.build()
		);


		final Set<CommunityAllocationResolved> all = entityDatabaseRepository
				.findAllNotExpiredByCommunityIdAndNameOrSiteNameWithRelatedObjects(communityId, "toFind");
		assertThat(all.size()).isEqualTo(1);
		final CommunityAllocationResolved entity = all.stream().findFirst().get();
		assertThat(entity.name).isEqualTo("toFindAll");
	}

	@Test
	void shouldFindCreatedService() {
		//given
		CommunityAllocationEntity entity = entityRepository.save(CommunityAllocationEntity.builder()
			.communityId(communityId.id)
			.resourceCreditId(resourceCreditId.id)
			.name("name")
			.amount(new BigDecimal(10))
			.build()
		);

		//when
		Optional<CommunityAllocation> byId =
			entityDatabaseRepository.findById(new CommunityAllocationId(entity.getId()));

		//then
		assertThat(byId).isPresent();
		CommunityAllocation allocation = byId.get();
		assertThat(allocation.id.id).isEqualTo(entity.getId());
		assertThat(allocation.communityId.id).isEqualTo(entity.communityId);
		assertThat(allocation.resourceCreditId.id).isEqualTo(entity.resourceCreditId);
		assertThat(allocation.name).isEqualTo(entity.name);
		assertThat(byId.get().amount).isEqualTo(new BigDecimal(10));
	}

	@Test
	void shouldNotFindByIdIfDoesntExist() {
		//given
		UUID wrongId = generateId();
		entityRepository.save(CommunityAllocationEntity.builder()
			.communityId(communityId.id)
			.resourceCreditId(resourceCreditId.id)
			.name("name")
			.amount(new BigDecimal(10))
			.build()
		);

		//when
		Optional<CommunityAllocation> byId = entityDatabaseRepository.findById(new CommunityAllocationId(wrongId));

		//then
		assertThat(byId).isEmpty();
	}

	@Test
	void shouldFindAllCommunityAllocations() {
		//given
		entityRepository.save(CommunityAllocationEntity.builder()
			.communityId(communityId.id)
			.resourceCreditId(resourceCreditId.id)
			.name("name")
			.amount(new BigDecimal(10))
			.build()
		);
		entityRepository.save(CommunityAllocationEntity.builder()
			.communityId(communityId2.id)
			.resourceCreditId(resourceCreditId2.id)
			.name("name2")
			.amount(new BigDecimal(10))
			.build()
		);

		//when
		Set<CommunityAllocation> all = entityDatabaseRepository.findAll();

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void shouldFindAllCommunityAllocationsByCommunityId() {
		//given
		entityRepository.save(CommunityAllocationEntity.builder()
				.communityId(communityId.id)
				.resourceCreditId(resourceCreditId.id)
				.name("name")
				.amount(new BigDecimal(10))
				.build()
		);
		entityRepository.save(CommunityAllocationEntity.builder()
				.communityId(communityId2.id)
				.resourceCreditId(resourceCreditId2.id)
				.name("name2")
				.amount(new BigDecimal(10))
				.build()
		);
		entityRepository.save(CommunityAllocationEntity.builder()
				.communityId(communityId2.id)
				.resourceCreditId(resourceCreditId2.id)
				.name("name3")
				.amount(new BigDecimal(10))
				.build()
		);

		//when
		Set<CommunityAllocation> all = entityDatabaseRepository.findAllByCommunityId(communityId);
		Set<CommunityAllocation> all2 = entityDatabaseRepository.findAllByCommunityId(communityId2);

		//then
		assertThat(all).hasSize(1);
		assertThat(all2).hasSize(2);
	}

	@Test
	void shouldCreateCommunityAllocation() {
		//given
		CommunityAllocation request = CommunityAllocation.builder()
			.communityId(communityId)
			.resourceCreditId(resourceCreditId)
			.name("name")
			.amount(new BigDecimal(10))
			.build();

		//when
		CommunityAllocationId newCommunityAllocationId = entityDatabaseRepository.create(request);

		//then
		Optional<CommunityAllocation> byId = entityDatabaseRepository.findById(newCommunityAllocationId);
		assertThat(byId).isPresent();
		assertThat(byId.get().id).isEqualTo(newCommunityAllocationId);
		assertThat(byId.get().communityId).isEqualTo(communityId);
		assertThat(byId.get().resourceCreditId).isEqualTo(resourceCreditId);
		assertThat(byId.get().name).isEqualTo("name");
		assertThat(byId.get().amount).isEqualTo(new BigDecimal(10));
	}

	@Test
	void shouldUpdateCommunityAllocation() {
		//given
		CommunityAllocationEntity old = entityRepository.save(CommunityAllocationEntity.builder()
			.communityId(communityId.id)
			.resourceCreditId(resourceCreditId.id)
			.name("name")
			.amount(new BigDecimal(10))
			.build()
		);
		CommunityAllocation requestToUpdate = CommunityAllocation.builder()
			.id(old.getId().toString())
			.communityId(communityId)
			.resourceCreditId(resourceCreditId)
			.name("new_name")
			.amount(new BigDecimal(101))
			.build();

		//when
		entityDatabaseRepository.update(requestToUpdate);

		//then
		Optional<CommunityAllocation> byId = entityDatabaseRepository.findById(new CommunityAllocationId(old.getId()));
		assertThat(byId).isPresent();
		assertThat(byId.get().name).isEqualTo("new_name");
		assertThat(byId.get().communityId).isEqualTo(communityId);
		assertThat(byId.get().resourceCreditId).isEqualTo(resourceCreditId);
		assertThat(byId.get().amount).isEqualTo(new BigDecimal(101));
	}

	@Test
	void savedCommunityAllocationExists() {
		//given
		CommunityAllocationEntity entity = entityRepository.save(CommunityAllocationEntity.builder()
			.communityId(communityId.id)
			.resourceCreditId(resourceCreditId.id)
			.name("name")
			.amount(new BigDecimal(10))
			.build()
		);

		//when + then
		assertThat(entityDatabaseRepository.exists(new CommunityAllocationId(entity.getId()))).isTrue();
	}

	@Test
	void shouldNotExistsDueToEmptyOrWrongId() {
		//given
		CommunityAllocationId nonExistedId = new CommunityAllocationId(generateId());

		//when + then
		assertThat(entityDatabaseRepository.exists(nonExistedId)).isFalse();
		assertThat(entityDatabaseRepository.exists(null)).isFalse();
		assertThat(entityDatabaseRepository.exists(new CommunityAllocationId((UUID)null))).isFalse();
	}

	@Test
	void shouldReturnTrueForUniqueName() {
		//given
		entityRepository.save(CommunityAllocationEntity.builder()
			.communityId(communityId.id)
			.resourceCreditId(resourceCreditId.id)
			.name("name")
			.amount(new BigDecimal(10))
			.build()
		);
		String uniqueName = "unique_name";

		//when + then
		assertThat(entityDatabaseRepository.isUniqueName(uniqueName)).isTrue();
	}

	@Test
	void shouldReturnFalseForNonUniqueName() {
		//given
		CommunityAllocationEntity existedCommunityAllocation = entityRepository.save(CommunityAllocationEntity.builder()
			.communityId(communityId.id)
			.resourceCreditId(resourceCreditId.id)
			.name("name")
			.amount(new BigDecimal(10))
			.build());

		//when + then
		assertThat(entityDatabaseRepository.isUniqueName(existedCommunityAllocation.name)).isFalse();
	}

	@Test
	void shouldReturnTrueForExistingResourceCreditId() {
		//given
		CommunityAllocationEntity existedResourceCredit = entityRepository.save(CommunityAllocationEntity.builder()
			.communityId(communityId.id)
			.resourceCreditId(resourceCreditId.id)
			.name("name")
			.amount(new BigDecimal(10))
			.build());

		//when + then
		assertThat(entityRepository.existsByResourceCreditId(existedResourceCredit.resourceCreditId)).isTrue();
	}

	@Test
	void shouldReturnFalseForNonExistingResourceCreditId() {
		//when + then
		assertThat(entityRepository.existsByResourceCreditId(UUID.randomUUID())).isFalse();
	}

}