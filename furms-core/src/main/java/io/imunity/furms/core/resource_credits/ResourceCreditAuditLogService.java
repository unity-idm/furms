/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_credits;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.AuditLogEvent;
import io.imunity.furms.domain.audit_log.AuditLogException;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_credits.ResourceCreditCreatedEvent;
import io.imunity.furms.domain.resource_credits.ResourceCreditRemovedEvent;
import io.imunity.furms.domain.resource_credits.ResourceCreditUpdatedEvent;
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
class ResourceCreditAuditLogService {

	private final AuthzService authzService;
	private final ApplicationEventPublisher publisher;
	private final ObjectMapper objectMapper;

	ResourceCreditAuditLogService(AuthzService authzService, ApplicationEventPublisher publisher, ObjectMapper objectMapper) {
		this.authzService = authzService;
		this.publisher = publisher;
		this.objectMapper = objectMapper;
	}

	@EventListener
	void onResourceCreditCreatedEvent(ResourceCreditCreatedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.resourceId(event.resourceCredit.id)
			.originator(currentAuthNUser)
			.action(Action.CREATE)
			.operationCategory(Operation.RESOURCE_CREDIT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.resourceCredit.name)
			.dataJson(toJson(event.resourceCredit))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onResourceCreditRemovedEvent(ResourceCreditRemovedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.resourceId(event.resourceCredit.id)
			.originator(currentAuthNUser)
			.action(Action.DELETE)
			.operationCategory(Operation.RESOURCE_CREDIT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.resourceCredit.name)
			.dataJson(toJson(event.resourceCredit))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onResourceCreditUpdatedEvent(ResourceCreditUpdatedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.resourceId(event.newResourceCredit.id)
			.originator(currentAuthNUser)
			.action(Action.UPDATE)
			.operationCategory(Operation.RESOURCE_CREDIT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.newResourceCredit.name)
			.dataJson(toJsonDiff(event.oldResourceCredit, event.newResourceCredit))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	private String toJson(ResourceCredit resourceCredit) {
		try {
			return objectMapper.writeValueAsString(resourceCredit);
		} catch (JsonProcessingException e) {
			throw new AuditLogException(String.format("Resource credit with id %s cannot be parse", resourceCredit.id), e);
		}
	}

	private String toJsonDiff(ResourceCredit oldResourceCredit, ResourceCredit newResourceCredit) {
		Map<String, Object> diffs = new HashMap<>();
		if(!Objects.equals(oldResourceCredit.name, newResourceCredit.name))
			diffs.put("name", newResourceCredit.name);
		if(!oldResourceCredit.splittable == newResourceCredit.splittable)
			diffs.put("splittable", newResourceCredit.splittable);
		if(!Objects.equals(oldResourceCredit.amount, newResourceCredit.amount))
			diffs.put("amount", newResourceCredit.amount);
		if(!Objects.equals(oldResourceCredit.utcStartTime, newResourceCredit.utcStartTime))
			diffs.put("utcStartTime", newResourceCredit.utcStartTime);
		if(!Objects.equals(oldResourceCredit.utcEndTime, newResourceCredit.utcEndTime))
			diffs.put("utcEndTime", newResourceCredit.utcEndTime);

		try {
			return objectMapper.writeValueAsString(diffs);
		} catch (JsonProcessingException e) {
			throw new AuditLogException(String.format("Resource credit with id %s cannot be parse", oldResourceCredit.id), e);
		}
	}
}
