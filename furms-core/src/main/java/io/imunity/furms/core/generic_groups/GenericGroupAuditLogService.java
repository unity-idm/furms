/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.generic_groups;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.AuditLogEvent;
import io.imunity.furms.domain.audit_log.AuditLogException;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.generic_groups.GenericGroup;
import io.imunity.furms.domain.generic_groups.GenericGroupCreatedEvent;
import io.imunity.furms.domain.generic_groups.GenericGroupRemovedEvent;
import io.imunity.furms.domain.generic_groups.GenericGroupUpdatedEvent;
import io.imunity.furms.domain.generic_groups.GenericGroupUserGrantedEvent;
import io.imunity.furms.domain.generic_groups.GenericGroupUserRevokedEvent;
import io.imunity.furms.domain.users.FURMSUser;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;

@Service
class GenericGroupAuditLogService {

	private final AuthzService authzService;
	private final ApplicationEventPublisher publisher;
	private final ObjectMapper objectMapper;

	GenericGroupAuditLogService(AuthzService authzService, ApplicationEventPublisher publisher, ObjectMapper objectMapper) {
		this.authzService = authzService;
		this.publisher = publisher;
		this.objectMapper = objectMapper;
	}

	@EventListener
	void onGenericGroupCreatedEvent(GenericGroupCreatedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.originator(currentAuthNUser)
			.action(Action.CREATE)
			.operationCategory(Operation.GENERIC_GROUPS_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.group.name)
			.dataJson(toJson(event.group))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onGenericGroupRemovedEvent(GenericGroupRemovedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.originator(currentAuthNUser)
			.action(Action.DELETE)
			.operationCategory(Operation.GENERIC_GROUPS_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.group.name)
			.dataJson(toJson(event.group))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onGenericGroupUpdatedEvent(GenericGroupUpdatedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.originator(currentAuthNUser)
			.action(Action.UPDATE)
			.operationCategory(Operation.GENERIC_GROUPS_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.newGroup.name)
			.dataJson(toJsonDiff(event.oldGroup, event.newGroup))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onGenericGroupUserGrantedEvent(GenericGroupUserGrantedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.originator(currentAuthNUser)
			.action(Action.GRANT)
			.operationCategory(Operation.GENERIC_GROUPS_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.user)
			.dataJson(toJson(event.group))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onGenericGroupUserRevokedEvent(GenericGroupUserRevokedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.originator(currentAuthNUser)
			.action(Action.REVOKE)
			.operationCategory(Operation.GENERIC_GROUPS_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.user)
			.dataJson(toJson(event.group))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	private String toJson(GenericGroup group) {
		try {
			return objectMapper.writeValueAsString(group);
		} catch (JsonProcessingException e) {
			throw new AuditLogException(String.format("Group with id %s cannot be parse", group.id), e);
		}
	}

	private String toJsonDiff(GenericGroup oldGroup, GenericGroup newGroup) {
		Map<String, Object> diffs = new HashMap<>();
		if(!oldGroup.name.equals(newGroup.name))
			diffs.put("name", newGroup.name);
		if(!oldGroup.description.equals(newGroup.description))
			diffs.put("description", newGroup.description);

		try {
			return objectMapper.writeValueAsString(diffs);
		} catch (JsonProcessingException e) {
			throw new AuditLogException(String.format("Group with id %s cannot be parse", oldGroup.id), e);
		}
	}
}
