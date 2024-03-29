/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_credits;


import io.imunity.furms.db.DBIntegrationTest;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.db.id.uuid.UUIDIdUtils.generateId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ResourceCreditDatabaseRepositoryTest extends DBIntegrationTest {

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private InfraServiceRepository infraServiceRepository;

	@Autowired
	private ResourceTypeRepository resourceTypeRepository;

	@Autowired
	private ResourceCreditDatabaseRepository repository;

	@Autowired
	private ResourceCreditEntityRepository entityRepository;

	private SiteId siteId;

	private ResourceTypeId resourceTypeId;
	private ResourceTypeId resourceTypeId2;

	private final LocalDateTime startTime = LocalDateTime.of(2020, 5, 20, 5, 12, 16);
	private final LocalDateTime endTime = LocalDateTime.of(2021, 6, 21, 4, 18, 4);
	private final LocalDateTime newStartTime = LocalDateTime.of(2020, 8, 3, 4, 7, 5);
	private final LocalDateTime newEndTime = LocalDateTime.of(2021, 9, 13, 3, 35, 33);
	private final LocalDateTime createTime = LocalDateTime.of(2020, 1, 30, 5, 8, 8);
	private final LocalDateTime createTime2 = LocalDateTime.of(2021, 8, 23, 8, 18, 18);

	@BeforeEach
	void init() {
		Site site = Site.builder()
			.name("name")
			.build();
		Site site1 = Site.builder()
			.name("name2")
			.build();
		siteId = siteRepository.create(site, new SiteExternalId("id"));
		SiteId siteId2 = siteRepository.create(site1, new SiteExternalId("id2"));

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
		resourceTypeId2 = resourceTypeRepository.create(resourceType2);
	}

	@Test
	void shouldFindCreatedService() {
		//given
		ResourceCreditEntity entity = entityRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId.id)
			.resourceTypeId(resourceTypeId.id)
			.name("name")
			.split(true)
			.amount(new BigDecimal(100))
			.createTime(createTime)
			.startTime(startTime)
			.endTime(endTime)
			.build()
		);

		//when
		Optional<ResourceCredit> byId = repository.findById(new ResourceCreditId(entity.getId()));

		//then
		assertThat(byId).isPresent();
		ResourceCredit project = byId.get();
		assertThat(project.id.id).isEqualTo(entity.getId());
		assertThat(project.name).isEqualTo(entity.name);
		assertThat(byId.get().splittable).isEqualTo(true);
		assertThat(byId.get().amount.compareTo(new BigDecimal(100))).isEqualTo(0);
		assertThat(byId.get().utcCreateTime).isEqualTo(createTime);
		assertThat(byId.get().utcStartTime).isEqualTo(startTime);
		assertThat(byId.get().utcEndTime).isEqualTo(endTime);
	}

	@Test
	void shouldNotFindByIdIfDoesntExist() {
		//given
		ResourceCreditId wrongId = new ResourceCreditId(generateId());
		entityRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId.id)
			.resourceTypeId(resourceTypeId.id)
			.name("name")
			.split(true)
			.amount(new BigDecimal(100))
			.createTime(createTime)
			.startTime(startTime)
			.endTime(endTime)
			.build()
		);

		//when
		Optional<ResourceCredit> byId = repository.findById(wrongId);

		//then
		assertThat(byId).isEmpty();
	}

	@Test
	void shouldFindAllResourceCredits() {
		//given
		entityRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId.id)
			.resourceTypeId(resourceTypeId.id)
			.name("name")
			.split(true)
			.amount(new BigDecimal(100))
			.createTime(createTime)
			.startTime(startTime)
			.endTime(endTime)
			.build()
		);
		entityRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId.id)
			.resourceTypeId(resourceTypeId.id)
			.name("name2")
			.split(false)
			.amount(new BigDecimal(455))
			.createTime(createTime2)
			.startTime(newStartTime)
			.endTime(newEndTime)
			.build()
		);

		//when
		Set<ResourceCredit> all = repository.findAll(siteId);

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void shouldFindAllResourceCreditsByNameAndExpired() {
		//given
		entityRepository.save(ResourceCreditEntity.builder()
				.siteId(siteId.id)
				.resourceTypeId(resourceTypeId.id)
				.name("test")
				.split(true)
				.amount(new BigDecimal(100))
				.createTime(createTime)
				.startTime(LocalDateTime.now().minusDays(1))
				.endTime(LocalDateTime.now().plusDays(1))
				.build());
		entityRepository.save(ResourceCreditEntity.builder()
				.siteId(siteId.id)
				.resourceTypeId(resourceTypeId.id)
				.name("testAsPreffix")
				.split(false)
				.amount(new BigDecimal(455))
				.createTime(createTime2)
				.startTime(LocalDateTime.now().minusDays(1))
				.endTime(LocalDateTime.now().plusDays(1))
				.build());
		entityRepository.save(ResourceCreditEntity.builder()
				.siteId(siteId.id)
				.resourceTypeId(resourceTypeId.id)
				.name("insideTextTestIs")
				.split(false)
				.amount(new BigDecimal(455))
				.createTime(createTime2)
				.startTime(LocalDateTime.now().minusDays(1))
				.endTime(LocalDateTime.now().minusSeconds(1))
				.build());

		//when
		Set<ResourceCredit> all = repository.findAllByNameOrSiteName("test");

		//then
		assertThat(all).hasSize(3);
	}

	@Test
	void shouldFindAllNotExpiredByResourceTypeId() {
		//given
		final LocalDateTime utcNow = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
		entityRepository.save(ResourceCreditEntity.builder()
				.siteId(siteId.id)
				.resourceTypeId(resourceTypeId.id)
				.name("not-expired-1")
				.split(true)
				.amount(new BigDecimal(100))
				.createTime(createTime)
				.startTime(utcNow.minusDays(1))
				.endTime(utcNow.plusDays(1))
				.build());
		entityRepository.save(ResourceCreditEntity.builder()
				.siteId(siteId.id)
				.resourceTypeId(resourceTypeId.id)
				.name("not-expired-2")
				.split(false)
				.amount(new BigDecimal(455))
				.createTime(createTime2)
				.startTime(utcNow.minusDays(1))
				.endTime(utcNow.plusDays(1))
				.build());
		entityRepository.save(ResourceCreditEntity.builder()
				.siteId(siteId.id)
				.resourceTypeId(resourceTypeId.id)
				.name("expired-1")
				.split(false)
				.amount(new BigDecimal(455))
				.createTime(createTime2)
				.startTime(utcNow.minusDays(1))
				.endTime(utcNow.minusSeconds(1))
				.build());

		//when
		Set<ResourceCredit> all = repository.findAllNotExpiredByResourceTypeId(resourceTypeId);

		//then
		assertThat(all).hasSize(2);
		assertTrue(all.stream().noneMatch(credit -> credit.name.equals("expired-1")));
	}

	@Test
	void shouldFindAllByCreditNameAndNonExpired() {
		//given
		final LocalDateTime utcNow = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
		entityRepository.save(ResourceCreditEntity.builder()
				.siteId(siteId.id)
				.resourceTypeId(resourceTypeId.id)
				.name("test")
				.split(true)
				.amount(new BigDecimal(100))
				.createTime(createTime)
				.startTime(utcNow.minusDays(1))
				.endTime(utcNow.plusDays(1))
				.build());
		entityRepository.save(ResourceCreditEntity.builder()
				.siteId(siteId.id)
				.resourceTypeId(resourceTypeId.id)
				.name("testAsPreffix")
				.split(false)
				.amount(new BigDecimal(455))
				.createTime(createTime2)
				.startTime(utcNow.minusDays(1))
				.endTime(utcNow.plusDays(1))
				.build());
		entityRepository.save(ResourceCreditEntity.builder()
				.siteId(siteId.id)
				.resourceTypeId(resourceTypeId.id)
				.name("expiredButWithtest")
				.split(false)
				.amount(new BigDecimal(455))
				.createTime(createTime2)
				.startTime(utcNow.minusDays(1))
				.endTime(utcNow.minusSeconds(1))
				.build());

		//when
		Set<ResourceCredit> all = repository.findAllNotExpiredByNameOrSiteName("test");

		//then
		assertThat(all).hasSize(2);
		assertTrue(all.stream().noneMatch(credit -> credit.name.equals("expiredButWithtest")));
	}

	@Test
	void shouldCreateResourceCredit() {
		//given
		ResourceCredit request = ResourceCredit.builder()
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
			.name("name")
			.splittable(true)
			.amount(new BigDecimal(100))
			.utcCreateTime(createTime)
			.utcStartTime(startTime)
			.utcEndTime(endTime)
			.build();

		//when
		ResourceCreditId newResourceCreditId = repository.create(request);

		//then
		Optional<ResourceCredit> byId = repository.findById(newResourceCreditId);
		assertThat(byId).isPresent();
		assertThat(byId.get().id).isNotNull();
		assertThat(byId.get().name).isEqualTo("name");
		assertThat(byId.get().splittable).isEqualTo(true);
		assertThat(byId.get().amount.compareTo(new BigDecimal(100))).isEqualTo(0);
		assertThat(byId.get().utcCreateTime).isEqualTo(createTime);
		assertThat(byId.get().utcStartTime).isEqualTo(startTime);
		assertThat(byId.get().utcEndTime).isEqualTo(endTime);
	}

	@Test
	void shouldUpdateResourceCredit() {
		//given
		ResourceCreditEntity old = entityRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId.id)
			.resourceTypeId(resourceTypeId.id)
			.name("name")
			.split(true)
			.amount(new BigDecimal(100))
			.createTime(createTime)
			.startTime(startTime)
			.endTime(endTime)
			.build()
		);
		ResourceCredit requestToUpdate = ResourceCredit.builder()
			.id(old.getId().toString())
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
			.name("new_name")
			.splittable(true)
			.amount(new BigDecimal(434))
			.utcCreateTime(createTime2)
			.utcStartTime(newStartTime)
			.utcEndTime(newEndTime)
			.build();

		//when
		repository.update(requestToUpdate);

		//then
		Optional<ResourceCredit> byId = repository.findById(new ResourceCreditId(old.getId()));
		assertThat(byId).isPresent();
		assertThat(byId.get().name).isEqualTo("new_name");
		assertThat(byId.get().splittable).isEqualTo(true);
		assertThat(byId.get().amount).isEqualTo(new BigDecimal(434));
		assertThat(byId.get().utcCreateTime).isEqualTo(createTime2);
		assertThat(byId.get().utcStartTime).isEqualTo(newStartTime);
		assertThat(byId.get().utcEndTime).isEqualTo(newEndTime);
	}

	@Test
	void savedResourceCreditExists() {
		//given
		ResourceCreditEntity entity = entityRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId.id)
			.resourceTypeId(resourceTypeId.id)
			.name("new_name")
			.split(true)
			.amount(new BigDecimal(434))
			.createTime(createTime2)
			.startTime(newStartTime)
			.endTime(newEndTime)
			.build()
		);

		//when + then
		assertThat(repository.exists(new ResourceCreditId(entity.getId()))).isTrue();
	}

	@Test
	void shouldNotExistsDueToEmptyOrWrongId() {
		//given
		ResourceCreditId nonExistedId = new ResourceCreditId(generateId());

		//when + then
		assertThat(repository.exists(nonExistedId)).isFalse();
		assertThat(repository.exists(null)).isFalse();
		assertThat(repository.exists(new ResourceCreditId((UUID) null))).isFalse();
	}

	@Test
	void shouldReturnFalseForUniqueName() {
		//given
		entityRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId.id)
			.resourceTypeId(resourceTypeId.id)
			.name("new_name")
			.split(true)
			.amount(new BigDecimal(434))
			.createTime(createTime2)
			.startTime(newStartTime)
			.endTime(newEndTime)
			.build()
		);
		String uniqueName = "unique_name";

		//when + then
		assertThat(repository.isNamePresent(uniqueName, siteId)).isFalse();
	}

	@Test
	void shouldReturnTrueForPresentNameInOtherSite() {
		//given
		entityRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId.id)
			.resourceTypeId(resourceTypeId.id)
			.name("new_name")
			.split(true)
			.amount(new BigDecimal(434))
			.createTime(createTime2)
			.startTime(newStartTime)
			.endTime(newEndTime)
			.build()
		);

		//when + then
		assertThat(repository.isNamePresent("new_name", new SiteId(UUID.randomUUID()))).isFalse();
	}
	
	@Test
	void shouldReturnTrueForPresentName() {
		//given
		ResourceCreditEntity existedResourceCredit = entityRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId.id)
			.resourceTypeId(resourceTypeId.id)
			.name("new_name")
			.split(true)
			.amount(new BigDecimal(434))
			.createTime(createTime2)
			.startTime(newStartTime)
			.endTime(newEndTime)
			.build());

		//when + then
		assertThat(repository.isNamePresent(existedResourceCredit.name, siteId)).isTrue();
	}

	@Test
	void shouldReturnTrueForExistingResourceTypeId() {
		//given
		ResourceCreditEntity existedResourceCredit = entityRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId.id)
			.resourceTypeId(resourceTypeId.id)
			.name("new_name")
			.split(true)
			.amount(new BigDecimal(434))
			.createTime(createTime2)
			.startTime(newStartTime)
			.endTime(newEndTime)
			.build());

		//when + then
		assertThat(repository.existsByResourceTypeId(new ResourceTypeId(existedResourceCredit.resourceTypeId))).isTrue();
	}

	@Test
	void shouldReturnTrueForExistingInResourceTypeId() {
		//given
		ResourceCreditEntity existedResourceCredit = entityRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId.id)
			.resourceTypeId(resourceTypeId.id)
			.name("new_name")
			.split(true)
			.amount(new BigDecimal(434))
			.createTime(createTime2)
			.startTime(newStartTime)
			.endTime(newEndTime)
			.build());

		//when + then
		assertThat(repository.existsByResourceTypeIdIn(List.of(new ResourceTypeId(existedResourceCredit.resourceTypeId)))).isTrue();
	}

	@Test
	void shouldReturnFalseForNonExistingResourceTypeId() {
		//given
		entityRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId.id)
			.resourceTypeId(resourceTypeId.id)
			.name("new_name")
			.split(true)
			.amount(new BigDecimal(434))
			.createTime(createTime2)
			.startTime(newStartTime)
			.endTime(newEndTime)
			.build());

		//when + then
		assertThat(repository.existsByResourceTypeId(resourceTypeId2)).isFalse();
	}

	@Test
	void shouldReturnFalseForNonExistingInResourceTypeId() {
		//given
		entityRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId.id)
			.resourceTypeId(resourceTypeId.id)
			.name("new_name")
			.split(true)
			.amount(new BigDecimal(434))
			.createTime(createTime2)
			.startTime(newStartTime)
			.endTime(newEndTime)
			.build());

		//when + then
		assertThat(repository.existsByResourceTypeIdIn(List.of(resourceTypeId2))).isFalse();
	}
}