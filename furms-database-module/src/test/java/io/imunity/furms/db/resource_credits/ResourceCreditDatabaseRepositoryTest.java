/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_credits;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceMeasureType;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.services.InfraServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.db.id.uuid.UUIDIdUtils.generateId;
import static org.assertj.core.api.Assertions.assertThat;

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

	private UUID siteId;
	private UUID siteId2;

	private UUID resourceTypeId;
	private UUID resourceTypeId2;

	private LocalDateTime startTime = LocalDateTime.of(2020, 5, 20, 5, 12, 16);
	private LocalDateTime endTime = LocalDateTime.of(2021, 6, 21, 4, 18, 4);
	private LocalDateTime newStartTime = LocalDateTime.of(2020, 8, 3, 4, 7, 5);
	private LocalDateTime newEndTime = LocalDateTime.of(2021, 9, 13, 3, 35, 33);
	private LocalDateTime createTime = LocalDateTime.of(2020, 1, 30, 5, 8, 8);
	private LocalDateTime createTime2 = LocalDateTime.of(2021, 8, 23, 8, 18, 18);

	@BeforeEach
	void init() {
		Site site = Site.builder()
			.name("name")
			.build();
		Site site1 = Site.builder()
			.name("name2")
			.build();
		siteId = UUID.fromString(siteRepository.create(site, new SiteExternalId("id")));
		siteId2 = UUID.fromString(siteRepository.create(site1, new SiteExternalId("id2")));

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
	}

	@Test
	void shouldFindCreatedService() {
		//given
		ResourceCreditEntity entity = entityRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
			.name("name")
			.split(true)
			.access(true)
			.amount(new BigDecimal(100))
			.createTime(createTime)
			.startTime(startTime)
			.endTime(endTime)
			.build()
		);

		//when
		Optional<ResourceCredit> byId = repository.findById(entity.getId().toString());

		//then
		assertThat(byId).isPresent();
		ResourceCredit project = byId.get();
		assertThat(project.id).isEqualTo(entity.getId().toString());
		assertThat(project.name).isEqualTo(entity.name);
		assertThat(byId.get().split).isEqualTo(true);
		assertThat(byId.get().access).isEqualTo(true);
		assertThat(byId.get().amount).isEqualTo(new BigDecimal(100));
		assertThat(byId.get().utcCreateTime).isEqualTo(createTime);
		assertThat(byId.get().utcStartTime).isEqualTo(startTime);
		assertThat(byId.get().utcEndTime).isEqualTo(endTime);
	}

	@Test
	void shouldNotFindByIdIfDoesntExist() {
		//given
		UUID wrongId = generateId();
		entityRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
			.name("name")
			.split(true)
			.access(true)
			.amount(new BigDecimal(100))
			.createTime(createTime)
			.startTime(startTime)
			.endTime(endTime)
			.build()
		);

		//when
		Optional<ResourceCredit> byId = repository.findById(wrongId.toString());

		//then
		assertThat(byId).isEmpty();
	}

	@Test
	void shouldFindAllResourceCredits() {
		//given
		entityRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
			.name("name")
			.split(true)
			.access(true)
			.amount(new BigDecimal(100))
			.createTime(createTime)
			.startTime(startTime)
			.endTime(endTime)
			.build()
		);
		entityRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
			.name("name2")
			.split(false)
			.access(false)
			.amount(new BigDecimal(455))
			.createTime(createTime2)
			.startTime(newStartTime)
			.endTime(newEndTime)
			.build()
		);

		//when
		Set<ResourceCredit> all = repository.findAll(siteId.toString());

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void shouldFindAllResourceCreditsByNameAndExpired() {
		//given
		entityRepository.save(ResourceCreditEntity.builder()
				.siteId(siteId)
				.resourceTypeId(resourceTypeId)
				.name("test")
				.split(true)
				.access(true)
				.amount(new BigDecimal(100))
				.createTime(createTime)
				.startTime(LocalDateTime.now().minusDays(1))
				.endTime(LocalDateTime.now().plusDays(1))
				.build());
		entityRepository.save(ResourceCreditEntity.builder()
				.siteId(siteId)
				.resourceTypeId(resourceTypeId)
				.name("testAsPreffix")
				.split(false)
				.access(false)
				.amount(new BigDecimal(455))
				.createTime(createTime2)
				.startTime(LocalDateTime.now().minusDays(1))
				.endTime(LocalDateTime.now().plusDays(1))
				.build());
		entityRepository.save(ResourceCreditEntity.builder()
				.siteId(siteId)
				.resourceTypeId(resourceTypeId)
				.name("insideTextTestIs")
				.split(false)
				.access(false)
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
	void shouldCreateResourceCredit() {
		//given
		ResourceCredit request = ResourceCredit.builder()
			.siteId(siteId.toString())
			.resourceTypeId(resourceTypeId.toString())
			.name("name")
			.split(true)
			.access(true)
			.amount(new BigDecimal(100))
			.utcCreateTime(createTime)
			.utcStartTime(startTime)
			.utcEndTime(endTime)
			.build();

		//when
		String newResourceCreditId = repository.create(request);

		//then
		Optional<ResourceCredit> byId = repository.findById(newResourceCreditId);
		assertThat(byId).isPresent();
		assertThat(byId.get().id).isNotNull();
		assertThat(byId.get().name).isEqualTo("name");
		assertThat(byId.get().split).isEqualTo(true);
		assertThat(byId.get().access).isEqualTo(true);
		assertThat(byId.get().amount).isEqualTo(new BigDecimal(100));
		assertThat(byId.get().utcCreateTime).isEqualTo(createTime);
		assertThat(byId.get().utcStartTime).isEqualTo(startTime);
		assertThat(byId.get().utcEndTime).isEqualTo(endTime);
	}

	@Test
	void shouldUpdateResourceCredit() {
		//given
		ResourceCreditEntity old = entityRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
			.name("name")
			.split(true)
			.access(true)
			.amount(new BigDecimal(100))
			.createTime(createTime)
			.startTime(startTime)
			.endTime(endTime)
			.build()
		);
		ResourceCredit requestToUpdate = ResourceCredit.builder()
			.id(old.getId().toString())
			.siteId(siteId.toString())
			.resourceTypeId(resourceTypeId.toString())
			.name("new_name")
			.split(true)
			.access(false)
			.amount(new BigDecimal(434))
			.utcCreateTime(createTime2)
			.utcStartTime(newStartTime)
			.utcEndTime(newEndTime)
			.build();

		//when
		repository.update(requestToUpdate);

		//then
		Optional<ResourceCredit> byId = repository.findById(old.getId().toString());
		assertThat(byId).isPresent();
		assertThat(byId.get().name).isEqualTo("new_name");
		assertThat(byId.get().split).isEqualTo(true);
		assertThat(byId.get().access).isEqualTo(false);
		assertThat(byId.get().amount).isEqualTo(new BigDecimal(434));
		assertThat(byId.get().utcCreateTime).isEqualTo(createTime2);
		assertThat(byId.get().utcStartTime).isEqualTo(newStartTime);
		assertThat(byId.get().utcEndTime).isEqualTo(newEndTime);
	}

	@Test
	void savedResourceCreditExists() {
		//given
		ResourceCreditEntity entity = entityRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
			.name("new_name")
			.split(true)
			.access(false)
			.amount(new BigDecimal(434))
			.createTime(createTime2)
			.startTime(newStartTime)
			.endTime(newEndTime)
			.build()
		);

		//when + then
		assertThat(repository.exists(entity.getId().toString())).isTrue();
	}

	@Test
	void shouldNotExistsDueToEmptyOrWrongId() {
		//given
		String nonExistedId = generateId().toString();

		//when + then
		assertThat(repository.exists(nonExistedId)).isFalse();
		assertThat(repository.exists(null)).isFalse();
		assertThat(repository.exists("")).isFalse();
	}

	@Test
	void shouldReturnTrueForUniqueName() {
		//given
		entityRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
			.name("new_name")
			.split(true)
			.access(false)
			.amount(new BigDecimal(434))
			.createTime(createTime2)
			.startTime(newStartTime)
			.endTime(newEndTime)
			.build()
		);
		String uniqueName = "unique_name";

		//when + then
		assertThat(repository.isUniqueName(uniqueName)).isTrue();
	}

	@Test
	void shouldReturnFalseForNonUniqueName() {
		//given
		ResourceCreditEntity existedResourceCredit = entityRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
			.name("new_name")
			.split(true)
			.access(false)
			.amount(new BigDecimal(434))
			.createTime(createTime2)
			.startTime(newStartTime)
			.endTime(newEndTime)
			.build());

		//when + then
		assertThat(repository.isUniqueName(existedResourceCredit.name)).isFalse();
	}

	@Test
	void shouldReturnTrueForExistingResourceTypeId() {
		//given
		ResourceCreditEntity existedResourceCredit = entityRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
			.name("new_name")
			.split(true)
			.access(false)
			.amount(new BigDecimal(434))
			.createTime(createTime2)
			.startTime(newStartTime)
			.endTime(newEndTime)
			.build());

		//when + then
		assertThat(repository.existsByResourceTypeId(existedResourceCredit.resourceTypeId.toString())).isTrue();
	}

	@Test
	void shouldReturnTrueForExistingInResourceTypeId() {
		//given
		ResourceCreditEntity existedResourceCredit = entityRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
			.name("new_name")
			.split(true)
			.access(false)
			.amount(new BigDecimal(434))
			.createTime(createTime2)
			.startTime(newStartTime)
			.endTime(newEndTime)
			.build());

		//when + then
		assertThat(repository.existsByResourceTypeIdIn(List.of(existedResourceCredit.resourceTypeId.toString()))).isTrue();
	}

	@Test
	void shouldReturnFalseForNonExistingResourceTypeId() {
		//given
		entityRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
			.name("new_name")
			.split(true)
			.access(false)
			.amount(new BigDecimal(434))
			.createTime(createTime2)
			.startTime(newStartTime)
			.endTime(newEndTime)
			.build());

		//when + then
		assertThat(repository.existsByResourceTypeId(resourceTypeId2.toString())).isFalse();
	}

	@Test
	void shouldReturnFalseForNonExistingInResourceTypeId() {
		//given
		entityRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
			.name("new_name")
			.split(true)
			.access(false)
			.amount(new BigDecimal(434))
			.createTime(createTime2)
			.startTime(newStartTime)
			.endTime(newEndTime)
			.build());

		//when + then
		assertThat(repository.existsByResourceTypeIdIn(List.of(resourceTypeId2.toString()))).isFalse();
	}
}