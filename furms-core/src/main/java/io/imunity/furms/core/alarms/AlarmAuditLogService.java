/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.alarms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.alarms.AlarmCreatedEvent;
import io.imunity.furms.domain.alarms.AlarmRemovedEvent;
import io.imunity.furms.domain.alarms.AlarmUpdatedEvent;
import io.imunity.furms.domain.alarms.AlarmWithUserIds;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.AuditLogEvent;
import io.imunity.furms.domain.audit_log.AuditLogException;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.spi.users.UsersDAO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;

@Service
class AlarmAuditLogService {

	private final AuthzService authzService;
	private final UsersDAO usersDAO;
	private final ApplicationEventPublisher publisher;
	private final ObjectMapper objectMapper;

	AlarmAuditLogService(AuthzService authzService, UsersDAO usersDAO, ApplicationEventPublisher publisher, ObjectMapper objectMapper) {
		this.authzService = authzService;
		this.usersDAO = usersDAO;
		this.publisher = publisher;
		this.objectMapper = objectMapper;
	}

	@EventListener
	void onAlarmCreatedEvent(AlarmCreatedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.resourceId(event.alarm.id.id)
			.originator(currentAuthNUser)
			.action(Action.CREATE)
			.operationCategory(Operation.ALARM_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.alarm.name)
			.dataJson(toJson(event.alarm))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onAlarmUpdatedEvent(AlarmUpdatedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.resourceId(event.newAlarm.id.id)
			.originator(currentAuthNUser)
			.action(Action.UPDATE)
			.operationCategory(Operation.ALARM_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.newAlarm.name)
			.dataJson(toJsonDiff(event.oldAlarm, event.newAlarm))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onAlarmRemovedEvent(AlarmRemovedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.resourceId(event.alarm.id.id)
			.originator(currentAuthNUser)
			.action(Action.DELETE)
			.operationCategory(Operation.ALARM_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.alarm.name)
			.dataJson(toJson(event.alarm))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	private String toJson(AlarmWithUserIds alarm) {
		Map<String, Object> json = new HashMap<>();
		json.put("id", alarm.id.asRawString());
		json.put("projectId", alarm.projectId.asRawString());
		json.put("projectAllocationId", alarm.projectAllocationId.asRawString());
		json.put("name", alarm.name);
		json.put("threshold", alarm.threshold);
		json.put("notifyAllUsers", alarm.allUsers);
		json.put("usersWhoWillBeNotifiedByAlarm", findUserEmails(alarm));

		try {
			return objectMapper.writeValueAsString(json);
		} catch (JsonProcessingException e) {
			throw new AuditLogException(String.format("Alarm with id %s cannot be parse", alarm.id), e);
		}
	}

	private String toJsonDiff(AlarmWithUserIds oldAlarm, AlarmWithUserIds newAlarm) {
		Map<String, Object> diffs = new HashMap<>();
		if(!Objects.equals(oldAlarm.name, newAlarm.name))
			diffs.put("name", newAlarm.name);
		if(!Objects.equals(oldAlarm.threshold, newAlarm.threshold))
			diffs.put("threshold", newAlarm.threshold);
		if(!Objects.equals(oldAlarm.allUsers, newAlarm.allUsers))
			diffs.put("notifyAllUsers", newAlarm.allUsers);
		if(!Objects.equals(oldAlarm.alarmUser, newAlarm.alarmUser))
			diffs.put("usersWhoWillBeNotifiedByAlarm", findUserEmails(newAlarm));

		try {
			return objectMapper.writeValueAsString(diffs);
		} catch (JsonProcessingException e) {
			throw new AuditLogException(String.format("Alarm with id %s cannot be parse", newAlarm.id), e);
		}
	}

	private Set<String> findUserEmails(AlarmWithUserIds newAlarm) {
		return usersDAO.getAllUsers().stream()
			.filter(x -> newAlarm.alarmUser.contains(x.fenixUserId.orElse(null)))
			.map(x -> x.email)
			.collect(Collectors.toSet());
	}
}
