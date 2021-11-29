/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.alarms;

import io.imunity.furms.api.alarms.AlarmService;
import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.validation.exceptions.EmailNotPresentException;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.alarms.AlarmCreatedEvent;
import io.imunity.furms.domain.alarms.AlarmId;
import io.imunity.furms.domain.alarms.AlarmRemovedEvent;
import io.imunity.furms.domain.alarms.AlarmUpdatedEvent;
import io.imunity.furms.domain.alarms.AlarmWithUserEmails;
import io.imunity.furms.domain.alarms.AlarmWithUserIds;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.alarms.AlarmRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_LIMITED_READ;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_LIMITED_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;

@Service
class AlarmServiceImpl implements AlarmService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final AlarmRepository alarmRepository;
	private final UsersDAO usersDAO;
	private final ApplicationEventPublisher publisher;

	AlarmServiceImpl(AlarmRepository alarmRepository, UsersDAO usersDAO, ApplicationEventPublisher publisher) {
		this.alarmRepository = alarmRepository;
		this.usersDAO = usersDAO;
		this.publisher = publisher;
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id="projectId")
	public Set<AlarmWithUserEmails> findAll(String projectId) {
		Map<FenixUserId, String> groupedUsers = getUserEmails();
		return alarmRepository.findAll(projectId).stream()
			.map(alarm -> new AlarmWithUserEmails(alarm, getUserIds(groupedUsers, alarm)))
			.collect(Collectors.toSet());
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id="projectId")
	public Optional<AlarmWithUserEmails> find(String projectId, AlarmId id) {
		if(!alarmRepository.exist(projectId, id))
			throw new IllegalArgumentException(String.format("Alarm %s and project %s are not related", id.id, projectId));
		Map<FenixUserId, String> groupedUsers = getUserEmails();

		return alarmRepository.find(id)
			.map(alarm -> new AlarmWithUserEmails(alarm, getUserIds(groupedUsers, alarm)));
	}

	private Set<String> getUserIds(Map<FenixUserId, String> groupedUsers, AlarmWithUserIds alarm) {
		return alarm.alarmUser.stream()
			.map(userId -> groupedUsers.getOrDefault(userId, userId.id))
			.collect(Collectors.toSet());
	}

	private Map<FenixUserId, String> getUserEmails() {
		return usersDAO.getAllUsers().stream()
			.filter(user -> user.fenixUserId.isPresent())
			.collect(Collectors.toMap(user -> user.fenixUserId.get(), user -> user.email));
	}

	private Map<String, FenixUserId> getUserIds() {
		return usersDAO.getAllUsers().stream()
			.filter(user -> user.fenixUserId.isPresent())
			.collect(Collectors.toMap(user -> user.email, user -> user.fenixUserId.get()));
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id="alarm.projectId")
	public void create(AlarmWithUserEmails alarm) {
		assertUniquenessForCreate(alarm.projectId, alarm.name);
		Map<String, FenixUserId> userIds = getUserIds();
		Set<FenixUserId> ids = getUserIds(alarm, userIds);
		AlarmWithUserIds alarmWithUserIds = new AlarmWithUserIds(alarm, ids);
		AlarmId alarmId = alarmRepository.create(alarmWithUserIds);
		publisher.publishEvent(new AlarmCreatedEvent(alarmWithUserIds));
		LOG.info("Alarm ID {} for project allocation ID: {} was created", alarmId.id, alarm.projectAllocationId);
	}

	private Set<FenixUserId> getUserIds(AlarmWithUserEmails alarm, Map<String, FenixUserId> userIds) {
		return alarm.alarmUserEmails.stream()
			.map(email -> {
				if (userIds.containsKey(email))
					return userIds.get(email);
				else
					throw new EmailNotPresentException(email);
			})
			.collect(Collectors.toSet());
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id="alarm.projectId")
	public void update(AlarmWithUserEmails alarm) {
		if(!alarmRepository.exist(alarm.projectId, alarm.id))
			throw new IllegalArgumentException(String.format("Alarm %s and project %s are not related", alarm.id, alarm.projectId));
		assertUniquenessForUpdate(alarm.projectId, alarm.id, alarm.name);
		AlarmWithUserIds oldAlarm = alarmRepository.find(alarm.id).get();
		Map<String, FenixUserId> userIds = getUserIds();
		Set<FenixUserId> ids = getUserIds(alarm, userIds);
		AlarmWithUserIds alarmWithUserIds = new AlarmWithUserIds(alarm, ids);
		alarmRepository.update(alarmWithUserIds);
		publisher.publishEvent(new AlarmUpdatedEvent(alarmWithUserIds, oldAlarm));
		LOG.info("Alarm ID {} for project allocation ID: {} was updated", alarm.id.id, alarm.projectAllocationId);
	}

	private void assertUniquenessForCreate(String projectId, String name) {
		if(alarmRepository.exist(projectId, name))
			throw new DuplicatedNameValidationError(String.format("Alarm name: %s - already exists", name));
	}

	private void assertUniquenessForUpdate(String projectId, AlarmId alarmId, String name) {
		boolean present = alarmRepository.find(alarmId)
			.filter(x -> !x.name.equals(name))
			.isPresent();
		if(present && alarmRepository.exist(projectId, name))
			throw new DuplicatedNameValidationError(String.format("Alarm name: %s - already exists", name));
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id="projectId")
	public void remove(String projectId, AlarmId id) {
		if(!alarmRepository.exist(projectId, id))
			throw new IllegalArgumentException(String.format("Alarm %s and project %s are not related", id.id, projectId));
		AlarmWithUserIds alarm = alarmRepository.find(id).get();
		alarmRepository.remove(id);
		publisher.publishEvent(new AlarmRemovedEvent(alarm));
		LOG.info("Alarm ID {} for project allocation ID: {} was removed", alarm.id.id, alarm.projectAllocationId);
	}
}
