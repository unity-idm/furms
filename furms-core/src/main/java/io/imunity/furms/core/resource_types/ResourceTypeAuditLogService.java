/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_types;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.AuditLogEvent;
import io.imunity.furms.domain.audit_log.AuditLogException;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.resource_types.ResourceTypeCreatedEvent;
import io.imunity.furms.domain.resource_types.ResourceTypeRemovedEvent;
import io.imunity.furms.domain.resource_types.ResourceTypeUpdatedEvent;
import io.imunity.furms.domain.users.FURMSUser;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;

@Service
class ResourceTypeAuditLogService {

	private final AuthzService authzService;
	private final ApplicationEventPublisher publisher;
	private final ObjectMapper objectMapper;

	ResourceTypeAuditLogService(AuthzService authzService, ApplicationEventPublisher publisher, ObjectMapper objectMapper) {
		this.authzService = authzService;
		this.publisher = publisher;
		this.objectMapper = objectMapper;
	}

	@EventListener
	void onResourceTypeCreatedEvent(ResourceTypeCreatedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.resourceId(event.resourceType.id.id)
			.originator(currentAuthNUser)
			.action(Action.CREATE)
			.operationCategory(Operation.RESOURCE_TYPES_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.resourceType.name)
			.dataJson(toJson(event.resourceType))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onResourceTypeRemovedEvent(ResourceTypeRemovedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.resourceId(event.resourceType.id.id)
			.originator(currentAuthNUser)
			.action(Action.DELETE)
			.operationCategory(Operation.RESOURCE_TYPES_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.resourceType.name)
			.dataJson(toJson(event.resourceType))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onResourceTypeUpdatedEvent(ResourceTypeUpdatedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.resourceId(event.newResourceType.id.id)
			.originator(currentAuthNUser)
			.action(Action.UPDATE)
			.operationCategory(Operation.RESOURCE_TYPES_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.newResourceType.name)
			.dataJson(toJsonDiff(event.oldResourceType, event.newResourceType))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	private String toJson(ResourceType resourceType) {
		try {
			return objectMapper.writeValueAsString(resourceType);
		} catch (JsonProcessingException e) {
			throw new AuditLogException(String.format("Resource type with id %s cannot be parse", resourceType.id), e);
		}
	}

	private String toJsonDiff(ResourceType oldResourceType, ResourceType newResourceType) {
		Map<String, Object> diffs = new HashMap<>();
		if(!Objects.equals(oldResourceType.name, newResourceType.name))
			diffs.put("name", newResourceType.name);
		if(!Objects.equals(oldResourceType.type, newResourceType.type))
			diffs.put("type", newResourceType.type);
		if(!Objects.equals(oldResourceType.unit, newResourceType.unit))
			diffs.put("unit", newResourceType.unit);
		if(oldResourceType.accessibleForAllProjectMembers != (newResourceType.accessibleForAllProjectMembers))
			diffs.put("accessibleForAllProjectMembers", newResourceType.accessibleForAllProjectMembers);

		try {
			return objectMapper.writeValueAsString(diffs);
		} catch (JsonProcessingException e) {
			throw new AuditLogException(String.format("Resource type with id %s cannot be parse", oldResourceType.id), e);
		}
	}
}
