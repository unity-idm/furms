/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.alarms;

import io.imunity.furms.domain.alarms.AlarmId;
import io.imunity.furms.domain.alarms.AlarmWithUserIds;
import io.imunity.furms.domain.alarms.FiredAlarm;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.alarms.AlarmRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
class AlarmDatabaseRepository implements AlarmRepository {

	private final AlarmEntityRepository repository;

	AlarmDatabaseRepository(AlarmEntityRepository repository) {
		this.repository = repository;
	}

	@Override
	public Set<AlarmWithUserIds> findAll(String projectId) {
		return repository.findAllByProjectId(UUID.fromString(projectId)).stream()
			.map(this::map)
			.collect(Collectors.toSet());
	}

	@Override
	public Set<FiredAlarm> findAll(List<UUID> projectIds, FenixUserId userId) {
		Set<ExtendedAlarmEntity> relatedAlarms;
		if(projectIds.isEmpty())
			relatedAlarms = repository.findAllFiredByUserId(userId.id);
		else
			relatedAlarms = repository.findAllFiredByProjectIdsOrUserId(projectIds, userId.id);

		return relatedAlarms.stream()
			.map(this::map)
			.collect(Collectors.toSet());
	}

	@Override
	public Optional<AlarmWithUserIds> find(AlarmId id) {
		return repository.findById(id.id)
			.map(this::map);
	}

	@Override
	public Optional<AlarmWithUserIds> find(String projectAllocationId) {
		return repository.findByProjectAllocationId(UUID.fromString(projectAllocationId))
			.map(this::map);
	}

	private AlarmWithUserIds map(AlarmEntity alarmEntity) {
		return AlarmWithUserIds.builder()
			.id(new AlarmId(alarmEntity.getId()))
			.projectId(alarmEntity.projectId.toString())
			.projectAllocationId(alarmEntity.projectAllocationId.toString())
			.name(alarmEntity.name)
			.threshold(alarmEntity.threshold)
			.allUsers(alarmEntity.allUsers)
			.fired(alarmEntity.fired)
			.alarmUser(alarmEntity.alarmUserEntities.stream()
				.map(y -> new FenixUserId(y.userId))
				.collect(Collectors.toSet())
			)
			.build();
	}

	private FiredAlarm map(ExtendedAlarmEntity alarmEntity) {
		return FiredAlarm.builder()
			.alarmId(new AlarmId(alarmEntity.getId()))
			.projectId(alarmEntity.projectId.toString())
			.projectAllocationId(alarmEntity.projectAllocationId.toString())
			.alarmName(alarmEntity.name)
			.projectAllocationName(alarmEntity.projectAllocationName)
			.build();
	}

	@Override
	public AlarmId create(AlarmWithUserIds alarm) {
		AlarmEntity alarmEntity = AlarmEntity.builder()
			.projectId(UUID.fromString(alarm.projectId))
			.projectAllocationId(UUID.fromString(alarm.projectAllocationId))
			.name(alarm.name)
			.threshold(alarm.threshold)
			.allUsers(alarm.allUsers)
			.alarmUserEntities(alarm.alarmUser.stream()
				.map(userId -> new AlarmUserEntity(userId.id))
				.collect(Collectors.toSet())
			)
			.build();
		AlarmEntity saved = repository.save(alarmEntity);
		return new AlarmId(saved.getId());
	}

	@Override
	public void update(AlarmWithUserIds alarm) {
		repository.findById(alarm.id.id)
			.map(alarmEntity -> AlarmEntity.builder()
				.id(alarmEntity.getId())
				.projectId(alarmEntity.projectId)
				.projectAllocationId(alarmEntity.projectAllocationId)
				.fired(alarmEntity.fired)
				.name(alarm.name)
				.fired(alarm.fired)
				.threshold(alarm.threshold)
				.allUsers(alarm.allUsers)
				.alarmUserEntities(alarm.alarmUser.stream()
					.map(userId -> new AlarmUserEntity(userId.id))
					.collect(Collectors.toSet())
				)
				.build()
			).ifPresent(repository::save);
	}

	@Override
	public void updateToFired(AlarmWithUserIds alarm) {
		repository.findById(alarm.id.id)
			.map(alarmEntity -> AlarmEntity.builder()
				.id(alarmEntity.getId())
				.projectId(alarmEntity.projectId)
				.projectAllocationId(alarmEntity.projectAllocationId)
				.name(alarmEntity.name)
				.threshold(alarmEntity.threshold)
				.fired(true)
				.allUsers(alarmEntity.allUsers)
				.alarmUserEntities(alarmEntity.alarmUserEntities)
				.build()
			).ifPresent(repository::save);
	}

	@Override
	public void remove(AlarmId id) {
		repository.deleteById(id.id);
	}

	@Override
	public boolean exist(String projectId, AlarmId id) {
		return repository.existsByIdAndProjectId(id.id, UUID.fromString(projectId));
	}

	@Override
	public boolean exist(String projectId, String name) {
		return repository.existsByProjectIdAndName(UUID.fromString(projectId), name);
	}
}
