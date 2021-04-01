/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.community_allocation;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
	private CommunityAllocationReadEntityRepository entityReadRepository;

	@Autowired
	private CommunityAllocationEntityRepository entityRepository;

	@Autowired
	private CommunityAllocationDatabaseRepository entityDatabaseRepository;

	private UUID siteId;
	private UUID siteId2;

	private UUID communityId;
	private UUID communityId2;

	private UUID resourceTypeId;
	private UUID resourceTypeId2;

	private UUID resourceCreditId;
	private UUID resourceCreditId2;

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
			.unit(ResourceMeasureUnit.SiUnit.tera)
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
		CommunityAllocationEntity save = entityRepository.save(
			CommunityAllocationEntity.builder()
				.communityId(communityId)
				.resourceCreditId(resourceCreditId)
				.name("anem")
				.amount(new BigDecimal(10))
				.build()
		);

		Optional<CommunityAllocationResolved> entity = entityDatabaseRepository.findByIdWithRelatedObjects(save.getId().toString());
		assertThat(entity).isPresent();
		assertThat(entity.get().name).isEqualTo("anem");
		assertThat(entity.get().amount).isEqualTo(new BigDecimal(10));
		assertThat(entity.get().site.getName()).isEqualTo("name");
		assertThat(entity.get().resourceType.type).isEqualTo(ResourceMeasureType.FLOATING_POINT);
		assertThat(entity.get().resourceType.unit).isEqualTo(ResourceMeasureUnit.SiUnit.tera);
		assertThat(entity.get().resourceCredit.name).isEqualTo("name");
		assertThat(entity.get().resourceCredit.split).isEqualTo(true);
		assertThat(entity.get().resourceCredit.access).isEqualTo(true);
		assertThat(entity.get().resourceCredit.amount).isEqualTo(new BigDecimal(100));
	}

	@Test
	void shouldReturnAllocationsWithRelatedObjects() {
		CommunityAllocationEntity save = entityRepository.save(
			CommunityAllocationEntity.builder()
				.communityId(communityId)
				.resourceCreditId(resourceCreditId)
				.name("anem")
				.amount(new BigDecimal(10))
				.build()
		);

		Set<CommunityAllocationResolved> entities = entityDatabaseRepository.findAllWithRelatedObjects(communityId.toString());
		assertThat(entities.size()).isEqualTo(1);
		CommunityAllocationResolved entity = entities.iterator().next();
		assertThat(entity.name).isEqualTo("anem");
		assertThat(entity.amount).isEqualTo(new BigDecimal(10));
		assertThat(entity.site.getName()).isEqualTo("name");
		assertThat(entity.resourceType.type).isEqualTo(ResourceMeasureType.FLOATING_POINT);
		assertThat(entity.resourceType.unit).isEqualTo(ResourceMeasureUnit.SiUnit.tera);
		assertThat(entity.resourceCredit.name).isEqualTo("name");
		assertThat(entity.resourceCredit.split).isEqualTo(true);
		assertThat(entity.resourceCredit.access).isEqualTo(true);
		assertThat(entity.resourceCredit.amount).isEqualTo(new BigDecimal(100));
	}

	@Test
	void shouldFindCreatedService() {
		//given
		CommunityAllocationEntity entity = entityRepository.save(CommunityAllocationEntity.builder()
			.communityId(communityId)
			.resourceCreditId(resourceCreditId)
			.name("name")
			.amount(new BigDecimal(10))
			.build()
		);

		//when
		Optional<CommunityAllocation> byId = entityDatabaseRepository.findById(entity.getId().toString());

		//then
		assertThat(byId).isPresent();
		CommunityAllocation allocation = byId.get();
		assertThat(allocation.id).isEqualTo(entity.getId().toString());
		assertThat(allocation.communityId).isEqualTo(entity.communityId.toString());
		assertThat(allocation.resourceCreditId).isEqualTo(entity.resourceCreditId.toString());
		assertThat(allocation.name).isEqualTo(entity.name);
		assertThat(byId.get().amount).isEqualTo(new BigDecimal(10));
	}

	@Test
	void shouldNotFindByIdIfDoesntExist() {
		//given
		UUID wrongId = generateId();
		entityRepository.save(CommunityAllocationEntity.builder()
			.communityId(communityId)
			.resourceCreditId(resourceCreditId)
			.name("name")
			.amount(new BigDecimal(10))
			.build()
		);

		//when
		Optional<CommunityAllocation> byId = entityDatabaseRepository.findById(wrongId.toString());

		//then
		assertThat(byId).isEmpty();
	}

	@Test
	void shouldFindAllCommunityAllocations() {
		//given
		entityRepository.save(CommunityAllocationEntity.builder()
			.communityId(communityId)
			.resourceCreditId(resourceCreditId)
			.name("name")
			.amount(new BigDecimal(10))
			.build()
		);
		entityRepository.save(CommunityAllocationEntity.builder()
			.communityId(communityId2)
			.resourceCreditId(resourceCreditId2)
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
	void shouldCreateCommunityAllocation() {
		//given
		CommunityAllocation request = CommunityAllocation.builder()
			.communityId(communityId.toString())
			.resourceCreditId(resourceCreditId.toString())
			.name("name")
			.amount(new BigDecimal(10))
			.build();

		//when
		String newCommunityAllocationId = entityDatabaseRepository.create(request);

		//then
		Optional<CommunityAllocation> byId = entityDatabaseRepository.findById(newCommunityAllocationId);
		assertThat(byId).isPresent();
		assertThat(byId.get().id).isEqualTo(newCommunityAllocationId);
		assertThat(byId.get().communityId).isEqualTo(communityId.toString());
		assertThat(byId.get().resourceCreditId).isEqualTo(resourceCreditId.toString());
		assertThat(byId.get().name).isEqualTo("name");
		assertThat(byId.get().amount).isEqualTo(new BigDecimal(10));
	}

	@Test
	void shouldUpdateCommunityAllocation() {
		//given
		CommunityAllocationEntity old = entityRepository.save(CommunityAllocationEntity.builder()
			.communityId(communityId)
			.resourceCreditId(resourceCreditId)
			.name("name")
			.amount(new BigDecimal(10))
			.build()
		);
		CommunityAllocation requestToUpdate = CommunityAllocation.builder()
			.id(old.getId().toString())
			.communityId(communityId.toString())
			.resourceCreditId(resourceCreditId.toString())
			.name("new_name")
			.amount(new BigDecimal(101))
			.build();

		//when
		entityDatabaseRepository.update(requestToUpdate);

		//then
		Optional<CommunityAllocation> byId = entityDatabaseRepository.findById(old.getId().toString());
		assertThat(byId).isPresent();
		assertThat(byId.get().name).isEqualTo("new_name");
		assertThat(byId.get().communityId).isEqualTo(communityId.toString());
		assertThat(byId.get().resourceCreditId).isEqualTo(resourceCreditId.toString());
		assertThat(byId.get().amount).isEqualTo(new BigDecimal(101));
	}

	@Test
	void savedCommunityAllocationExists() {
		//given
		CommunityAllocationEntity entity = entityRepository.save(CommunityAllocationEntity.builder()
			.communityId(communityId)
			.resourceCreditId(resourceCreditId)
			.name("name")
			.amount(new BigDecimal(10))
			.build()
		);

		//when + then
		assertThat(entityDatabaseRepository.exists(entity.getId().toString())).isTrue();
	}

	@Test
	void shouldNotExistsDueToEmptyOrWrongId() {
		//given
		String nonExistedId = generateId().toString();

		//when + then
		assertThat(entityDatabaseRepository.exists(nonExistedId)).isFalse();
		assertThat(entityDatabaseRepository.exists(null)).isFalse();
		assertThat(entityDatabaseRepository.exists("")).isFalse();
	}

	@Test
	void shouldReturnTrueForUniqueName() {
		//given
		entityRepository.save(CommunityAllocationEntity.builder()
			.communityId(communityId)
			.resourceCreditId(resourceCreditId)
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
			.communityId(communityId)
			.resourceCreditId(resourceCreditId)
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
			.communityId(communityId)
			.resourceCreditId(resourceCreditId)
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