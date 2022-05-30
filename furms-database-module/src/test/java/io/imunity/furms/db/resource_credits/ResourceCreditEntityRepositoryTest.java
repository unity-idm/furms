/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_credits;


import io.imunity.furms.db.DBIntegrationTest;
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
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.db.id.uuid.UUIDIdUtils.generateId;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ResourceCreditEntityRepositoryTest extends DBIntegrationTest {

	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private InfraServiceRepository infraServiceRepository;
	@Autowired
	private ResourceTypeRepository resourceTypeRepository;
	@Autowired
	private ResourceCreditEntityRepository resourceCreditRepository;

	private SiteId siteId;
	private SiteId siteId2;

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
		siteId2 = siteRepository.create(site1, new SiteExternalId("id2"));

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
			.unit(ResourceMeasureUnit.KILO)
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
	void shouldCreateResourceType() {
		//given
		ResourceCreditEntity entityToSave = ResourceCreditEntity.builder()
			.siteId(siteId.id)
			.resourceTypeId(resourceTypeId.id)
			.name("name")
			.split(true)
			.amount(new BigDecimal(100))
			.createTime(createTime)
			.startTime(startTime)
			.endTime(endTime)
			.build();

		//when
		ResourceCreditEntity saved = resourceCreditRepository.save(entityToSave);

		//then
		assertThat(resourceCreditRepository.findAll()).hasSize(1);
		Optional<ResourceCreditEntity> byId = resourceCreditRepository.findById(saved.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().siteId).isEqualTo(siteId.id);
		assertThat(byId.get().resourceTypeId).isEqualTo(resourceTypeId.id);
		assertThat(byId.get().name).isEqualTo("name");
		assertThat(byId.get().split).isEqualTo(true);
		assertThat(byId.get().amount).isEqualTo(new BigDecimal(100));
		assertThat(byId.get().createTime).isEqualTo(createTime);
		assertThat(byId.get().startTime).isEqualTo(startTime);
		assertThat(byId.get().endTime).isEqualTo(endTime);
	}

	@Test
	void shouldUpdateResourceCredit() {
		//given
		ResourceCreditEntity old = ResourceCreditEntity.builder()
			.siteId(siteId.id)
			.resourceTypeId(resourceTypeId.id)
			.name("name")
			.split(true)
			.amount(new BigDecimal(100))
			.createTime(createTime)
			.startTime(startTime)
			.endTime(endTime)
			.build();
		resourceCreditRepository.save(old);
		ResourceCreditEntity toUpdate = ResourceCreditEntity.builder()
			.siteId(siteId.id)
			.resourceTypeId(resourceTypeId2.id)
			.name("name2")
			.split(false)
			.amount(new BigDecimal(111))
			.createTime(createTime2)
			.startTime(newStartTime)
			.endTime(newEndTime)
			.build();

		//when
		resourceCreditRepository.save(toUpdate);

		//then
		Optional<ResourceCreditEntity> byId = resourceCreditRepository.findById(toUpdate.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().siteId).isEqualTo(siteId.id);
		assertThat(byId.get().resourceTypeId).isEqualTo(resourceTypeId2.id);
		assertThat(byId.get().name).isEqualTo("name2");
		assertThat(byId.get().split).isEqualTo(false);
		assertThat(byId.get().amount).isEqualTo(new BigDecimal(111));
		assertThat(byId.get().createTime).isEqualTo(createTime2);
		assertThat(byId.get().startTime).isEqualTo(newStartTime);
		assertThat(byId.get().endTime).isEqualTo(newEndTime);
	}

	@Test
	void shouldFindCreatedResourceCredits() {
		//given
		ResourceCreditEntity toFind = ResourceCreditEntity.builder()
			.siteId(siteId.id)
			.resourceTypeId(resourceTypeId.id)
			.name("name")
			.split(true)
			.amount(new BigDecimal(100))
			.createTime(createTime)
			.startTime(startTime)
			.endTime(endTime)
			.build();
		resourceCreditRepository.save(toFind);

		//when
		Optional<ResourceCreditEntity> byId = resourceCreditRepository.findById(toFind.getId());

		//then
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindAllAvailableResourceCredits() {
		//given
		resourceCreditRepository.save(ResourceCreditEntity.builder()
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
		resourceCreditRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId.id)
			.resourceTypeId(resourceTypeId.id)
			.name("name1")
			.split(true)
			.amount(new BigDecimal(342))
			.createTime(createTime2)
			.startTime(newStartTime)
			.endTime(newEndTime)
			.build()
		);

		//when
		Iterable<ResourceCreditEntity> all = resourceCreditRepository.findAll();

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void shouldFindAllBySiteNameAndIncludeExpired() {
		//given
		resourceCreditRepository.save(ResourceCreditEntity.builder()
				.siteId(siteId2.id)
				.resourceTypeId(resourceTypeId.id)
				.name("other1")
				.split(true)
				.amount(new BigDecimal(100))
				.createTime(createTime)
				.startTime(LocalDateTime.now().minusSeconds(10))
				.endTime(LocalDateTime.now().plusDays(10))
				.build());
		resourceCreditRepository.save(ResourceCreditEntity.builder()
				.siteId(siteId2.id)
				.resourceTypeId(resourceTypeId.id)
				.name("other2")
				.split(true)
				.amount(new BigDecimal(342))
				.createTime(createTime2)
				.startTime(LocalDateTime.now().minusSeconds(10))
				.endTime(LocalDateTime.now().minusSeconds(1))
				.build());
		final ResourceCreditEntity otherSite = resourceCreditRepository.save(ResourceCreditEntity.builder()
				.siteId(siteId.id)
				.resourceTypeId(resourceTypeId.id)
				.name("other3")
				.split(true)
				.amount(new BigDecimal(342))
				.createTime(createTime2)
				.startTime(LocalDateTime.now().minusSeconds(10))
				.endTime(LocalDateTime.now().minusSeconds(1))
				.build());

		//when
		final Set<ResourceCreditEntity> all = resourceCreditRepository.findAllByNameOrSiteName("name2");

		//then
		assertThat(all).hasSize(2);
		assertThat(all).doesNotContain(otherSite);
	}

	@Test
	void shouldFindAllByCreditNamePartAndIncludeExpired() {
		//given
		final LocalDateTime utcNow = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
		resourceCreditRepository.save(ResourceCreditEntity.builder()
				.siteId(siteId.id)
				.resourceTypeId(resourceTypeId.id)
				.name("other1")
				.split(true)
				.amount(new BigDecimal(100))
				.createTime(createTime)
				.startTime(utcNow.minusSeconds(10))
				.endTime(utcNow.plusDays(10))
				.build());
		resourceCreditRepository.save(ResourceCreditEntity.builder()
				.siteId(siteId.id)
				.resourceTypeId(resourceTypeId.id)
				.name("other2")
				.split(true)
				.amount(new BigDecimal(342))
				.createTime(createTime2)
				.startTime(utcNow.minusSeconds(10))
				.endTime(utcNow.minusSeconds(1))
				.build());
		final ResourceCreditEntity differentName = resourceCreditRepository.save(ResourceCreditEntity.builder()
				.siteId(siteId.id)
				.resourceTypeId(resourceTypeId.id)
				.name("different")
				.split(true)
				.amount(new BigDecimal(342))
				.createTime(createTime2)
				.startTime(utcNow.minusSeconds(10))
				.endTime(utcNow.minusSeconds(1))
				.build());

		//when
		final Set<ResourceCreditEntity> all = resourceCreditRepository.findAllByNameOrSiteName("ther");

		//then
		assertThat(all).hasSize(2);
		assertThat(all).doesNotContain(differentName);
	}

	@Test
	void savedServiceExistsByResourceCreditId() {
		//given
		ResourceCreditEntity service = resourceCreditRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId.id)
			.resourceTypeId(resourceTypeId.id)
			.name("name")
			.split(true)
			.amount(new BigDecimal(100))
			.createTime(createTime)
			.startTime(startTime)
			.endTime(endTime)
			.build());

		//when + then
		assertThat(resourceCreditRepository.existsById(service.getId())).isTrue();
		assertThat(resourceCreditRepository.existsById(generateId())).isFalse();
	}

	@Test
	void savedResourceCreditExistsByName() {
		//given
		ResourceCreditEntity service = resourceCreditRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId.id)
			.resourceTypeId(resourceTypeId.id)
			.name("name")
			.split(true)
			.amount(new BigDecimal(100))
			.createTime(createTime)
			.startTime(startTime)
			.endTime(endTime)
			.build());

		//when
		boolean exists = resourceCreditRepository.existsByNameAndSiteId(service.name, service.siteId);

		//then
		assertThat(exists).isTrue();
	}

	@Test
	void savedResourceCreditDoesNotExistByName() {
		//given
		resourceCreditRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId.id)
			.resourceTypeId(resourceTypeId.id)
			.name("name")
			.split(true)
			.amount(new BigDecimal(100))
			.createTime(createTime)
			.startTime(startTime)
			.endTime(endTime)
			.build());

		//when
		boolean nonExists = resourceCreditRepository.existsByNameAndSiteId("wrong_name", siteId.id);

		//then
		assertThat(nonExists).isFalse();
	}

	@Test
	void shouldDeleteService() {
		//given
		ResourceCreditEntity entityToRemove = resourceCreditRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId.id)
			.resourceTypeId(resourceTypeId.id)
			.name("name")
			.split(true)
			.amount(new BigDecimal(100))
			.createTime(createTime)
			.startTime(startTime)
			.endTime(endTime)
			.build());

		//when
		resourceCreditRepository.deleteById(entityToRemove.getId());

		//then
		assertThat(resourceCreditRepository.findAll()).hasSize(0);
	}

	@Test
	void shouldDeleteAllServices() {
		//given
		resourceCreditRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId.id)
			.resourceTypeId(resourceTypeId.id)
			.name("name")
			.split(true)
			.amount(new BigDecimal(100))
			.createTime(createTime)
			.startTime(startTime)
			.endTime(endTime)
			.build());
		resourceCreditRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId.id)
			.resourceTypeId(resourceTypeId.id)
			.name("name1")
			.split(false)
			.amount(new BigDecimal(5345))
			.createTime(createTime2)
			.startTime(newStartTime)
			.endTime(endTime)
			.build());

		//when
		resourceCreditRepository.deleteAll();

		//then
		assertThat(resourceCreditRepository.findAll()).hasSize(0);
	}

}