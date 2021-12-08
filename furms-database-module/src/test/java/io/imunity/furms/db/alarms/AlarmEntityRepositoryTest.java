/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.alarms;


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
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import io.imunity.furms.spi.services.InfraServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.AfterEach;
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

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AlarmEntityRepositoryTest extends DBIntegrationTest {

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
	private ResourceUsageRepository resourceUsageRepository;

	@Autowired
	private AlarmEntityRepository alarmEntityRepository;

	private UUID siteId;

	private UUID projectId;
	private UUID projectId2;

	private UUID projectAllocationId;
	private UUID projectAllocationId1;
	private UUID projectAllocationId2;

	@BeforeEach
	void init() throws IOException {
		Site site = Site.builder()
			.name("name")
			.build();
		Site site1 = Site.builder()
			.name("name2")
			.build();
		siteId = UUID.fromString(siteRepository.create(site, new SiteExternalId("id")));
		siteRepository.create(site1, new SiteExternalId("id2"));

		Community community = Community.builder()
			.name("name")
			.logo(FurmsImage.empty())
			.build();
		Community community2 = Community.builder()
			.name("name1")
			.logo(FurmsImage.empty())
			.build();
		UUID communityId = UUID.fromString(communityRepository.create(community));
		UUID communityId2 = UUID.fromString(communityRepository.create(community2));

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
		Project project2 = Project.builder()
			.communityId(communityId2.toString())
			.name("name2")
			.logo(FurmsImage.empty())
			.description("new_description")
			.acronym("acronym")
			.researchField("research filed")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();

		projectId = UUID.fromString(projectRepository.create(project));
		projectId2 = UUID.fromString(projectRepository.create(project2));

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
		UUID communityAllocationId2 = UUID.fromString(communityAllocationRepository.create(
			CommunityAllocation.builder()
				.communityId(communityId.toString())
				.resourceCreditId(resourceCreditId.toString())
				.name("anem2")
				.amount(new BigDecimal(30))
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
		projectAllocationId1 = UUID.fromString(projectAllocationRepository.create(
			ProjectAllocation.builder()
				.projectId(projectId.toString())
				.communityAllocationId(communityAllocationId2.toString())
				.name("name1")
				.amount(new BigDecimal(20))
				.build()
		));
		projectAllocationId2 = UUID.fromString(projectAllocationRepository.create(
			ProjectAllocation.builder()
				.projectId(projectId2.toString())
				.communityAllocationId(communityAllocationId2.toString())
				.name("anem2")
				.amount(new BigDecimal(30))
				.build()
		));
	}

	@AfterEach
	void clean(){
		alarmEntityRepository.deleteAll();
	}

	@Test
	void shouldCreate() {
		AlarmEntity alarmEntity = AlarmEntity.builder()
			.projectId(projectId)
			.projectAllocationId(projectAllocationId)
			.name("name")
			.threshold(50)
			.allUsers(false)
			.alarmUserEntities(Set.of(new AlarmUserEntity("userId1"), new AlarmUserEntity("userId2")))
			.build();

		AlarmEntity saved = alarmEntityRepository.save(alarmEntity);

		Optional<AlarmEntity> alarm = alarmEntityRepository.findById(saved.getId());
		assertThat(alarm).isPresent();
		assertThat(alarm.get()).isEqualTo(saved);
	}

	@Test
	void shouldFindByAllocationId() {
		AlarmEntity alarmEntity = AlarmEntity.builder()
			.projectId(projectId)
			.projectAllocationId(projectAllocationId)
			.name("name")
			.threshold(50)
			.allUsers(false)
			.alarmUserEntities(Set.of(new AlarmUserEntity("userId1"), new AlarmUserEntity("userId2")))
			.build();

		AlarmEntity saved = alarmEntityRepository.save(alarmEntity);

		Optional<AlarmEntity> alarm = alarmEntityRepository.findByProjectAllocationId(projectAllocationId);
		assertThat(alarm).isPresent();
		assertThat(alarm.get()).isEqualTo(saved);
	}

	@Test
	void shouldExistByIdAndProjectId() {
		AlarmEntity alarmEntity = AlarmEntity.builder()
			.projectId(projectId)
			.projectAllocationId(projectAllocationId)
			.name("name")
			.threshold(50)
			.allUsers(false)
			.alarmUserEntities(Set.of(new AlarmUserEntity("userId1"), new AlarmUserEntity("userId2")))
			.build();

		AlarmEntity saved = alarmEntityRepository.save(alarmEntity);

		boolean exists = alarmEntityRepository.existsByIdAndProjectId(saved.getId(), projectId);
		assertThat(exists).isTrue();
	}

	@Test
	void shouldNotExistByIdAndProjectId() {
		AlarmEntity alarmEntity = AlarmEntity.builder()
			.projectId(projectId2)
			.projectAllocationId(projectAllocationId2)
			.name("name")
			.threshold(50)
			.allUsers(false)
			.alarmUserEntities(Set.of(new AlarmUserEntity("userId1"), new AlarmUserEntity("userId2")))
			.build();

		AlarmEntity saved = alarmEntityRepository.save(alarmEntity);

		boolean exists = alarmEntityRepository.existsByIdAndProjectId(saved.getId(), projectId);
		assertThat(exists).isFalse();
	}

	@Test
	void shouldExistByProjectIdAndName() {
		AlarmEntity alarmEntity = AlarmEntity.builder()
			.projectId(projectId)
			.projectAllocationId(projectAllocationId)
			.name("name")
			.threshold(50)
			.allUsers(false)
			.alarmUserEntities(Set.of(new AlarmUserEntity("userId1"), new AlarmUserEntity("userId2")))
			.build();

		AlarmEntity saved = alarmEntityRepository.save(alarmEntity);

		boolean exists = alarmEntityRepository.existsByProjectIdAndName(projectId, "name");
		assertThat(exists).isTrue();
	}

	@Test
	void shouldNotExistByProjectIdAndName() {
		AlarmEntity alarmEntity = AlarmEntity.builder()
			.projectId(projectId)
			.projectAllocationId(projectAllocationId)
			.name("name")
			.threshold(50)
			.allUsers(false)
			.alarmUserEntities(Set.of(new AlarmUserEntity("userId1"), new AlarmUserEntity("userId2")))
			.build();

		AlarmEntity saved = alarmEntityRepository.save(alarmEntity);

		boolean exists = alarmEntityRepository.existsByProjectIdAndName(projectId, "name2");
		assertThat(exists).isFalse();
	}

	@Test
	void shouldUpdate() {
		AlarmEntity alarmEntity = AlarmEntity.builder()
			.projectId(projectId)
			.projectAllocationId(projectAllocationId)
			.name("name")
			.threshold(50)
			.allUsers(false)
			.alarmUserEntities(Set.of(new AlarmUserEntity("userId1"), new AlarmUserEntity("userId2")))
			.build();

		AlarmEntity saved = alarmEntityRepository.save(alarmEntity);

		AlarmEntity alarmEntity1 = AlarmEntity.builder()
			.id(saved.getId())
			.projectId(projectId)
			.projectAllocationId(projectAllocationId1)
			.name("name2")
			.threshold(30)
			.allUsers(true)
			.alarmUserEntities(Set.of(new AlarmUserEntity("userId1"), new AlarmUserEntity("userId2")))
			.build();

		AlarmEntity updated = alarmEntityRepository.save(alarmEntity1);

		Optional<AlarmEntity> alarm = alarmEntityRepository.findById(saved.getId());
		assertThat(alarm).isPresent();
		assertThat(alarm.get()).isEqualTo(updated);
	}

	@Test
	void shouldFindAllByProjectId() {
		AlarmEntity alarmEntity = AlarmEntity.builder()
			.projectId(projectId)
			.projectAllocationId(projectAllocationId)
			.name("name")
			.threshold(50)
			.allUsers(false)
			.alarmUserEntities(Set.of(new AlarmUserEntity("userId1"), new AlarmUserEntity("userId2")))
			.build();

		AlarmEntity saved = alarmEntityRepository.save(alarmEntity);

		AlarmEntity alarmEntity1 = AlarmEntity.builder()
			.projectId(projectId)
			.projectAllocationId(projectAllocationId1)
			.name("name2")
			.threshold(30)
			.allUsers(true)
			.alarmUserEntities(Set.of(new AlarmUserEntity("userId1"), new AlarmUserEntity("userId2")))
			.build();

		AlarmEntity saved1 = alarmEntityRepository.save(alarmEntity1);

		AlarmEntity alarmEntity2 = AlarmEntity.builder()
			.projectId(projectId2)
			.projectAllocationId(projectAllocationId2)
			.name("name2")
			.threshold(30)
			.allUsers(true)
			.alarmUserEntities(Set.of(new AlarmUserEntity("userId1"), new AlarmUserEntity("userId2")))
			.build();

		AlarmEntity saved2 = alarmEntityRepository.save(alarmEntity2);

		Set<AlarmEntity> alarms = alarmEntityRepository.findAllByProjectId(projectId);
		assertThat(alarms.size()).isEqualTo(2);
		assertThat(alarms).isEqualTo(Set.of(saved, saved1));
	}

	@Test
	void shouldFindAllByUserId() {
		AlarmEntity alarmEntity = AlarmEntity.builder()
			.projectId(projectId)
			.projectAllocationId(projectAllocationId)
			.name("name")
			.threshold(50)
			.allUsers(false)
			.fired(true)
			.alarmUserEntities(Set.of(new AlarmUserEntity("userId1"), new AlarmUserEntity("userId2")))
			.build();

		alarmEntityRepository.save(alarmEntity);

		AlarmEntity alarmEntity1 = AlarmEntity.builder()
			.projectId(projectId)
			.projectAllocationId(projectAllocationId1)
			.name("name1")
			.threshold(30)
			.allUsers(true)
			.fired(true)
			.alarmUserEntities(Set.of(new AlarmUserEntity("userId2"), new AlarmUserEntity("userId3")))
			.build();

		alarmEntityRepository.save(alarmEntity1);

		AlarmEntity alarmEntity2 = AlarmEntity.builder()
			.projectId(projectId2)
			.projectAllocationId(projectAllocationId2)
			.name("name2")
			.threshold(30)
			.allUsers(true)
			.fired(true)
			.alarmUserEntities(Set.of(new AlarmUserEntity("userId1"), new AlarmUserEntity("userId3")))
			.build();

		alarmEntityRepository.save(alarmEntity2);

		Set<ExtendedAlarmEntity> alarms = alarmEntityRepository.findAllFiredByUserId("userId1");
		assertThat(alarms.size()).isEqualTo(2);
		assertThat(alarms.stream().map(entity -> entity.threshold).collect(toSet())).isEqualTo(Set.of(50, 30));
		assertThat(alarms.stream().map(entity -> entity.name).collect(toSet())).isEqualTo(Set.of("name", "name2"));
		assertThat(alarms.stream().map(entity -> entity.allUsers).collect(toSet())).isEqualTo(Set.of(true, false));
	}

	@Test
	void shouldFindAllByProjectIdsOrUserId() {
		AlarmEntity alarmEntity = AlarmEntity.builder()
			.projectId(projectId)
			.projectAllocationId(projectAllocationId)
			.name("name")
			.threshold(50)
			.allUsers(false)
			.fired(true)
			.alarmUserEntities(Set.of(new AlarmUserEntity("userId1"), new AlarmUserEntity("userId2")))
			.build();

		alarmEntityRepository.save(alarmEntity);

		AlarmEntity alarmEntity1 = AlarmEntity.builder()
			.projectId(projectId)
			.projectAllocationId(projectAllocationId1)
			.name("name1")
			.threshold(30)
			.allUsers(false)
			.fired(true)
			.alarmUserEntities(Set.of(new AlarmUserEntity("userId2"), new AlarmUserEntity("userId3")))
			.build();

		alarmEntityRepository.save(alarmEntity1);

		AlarmEntity alarmEntity2 = AlarmEntity.builder()
			.projectId(projectId2)
			.projectAllocationId(projectAllocationId2)
			.name("name2")
			.threshold(30)
			.allUsers(true)
			.fired(true)
			.alarmUserEntities(Set.of(new AlarmUserEntity("userId3")))
			.build();

		alarmEntityRepository.save(alarmEntity2);

		Set<ExtendedAlarmEntity> alarms = alarmEntityRepository.findAllFiredByProjectIdsOrUserId(List.of(projectId2), "userId1");
		assertThat(alarms.size()).isEqualTo(2);
		assertThat(alarms.stream().map(entity -> entity.threshold).collect(toSet())).isEqualTo(Set.of(50, 30));
		assertThat(alarms.stream().map(entity -> entity.name).collect(toSet())).isEqualTo(Set.of("name", "name2"));
		assertThat(alarms.stream().map(entity -> entity.allUsers).collect(toSet())).isEqualTo(Set.of(true, false));
	}
}