/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.alarms;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.alarms.AlarmId;
import io.imunity.furms.domain.alarms.AlarmWithUserIds;
import io.imunity.furms.domain.alarms.FiredAlarm;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
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
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.services.InfraServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AlarmDatabaseRepositoryTest extends DBIntegrationTest {

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
	private AlarmEntityRepository alarmEntityRepository;
	@Autowired
	private AlarmDatabaseRepository databaseRepository;

	private ProjectId projectId;
	private ProjectId projectId2;

	private ProjectAllocationId projectAllocationId;
	private ProjectAllocationId projectAllocationId1;
	private ProjectAllocationId projectAllocationId2;

	@BeforeEach
	void init() {
		Site site = Site.builder()
			.name("name")
			.build();
		Site site1 = Site.builder()
			.name("name2")
			.build();
		SiteId siteId = siteRepository.create(site, new SiteExternalId("id"));
		siteRepository.create(site1, new SiteExternalId("id2"));

		Community community = Community.builder()
			.name("name")
			.logo(FurmsImage.empty())
			.build();
		Community community2 = Community.builder()
			.name("name1")
			.logo(FurmsImage.empty())
			.build();
		CommunityId communityId = communityRepository.create(community);
		CommunityId communityId2 = communityRepository.create(community2);

		Project project = Project.builder()
			.communityId(communityId)
			.name("name")
			.description("new_description")
			.logo(FurmsImage.empty())
			.acronym("acronym")
			.researchField("research filed")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();
		Project project2 = Project.builder()
			.communityId(communityId2)
			.name("name2")
			.logo(FurmsImage.empty())
			.description("new_description")
			.acronym("acronym")
			.researchField("research filed")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();

		projectId = projectRepository.create(project);
		projectId2 = projectRepository.create(project2);

		InfraService service = InfraService.builder()
			.siteId(siteId)
			.name("name")
			.build();

		InfraServiceId serviceId = infraServiceRepository.create(service);

		ResourceType resourceType = ResourceType.builder()
			.siteId(siteId)
			.serviceId(serviceId)
			.name("name")
			.type(ResourceMeasureType.FLOATING_POINT)
			.unit(ResourceMeasureUnit.KILO)
			.build();
		ResourceTypeId resourceTypeId = resourceTypeRepository.create(resourceType);

		ResourceCreditId resourceCreditId = resourceCreditRepository.create(ResourceCredit.builder()
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
			.name("name")
			.splittable(true)
			.amount(new BigDecimal(100))
			.utcCreateTime(LocalDateTime.now())
			.utcStartTime(LocalDateTime.now().plusDays(1))
			.utcEndTime(LocalDateTime.now().plusDays(3))
			.build());

		CommunityAllocationId communityAllocationId = communityAllocationRepository.create(
			CommunityAllocation.builder()
				.communityId(communityId)
				.resourceCreditId(resourceCreditId)
				.name("anem")
				.amount(new BigDecimal(10))
				.build()
		);
		CommunityAllocationId communityAllocationId2 = communityAllocationRepository.create(
			CommunityAllocation.builder()
				.communityId(communityId)
				.resourceCreditId(resourceCreditId)
				.name("anem2")
				.amount(new BigDecimal(30))
				.build()
		);

		projectAllocationId = projectAllocationRepository.create(
			ProjectAllocation.builder()
				.projectId(projectId)
				.communityAllocationId(communityAllocationId)
				.name("anem")
				.amount(new BigDecimal(10))
				.build()
		);
		projectAllocationId1 = projectAllocationRepository.create(
			ProjectAllocation.builder()
				.projectId(projectId)
				.communityAllocationId(communityAllocationId2)
				.name("name1")
				.amount(new BigDecimal(20))
				.build()
		);
		projectAllocationId2 = projectAllocationRepository.create(
			ProjectAllocation.builder()
				.projectId(projectId2)
				.communityAllocationId(communityAllocationId2)
				.name("anem2")
				.amount(new BigDecimal(30))
				.build()
		);
	}

	@AfterEach
	void clean(){
		alarmEntityRepository.deleteAll();
	}

	@Test
	void shouldCreate() {
		Set<FenixUserId> alarmUser = Set.of(new FenixUserId("userId1"), new FenixUserId("userId2"));
		AlarmWithUserIds alarm = AlarmWithUserIds.builder()
			.projectId(projectId)
			.projectAllocationId(projectAllocationId)
			.name("name")
			.threshold(50)
			.allUsers(false)
			.alarmUser(alarmUser)
			.build();

		AlarmId alarmId = databaseRepository.create(alarm);

		Optional<AlarmEntity> alarmEntity = alarmEntityRepository.findById(alarmId.id);
		assertThat(alarmEntity).isPresent();
		assertThat(alarmEntity.get().projectId).isEqualTo(projectId.id);
		assertThat(alarmEntity.get().projectAllocationId).isEqualTo(projectAllocationId.id);
		assertThat(alarmEntity.get().name).isEqualTo("name");
		assertThat(alarmEntity.get().threshold).isEqualTo(50);
		assertThat(alarmEntity.get().allUsers).isEqualTo(false);
		assertThat(alarmEntity.get().alarmUserEntities.stream().map(usr -> new FenixUserId(usr.userId)).collect(toSet())).isEqualTo(alarmUser);
	}

	@Test
	void shouldUpdate() {
		Set<AlarmUserEntity> alarmUser = Set.of(new AlarmUserEntity("userId1"), new AlarmUserEntity("userId2"));
		AlarmEntity alarm = AlarmEntity.builder()
			.projectId(projectId.id)
			.projectAllocationId(projectAllocationId.id)
			.name("name")
			.threshold(50)
			.allUsers(false)
			.alarmUserEntities(alarmUser)
			.build();

		AlarmEntity saved = alarmEntityRepository.save(alarm);

		Set<FenixUserId> alarmUser1 = Set.of(new FenixUserId("userId1"), new FenixUserId("userId2"));
		AlarmId alarmId = new AlarmId(saved.getId());

		AlarmWithUserIds alarm1 = AlarmWithUserIds.builder()
			.id(alarmId)
			.projectId(projectId)
			.projectAllocationId(projectAllocationId)
			.name("name2")
			.threshold(60)
			.allUsers(true)
			.alarmUser(alarmUser1)
			.build();

		databaseRepository.update(alarm1);

		Optional<AlarmEntity> alarmEntity = alarmEntityRepository.findById(alarmId.id);
		assertThat(alarmEntity).isPresent();
		assertThat(alarmEntity.get().projectId).isEqualTo(projectId.id);
		assertThat(alarmEntity.get().projectAllocationId).isEqualTo(projectAllocationId.id);
		assertThat(alarmEntity.get().name).isEqualTo("name2");
		assertThat(alarmEntity.get().threshold).isEqualTo(60);
		assertThat(alarmEntity.get().allUsers).isEqualTo(true);
		assertThat(alarmEntity.get().alarmUserEntities.stream().map(usr -> new FenixUserId(usr.userId)).collect(toSet())).isEqualTo(alarmUser1);
	}

	@Test
	void shouldFindAll() {
		AlarmEntity alarm = AlarmEntity.builder()
			.projectId(projectId.id)
			.projectAllocationId(projectAllocationId.id)
			.name("name")
			.threshold(50)
			.allUsers(false)
			.alarmUserEntities(Set.of(new AlarmUserEntity("userId1"), new AlarmUserEntity("userId2")))
			.build();

		alarmEntityRepository.save(alarm);

		AlarmEntity alarm1 = AlarmEntity.builder()
			.projectId(projectId.id)
			.projectAllocationId(projectAllocationId1.id)
			.name("name1")
			.threshold(50)
			.allUsers(false)
			.alarmUserEntities(Set.of(new AlarmUserEntity("userId1"), new AlarmUserEntity("userId2")))
			.build();

		alarmEntityRepository.save(alarm1);

		AlarmEntity alarm2 = AlarmEntity.builder()
			.projectId(projectId2.id)
			.projectAllocationId(projectAllocationId2.id)
			.name("name2")
			.threshold(50)
			.allUsers(false)
			.alarmUserEntities(Set.of(new AlarmUserEntity("userId1"), new AlarmUserEntity("userId2")))
			.build();

		alarmEntityRepository.save(alarm2);

		Set<AlarmWithUserIds> all = databaseRepository.findAll(projectId);
		assertThat(all.size()).isEqualTo(2);
		assertThat(all.stream().map(a -> a.projectId).collect(toSet())).isEqualTo(Set.of(projectId));
		assertThat(all.stream().map(a -> a.projectAllocationId).collect(toSet())).isEqualTo(Set.of(projectAllocationId, projectAllocationId1));
		assertThat(all.stream().map(a -> a.name).collect(toSet())).isEqualTo(Set.of("name", "name1"));
		assertThat(all.stream().map(a -> a.threshold).collect(toSet())).isEqualTo(Set.of(50));
		assertThat(all.stream().map(a -> a.allUsers).collect(toSet())).isEqualTo(Set.of(false));
	}

	@Test
	void shouldRemove() {
		Set<AlarmUserEntity> alarmUser = Set.of(new AlarmUserEntity("userId1"), new AlarmUserEntity("userId2"));
		AlarmEntity alarm = AlarmEntity.builder()
			.projectId(projectId.id)
			.projectAllocationId(projectAllocationId.id)
			.name("name")
			.threshold(50)
			.allUsers(false)
			.alarmUserEntities(alarmUser)
			.build();

		AlarmEntity saved = alarmEntityRepository.save(alarm);

		AlarmId alarmId = new AlarmId(saved.getId());

		databaseRepository.remove(alarmId);

		Optional<AlarmEntity> alarmEntity = alarmEntityRepository.findById(alarmId.id);
		assertThat(alarmEntity).isEmpty();
	}

	@Test
	void shouldExistByProjectIdAndAlarmId() {
		Set<AlarmUserEntity> alarmUser = Set.of(new AlarmUserEntity("userId1"), new AlarmUserEntity("userId2"));
		AlarmEntity alarm = AlarmEntity.builder()
			.projectId(projectId.id)
			.projectAllocationId(projectAllocationId.id)
			.name("name")
			.threshold(50)
			.allUsers(false)
			.alarmUserEntities(alarmUser)
			.build();

		AlarmEntity saved = alarmEntityRepository.save(alarm);

		AlarmId alarmId = new AlarmId(saved.getId());

		databaseRepository.exist(projectId, alarmId);

		assertThat(databaseRepository.exist(projectId, alarmId)).isTrue();
	}

	@Test
	void shouldNotExistByProjectIdAndAlarmId() {
		Set<AlarmUserEntity> alarmUser = Set.of(new AlarmUserEntity("userId1"), new AlarmUserEntity("userId2"));
		AlarmEntity alarm = AlarmEntity.builder()
			.projectId(projectId2.id)
			.projectAllocationId(projectAllocationId2.id)
			.name("name")
			.threshold(50)
			.allUsers(false)
			.alarmUserEntities(alarmUser)
			.build();

		AlarmEntity saved = alarmEntityRepository.save(alarm);

		AlarmId alarmId = new AlarmId(saved.getId());

		databaseRepository.exist(projectId, alarmId);

		assertThat(databaseRepository.exist(projectId, alarmId)).isFalse();
	}

	@Test
	void shouldExistByProjectIdAndName() {
		Set<AlarmUserEntity> alarmUser = Set.of(new AlarmUserEntity("userId1"), new AlarmUserEntity("userId2"));
		AlarmEntity alarm = AlarmEntity.builder()
			.projectId(projectId.id)
			.projectAllocationId(projectAllocationId.id)
			.name("name")
			.threshold(50)
			.allUsers(false)
			.alarmUserEntities(alarmUser)
			.build();

		AlarmEntity saved = alarmEntityRepository.save(alarm);

		AlarmId alarmId = new AlarmId(saved.getId());

		databaseRepository.exist(projectId, alarmId);

		assertThat(databaseRepository.exist(projectId, "name")).isTrue();
	}

	@Test
	void shouldNotExistByProjectIdAndName() {
		Set<AlarmUserEntity> alarmUser = Set.of(new AlarmUserEntity("userId1"), new AlarmUserEntity("userId2"));
		AlarmEntity alarm = AlarmEntity.builder()
			.projectId(projectId.id)
			.projectAllocationId(projectAllocationId.id)
			.name("name")
			.threshold(50)
			.allUsers(false)
			.alarmUserEntities(alarmUser)
			.build();

		AlarmEntity saved = alarmEntityRepository.save(alarm);

		AlarmId alarmId = new AlarmId(saved.getId());

		databaseRepository.exist(projectId, alarmId);

		assertThat(databaseRepository.exist(projectId, "name2")).isFalse();
	}

	@Test
	void shouldFindByAlarmId() {
		Set<AlarmUserEntity> alarmUser = Set.of(new AlarmUserEntity("userId1"), new AlarmUserEntity("userId2"));
		AlarmEntity alarm = AlarmEntity.builder()
			.projectId(projectId.id)
			.projectAllocationId(projectAllocationId.id)
			.name("name")
			.threshold(50)
			.allUsers(false)
			.alarmUserEntities(alarmUser)
			.build();

		AlarmEntity saved = alarmEntityRepository.save(alarm);

		AlarmId alarmId = new AlarmId(saved.getId());

		Optional<AlarmWithUserIds> alarmWithUserIds = databaseRepository.find(alarmId);

		assertThat(alarmWithUserIds).isPresent();
		assertThat(alarmWithUserIds.get().projectId).isEqualTo(projectId);
		assertThat(alarmWithUserIds.get().projectAllocationId).isEqualTo(projectAllocationId);
		assertThat(alarmWithUserIds.get().name).isEqualTo("name");
		assertThat(alarmWithUserIds.get().threshold).isEqualTo(50);
		assertThat(alarmWithUserIds.get().allUsers).isEqualTo(false);
		assertThat(alarmWithUserIds.get().alarmUser).isEqualTo(Set.of(new FenixUserId("userId1"), new FenixUserId("userId2")));
	}

	@Test
	void shouldFindByProjectAllocationId() {
		Set<AlarmUserEntity> alarmUser = Set.of(new AlarmUserEntity("userId1"), new AlarmUserEntity("userId2"));
		AlarmEntity alarm = AlarmEntity.builder()
			.projectId(projectId.id)
			.projectAllocationId(projectAllocationId.id)
			.name("name")
			.threshold(50)
			.allUsers(false)
			.alarmUserEntities(alarmUser)
			.build();

		alarmEntityRepository.save(alarm);

		Optional<AlarmWithUserIds> alarmWithUserIds = databaseRepository.find(projectAllocationId);

		assertThat(alarmWithUserIds).isPresent();
		assertThat(alarmWithUserIds.get().projectId).isEqualTo(projectId);
		assertThat(alarmWithUserIds.get().projectAllocationId).isEqualTo(projectAllocationId);
		assertThat(alarmWithUserIds.get().name).isEqualTo("name");
		assertThat(alarmWithUserIds.get().threshold).isEqualTo(50);
		assertThat(alarmWithUserIds.get().allUsers).isEqualTo(false);
		assertThat(alarmWithUserIds.get().alarmUser).isEqualTo(Set.of(new FenixUserId("userId1"), new FenixUserId("userId2")));
	}

	@Test
	void shouldFindAllByUserId() {
		AlarmEntity alarmEntity = AlarmEntity.builder()
			.projectId(projectId.id)
			.projectAllocationId(projectAllocationId.id)
			.name("name")
			.threshold(50)
			.allUsers(false)
			.fired(true)
			.alarmUserEntities(Set.of(new AlarmUserEntity("userId1"), new AlarmUserEntity("userId2")))
			.build();

		AlarmEntity saved = alarmEntityRepository.save(alarmEntity);

		AlarmEntity alarmEntity1 = AlarmEntity.builder()
			.projectId(projectId.id)
			.projectAllocationId(projectAllocationId1.id)
			.name("name1")
			.threshold(50)
			.allUsers(true)
			.fired(false)
			.alarmUserEntities(Set.of(new AlarmUserEntity("userId1"), new AlarmUserEntity("userId3")))
			.build();

		alarmEntityRepository.save(alarmEntity1);

		AlarmEntity alarmEntity2 = AlarmEntity.builder()
			.projectId(projectId2.id)
			.projectAllocationId(projectAllocationId2.id)
			.name("name2")
			.threshold(30)
			.allUsers(true)
			.fired(true)
			.alarmUserEntities(Set.of(new AlarmUserEntity("userId1"), new AlarmUserEntity("userId3")))
			.build();

		AlarmEntity saved2 = alarmEntityRepository.save(alarmEntity2);

		Set<FiredAlarm> alarms = databaseRepository.findAll(List.of(), new FenixUserId("userId1"));
		assertThat(alarms.size()).isEqualTo(2);
		assertThat(alarms.stream().map(activeAlarm -> activeAlarm.alarmId).collect(toSet())).isEqualTo(Set.of(new AlarmId(saved.getId()), new AlarmId(saved2.getId())));
		assertThat(alarms.stream().map(activeAlarm -> activeAlarm.alarmName).collect(toSet())).isEqualTo(Set.of("name", "name2"));
		assertThat(alarms.stream().map(activeAlarm -> activeAlarm.projectId).collect(toSet())).isEqualTo(Set.of(projectId, projectId2));
		assertThat(alarms.stream().map(activeAlarm -> activeAlarm.projectAllocationId).collect(toSet())).isEqualTo(Set.of(projectAllocationId, projectAllocationId2));
		assertThat(alarms.stream().map(activeAlarm -> activeAlarm.projectAllocationName).collect(toSet())).isEqualTo(Set.of("anem2", "anem"));
	}

	@Test
	void shouldFindAllByProjectIdsOrUserId() {
		AlarmEntity alarmEntity = AlarmEntity.builder()
			.projectId(projectId.id)
			.projectAllocationId(projectAllocationId.id)
			.name("name")
			.threshold(50)
			.allUsers(false)
			.fired(true)
			.alarmUserEntities(Set.of(new AlarmUserEntity("userId1"), new AlarmUserEntity("userId2")))
			.build();

		AlarmEntity saved = alarmEntityRepository.save(alarmEntity);

		AlarmEntity alarmEntity1 = AlarmEntity.builder()
			.projectId(projectId.id)
			.projectAllocationId(projectAllocationId1.id)
			.name("name1")
			.threshold(50)
			.allUsers(true)
			.fired(false)
			.alarmUserEntities(Set.of(new AlarmUserEntity("userId2"), new AlarmUserEntity("userId3")))
			.build();

		alarmEntityRepository.save(alarmEntity1);

		AlarmEntity alarmEntity2 = AlarmEntity.builder()
			.projectId(projectId2.id)
			.projectAllocationId(projectAllocationId2.id)
			.name("name2")
			.threshold(30)
			.allUsers(true)
			.fired(true)
			.alarmUserEntities(Set.of(new AlarmUserEntity("userId3")))
			.build();

		AlarmEntity saved2 = alarmEntityRepository.save(alarmEntity2);

		Set<FiredAlarm> alarms = databaseRepository.findAll(List.of(projectId, projectId2), new FenixUserId("userId1"));
		assertThat(alarms.size()).isEqualTo(2);
		assertThat(alarms.stream().map(activeAlarm -> activeAlarm.alarmId).collect(toSet())).isEqualTo(Set.of(new AlarmId(saved.getId()), new AlarmId(saved2.getId())));
		assertThat(alarms.stream().map(activeAlarm -> activeAlarm.alarmName).collect(toSet())).isEqualTo(Set.of("name", "name2"));
		assertThat(alarms.stream().map(activeAlarm -> activeAlarm.projectId).collect(toSet())).isEqualTo(Set.of(projectId, projectId2));
		assertThat(alarms.stream().map(activeAlarm -> activeAlarm.projectAllocationId).collect(toSet())).isEqualTo(Set.of(projectAllocationId, projectAllocationId2));
		assertThat(alarms.stream().map(activeAlarm -> activeAlarm.projectAllocationName).collect(toSet())).isEqualTo(Set.of("anem2", "anem"));
	}
}