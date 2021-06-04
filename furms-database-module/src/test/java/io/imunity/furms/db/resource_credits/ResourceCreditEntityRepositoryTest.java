/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_credits;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.resource_types.ResourceMeasureType;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceType;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.db.id.uuid.UUIDIdUtils.generateId;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
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
	void init() throws IOException {
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
			.unit(ResourceMeasureUnit.KILO)
			.build();
		ResourceType resourceType2 = ResourceType.builder()
			.siteId(siteId2.toString())
			.serviceId(serviceId2.toString())
			.name("name2")
			.type(ResourceMeasureType.DATA)
			.unit(ResourceMeasureUnit.MB)
			.build();

		resourceTypeId = UUID.fromString(resourceTypeRepository.create(resourceType));
		resourceTypeId2 = UUID.fromString(resourceTypeRepository.create(resourceType2));
	}

	@Test
	void shouldCreateResourceType() {
		//given
		ResourceCreditEntity entityToSave = ResourceCreditEntity.builder()
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
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
		assertThat(byId.get().siteId).isEqualTo(siteId);
		assertThat(byId.get().resourceTypeId).isEqualTo(resourceTypeId);
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
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
			.name("name")
			.split(true)
			.amount(new BigDecimal(100))
			.createTime(createTime)
			.startTime(startTime)
			.endTime(endTime)
			.build();
		resourceCreditRepository.save(old);
		ResourceCreditEntity toUpdate = ResourceCreditEntity.builder()
			.siteId(siteId)
			.resourceTypeId(resourceTypeId2)
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
		assertThat(byId.get().siteId).isEqualTo(siteId);
		assertThat(byId.get().resourceTypeId).isEqualTo(resourceTypeId2);
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
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
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
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
			.name("name")
			.split(true)
			.amount(new BigDecimal(100))
			.createTime(createTime)
			.startTime(startTime)
			.endTime(endTime)
			.build()
		);
		resourceCreditRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
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
	void shouldFindAllBySiteNameAndNonExpired() {
		//given
		final ResourceCreditEntity nonExpired = resourceCreditRepository.save(ResourceCreditEntity.builder()
				.siteId(siteId2)
				.resourceTypeId(resourceTypeId)
				.name("other1")
				.split(true)
				.amount(new BigDecimal(100))
				.createTime(createTime)
				.startTime(LocalDateTime.now().minusSeconds(10))
				.endTime(LocalDateTime.now().plusDays(10))
				.build());
		resourceCreditRepository.save(ResourceCreditEntity.builder()
				.siteId(siteId2)
				.resourceTypeId(resourceTypeId)
				.name("other2")
				.split(true)
				.amount(new BigDecimal(342))
				.createTime(createTime2)
				.startTime(LocalDateTime.now().minusSeconds(10))
				.endTime(LocalDateTime.now().minusSeconds(1))
				.build());
		resourceCreditRepository.save(ResourceCreditEntity.builder()
				.siteId(siteId)
				.resourceTypeId(resourceTypeId)
				.name("other3")
				.split(true)
				.amount(new BigDecimal(342))
				.createTime(createTime2)
				.startTime(LocalDateTime.now().minusSeconds(10))
				.endTime(LocalDateTime.now().minusSeconds(1))
				.build());

		//when
		final List<ResourceCreditEntity> all = resourceCreditRepository.findAllByNameOrSiteNameWithoutExpired("name2")
													.collect(toList());

		//then
		assertThat(all).hasSize(1);
		final ResourceCreditEntity foundEntity = all.get(0);
		assertThat(foundEntity.getId()).isEqualTo(nonExpired.getId());
	}

	@Test
	void shouldFindAllBySiteNameAndIncludeExpired() {
		//given
		resourceCreditRepository.save(ResourceCreditEntity.builder()
				.siteId(siteId2)
				.resourceTypeId(resourceTypeId)
				.name("other1")
				.split(true)
				.amount(new BigDecimal(100))
				.createTime(createTime)
				.startTime(LocalDateTime.now().minusSeconds(10))
				.endTime(LocalDateTime.now().plusDays(10))
				.build());
		resourceCreditRepository.save(ResourceCreditEntity.builder()
				.siteId(siteId2)
				.resourceTypeId(resourceTypeId)
				.name("other2")
				.split(true)
				.amount(new BigDecimal(342))
				.createTime(createTime2)
				.startTime(LocalDateTime.now().minusSeconds(10))
				.endTime(LocalDateTime.now().minusSeconds(1))
				.build());
		final ResourceCreditEntity otherSite = resourceCreditRepository.save(ResourceCreditEntity.builder()
				.siteId(siteId)
				.resourceTypeId(resourceTypeId)
				.name("other3")
				.split(true)
				.amount(new BigDecimal(342))
				.createTime(createTime2)
				.startTime(LocalDateTime.now().minusSeconds(10))
				.endTime(LocalDateTime.now().minusSeconds(1))
				.build());

		//when
		final Set<ResourceCreditEntity> all = resourceCreditRepository.findAllByNameOrSiteName("name2")
												.collect(toSet());

		//then
		assertThat(all).hasSize(2);
		assertThat(all).doesNotContain(otherSite);
	}

	@Test
	void shouldFindAllByCreditNameAndNonExpired() {
		//given
		final ResourceCreditEntity nonExpired = resourceCreditRepository.save(ResourceCreditEntity.builder()
				.siteId(siteId)
				.resourceTypeId(resourceTypeId)
				.name("other1")
				.split(true)
				.amount(new BigDecimal(100))
				.createTime(createTime)
				.startTime(LocalDateTime.now().minusSeconds(10))
				.endTime(LocalDateTime.now().plusDays(10))
				.build());
		resourceCreditRepository.save(ResourceCreditEntity.builder()
				.siteId(siteId)
				.resourceTypeId(resourceTypeId)
				.name("other2")
				.split(true)
				.amount(new BigDecimal(342))
				.createTime(createTime2)
				.startTime(LocalDateTime.now().minusSeconds(10))
				.endTime(LocalDateTime.now().minusSeconds(1))
				.build());

		//when
		final List<ResourceCreditEntity> all = resourceCreditRepository.findAllByNameOrSiteNameWithoutExpired("other")
													.collect(toList());

		//then
		assertThat(all).hasSize(1);
		final ResourceCreditEntity foundEntity = all.get(0);
		assertThat(foundEntity.getId()).isEqualTo(nonExpired.getId());
	}

	@Test
	void shouldFindAllByCreditNamePartAndIncludeExpired() {
		//given
		resourceCreditRepository.save(ResourceCreditEntity.builder()
				.siteId(siteId)
				.resourceTypeId(resourceTypeId)
				.name("other1")
				.split(true)
				.amount(new BigDecimal(100))
				.createTime(createTime)
				.startTime(LocalDateTime.now().minusSeconds(10))
				.endTime(LocalDateTime.now().plusDays(10))
				.build());
		resourceCreditRepository.save(ResourceCreditEntity.builder()
				.siteId(siteId)
				.resourceTypeId(resourceTypeId)
				.name("other2")
				.split(true)
				.amount(new BigDecimal(342))
				.createTime(createTime2)
				.startTime(LocalDateTime.now().minusSeconds(10))
				.endTime(LocalDateTime.now().minusSeconds(1))
				.build());
		final ResourceCreditEntity differentName = resourceCreditRepository.save(ResourceCreditEntity.builder()
				.siteId(siteId)
				.resourceTypeId(resourceTypeId)
				.name("different")
				.split(true)
				.amount(new BigDecimal(342))
				.createTime(createTime2)
				.startTime(LocalDateTime.now().minusSeconds(10))
				.endTime(LocalDateTime.now().minusSeconds(1))
				.build());

		//when
		final Set<ResourceCreditEntity> all = resourceCreditRepository.findAllByNameOrSiteName("ther")
												.collect(toSet());

		//then
		assertThat(all).hasSize(2);
		assertThat(all).doesNotContain(differentName);
	}

	@Test
	void savedServiceExistsByResourceCreditId() {
		//given
		ResourceCreditEntity service = resourceCreditRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
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
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
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
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
			.name("name")
			.split(true)
			.amount(new BigDecimal(100))
			.createTime(createTime)
			.startTime(startTime)
			.endTime(endTime)
			.build());

		//when
		boolean nonExists = resourceCreditRepository.existsByNameAndSiteId("wrong_name", siteId);

		//then
		assertThat(nonExists).isFalse();
	}

	@Test
	void shouldDeleteService() {
		//given
		ResourceCreditEntity entityToRemove = resourceCreditRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
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
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
			.name("name")
			.split(true)
			.amount(new BigDecimal(100))
			.createTime(createTime)
			.startTime(startTime)
			.endTime(endTime)
			.build());
		resourceCreditRepository.save(ResourceCreditEntity.builder()
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
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