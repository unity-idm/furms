/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.AuditLogEvent;
import io.imunity.furms.domain.audit_log.AuditLogException;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationCreatedEvent;
import io.imunity.furms.domain.project_allocation.ProjectAllocationRemovedEvent;
import io.imunity.furms.domain.project_allocation.ProjectAllocationUpdatedEvent;
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
class ProjectAllocationAuditLogService {

	private final AuthzService authzService;
	private final ApplicationEventPublisher publisher;
	private final ObjectMapper objectMapper;

	ProjectAllocationAuditLogService(AuthzService authzService, ApplicationEventPublisher publisher, ObjectMapper objectMapper) {
		this.authzService = authzService;
		this.publisher = publisher;
		this.objectMapper = objectMapper;
	}

	@EventListener
	void onProjectAllocationCreatedEvent(ProjectAllocationCreatedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.resourceId(event.projectAllocation.id.id)
			.originator(currentAuthNUser)
			.action(Action.CREATE)
			.operationCategory(Operation.PROJECT_ALLOCATION)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.projectAllocation.name)
			.dataJson(toJson(event.projectAllocation))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onProjectAllocationRemovedEvent(ProjectAllocationRemovedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.resourceId(event.projectAllocation.id.id)
			.originator(currentAuthNUser)
			.action(Action.DELETE)
			.operationCategory(Operation.PROJECT_ALLOCATION)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.projectAllocation.name)
			.dataJson(toJson(event.projectAllocation))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onProjectAllocationUpdatedEvent(ProjectAllocationUpdatedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.resourceId(event.newProjectAllocation.id.id)
			.originator(currentAuthNUser)
			.action(Action.UPDATE)
			.operationCategory(Operation.PROJECT_ALLOCATION)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.newProjectAllocation.name)
			.dataJson(toJsonDiff(event.oldProjectAllocation, event.newProjectAllocation))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	private String toJson(ProjectAllocation projectAllocation) {
		try {
			return objectMapper.writeValueAsString(projectAllocation);
		} catch (JsonProcessingException e) {
			throw new AuditLogException(String.format("Project allocation with id %s cannot be parse", projectAllocation.id), e);
		}
	}

	private String toJsonDiff(ProjectAllocation oldProjectAllocation, ProjectAllocation newProjectAllocation) {
		Map<String, Object> diffs = new HashMap<>();
		if(!Objects.equals(oldProjectAllocation.name, newProjectAllocation.name))
			diffs.put("name", newProjectAllocation.name);
		if(!Objects.equals(oldProjectAllocation.amount, newProjectAllocation.amount))
			diffs.put("amount", newProjectAllocation.amount);

		try {
			return objectMapper.writeValueAsString(diffs);
		} catch (JsonProcessingException e) {
			throw new AuditLogException(String.format("Project allocation with id %s cannot be parse", oldProjectAllocation.id), e);
		}
	}
}
