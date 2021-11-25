/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.alarms;

import io.imunity.furms.api.alarms.AlarmService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.alarms.Alarm;
import io.imunity.furms.domain.alarms.AlarmId;
import io.imunity.furms.spi.alarms.AlarmRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_LIMITED_READ;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_LIMITED_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;

@Service
class AlarmServiceImpl implements AlarmService {

	private final AlarmRepository alarmRepository;
	private final ApplicationEventPublisher publisher;

	AlarmServiceImpl(AlarmRepository alarmRepository, ApplicationEventPublisher publisher) {
		this.alarmRepository = alarmRepository;
		this.publisher = publisher;
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id="projectId")
	public Set<Alarm> findAll(String projectId) {
		return alarmRepository.findAll(projectId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id="projectId")
	public Optional<Alarm> find(String projectId, AlarmId id) {
		if(!alarmRepository.exist(projectId, id))
			throw new IllegalArgumentException(String.format("Alarm %s and project %s are not related", id.id, projectId));
		return alarmRepository.find(id);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id="alarm.projectId")
	public void create(Alarm alarm) {
		alarmRepository.create(alarm);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id="alarm.projectId")
	public void update(Alarm alarm) {
		alarmRepository.update(alarm);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id="projectId")
	public void remove(String projectId, AlarmId id) {
		if(!alarmRepository.exist(projectId, id))
			throw new IllegalArgumentException(String.format("Alarm %s and project %s are not related", id.id, projectId));
		alarmRepository.remove(id);
	}
}
