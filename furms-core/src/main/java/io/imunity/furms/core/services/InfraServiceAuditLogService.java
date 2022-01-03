/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.AuditLogEvent;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.services.InfraServiceCreatedEvent;
import io.imunity.furms.domain.services.InfraServiceRemovedEvent;
import io.imunity.furms.domain.services.InfraServiceUpdatedEvent;
import io.imunity.furms.domain.users.FURMSUser;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;

@Service
class InfraServiceAuditLogService {

	private final AuthzService authzService;
	private final ApplicationEventPublisher publisher;
	private final ObjectMapper objectMapper;

	InfraServiceAuditLogService(AuthzService authzService, ApplicationEventPublisher publisher, ObjectMapper objectMapper) {
		this.authzService = authzService;
		this.publisher = publisher;
		this.objectMapper = objectMapper;
	}

	@EventListener
	void onCreateServiceEvent(InfraServiceCreatedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.originator(currentAuthNUser)
			.action(Action.CREATE)
			.operationCategory(Operation.SERVICES_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.infraService.name)
			.dataJson(toJson(event.infraService))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onRemoveServiceEvent(InfraServiceRemovedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.originator(currentAuthNUser)
			.action(Action.DELETE)
			.operationCategory(Operation.SERVICES_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.infraService.name)
			.dataJson(toJson(event.infraService))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onUpdateServiceEvent(InfraServiceUpdatedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.originator(currentAuthNUser)
			.action(Action.UPDATE)
			.operationCategory(Operation.SERVICES_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.newInfraService.name)
			.dataJson(toJsonDiff(event.oldInfraService, event.newInfraService))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	private String toJson(InfraService infraService) {
		try {
			return objectMapper.writeValueAsString(infraService);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private String toJsonDiff(InfraService oldService, InfraService newService) {
		Map<String, Object> diffs = new HashMap<>();
		if(!oldService.name.equals(newService.name))
			diffs.put("name", newService.name);
		if(!oldService.description.equals(newService.description))
			diffs.put("description", newService.description);
		if(!oldService.policyId.equals(newService.policyId))
			diffs.put("policyId", newService.policyId);

		try {
			return objectMapper.writeValueAsString(diffs);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
