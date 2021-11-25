/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.alarms;

import io.imunity.furms.domain.alarms.Alarm;
import io.imunity.furms.domain.alarms.AlarmId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.spi.alarms.AlarmRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

@Repository
class AlarmDatabaseRepository implements AlarmRepository {

	private final AlarmEntityRepository repository;
	private final UsersDAO usersDAO;

	AlarmDatabaseRepository(AlarmEntityRepository repository, UsersDAO usersDAO) {
		this.repository = repository;
		this.usersDAO = usersDAO;
	}

	@Override
	public Set<Alarm> findAll(String projectId) {
		Map<String, FURMSUser> userIdFURMSUserMap = getGroupedUsers();
		return repository.findAllByProjectId(UUID.fromString(projectId)).stream()
			.map(alarmEntity -> map(userIdFURMSUserMap, alarmEntity))
			.collect(Collectors.toSet());
	}
	@Override
	public Optional<Alarm> find(AlarmId id) {
		Map<String, FURMSUser> userIdFURMSUserMap = getGroupedUsers();
		return repository.findById(id.id)
			.map(alarmEntity -> map(userIdFURMSUserMap, alarmEntity));
	}

	private Alarm map(Map<String, FURMSUser> userIdFURMSUserMap, AlarmEntity alarmEntity) {
		return Alarm.builder()
			.id(new AlarmId(alarmEntity.getId()))
			.projectId(alarmEntity.projectId.toString())
			.projectAllocationId(alarmEntity.projectAllocationId.toString())
			.name(alarmEntity.name)
			.threshold(alarmEntity.threshold)
			.allUsers(alarmEntity.allUsers)
			.alarmUser(alarmEntity.alarmUserEntities.stream()
				.map(y -> userIdFURMSUserMap.get(y.userId))
				.collect(Collectors.toSet())
			)
			.build();
	}

	private Map<String, FURMSUser> getGroupedUsers() {
		return usersDAO.getAllUsers().stream()
			.filter(user -> user.fenixUserId.isPresent())
			.collect(Collectors.toMap(user -> user.fenixUserId.get().id, identity()));
	}

	@Override
	public AlarmId create(Alarm alarm) {
		AlarmEntity alarmEntity = AlarmEntity.builder()
			.id(alarm.id.id)
			.projectId(UUID.fromString(alarm.projectId))
			.projectAllocationId(UUID.fromString(alarm.projectAllocationId))
			.name(alarm.name)
			.threshold(alarm.threshold)
			.allUsers(alarm.allUsers)
			.alarmUserEntities(alarm.alarmUser.stream()
				.map(x -> new AlarmUserEntity(x.fenixUserId.get().id))
				.collect(Collectors.toSet())
			)
			.build();
		AlarmEntity saved = repository.save(alarmEntity);
		return new AlarmId(saved.getId());
	}

	@Override
	public void update(Alarm alarm) {
		repository.findById(alarm.id.id)
			.map(alarmEntity -> AlarmEntity.builder()
				.id(alarmEntity.getId())
				.projectId(alarmEntity.projectId)
				.projectAllocationId(alarmEntity.projectAllocationId)
				.name(alarm.name)
				.threshold(alarm.threshold)
				.allUsers(alarm.allUsers)
				.alarmUserEntities(alarm.alarmUser.stream()
					.map(y -> new AlarmUserEntity(y.fenixUserId.get().id))
					.collect(Collectors.toSet())
				)
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
}
