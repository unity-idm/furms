/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.community_allocation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.AuditLogEvent;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationCreatedEvent;
import io.imunity.furms.domain.community_allocation.CommunityAllocationRemovedEvent;
import io.imunity.furms.domain.community_allocation.CommunityAllocationUpdatedEvent;
import io.imunity.furms.domain.users.FURMSUser;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;

@Service
class CommunityAllocationAuditLogService {

	private final AuthzService authzService;
	private final ApplicationEventPublisher publisher;
	private final ObjectMapper objectMapper;

	CommunityAllocationAuditLogService(AuthzService authzService, ApplicationEventPublisher publisher, ObjectMapper objectMapper) {
		this.authzService = authzService;
		this.publisher = publisher;
		this.objectMapper = objectMapper;
	}

	@EventListener
	void onCommunityAllocationCreatedEvent(CommunityAllocationCreatedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.originator(currentAuthNUser)
			.action(Action.CREATE)
			.operationCategory(Operation.COMMUNITY_ALLOCATION)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.communityAllocation.name)
			.dataJson(toJson(event.communityAllocation))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onCommunityAllocationRemovedEvent(CommunityAllocationRemovedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.originator(currentAuthNUser)
			.action(Action.DELETE)
			.operationCategory(Operation.COMMUNITY_ALLOCATION)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.communityAllocation.name)
			.dataJson(toJson(event.communityAllocation))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onCommunityAllocationUpdatedEvent(CommunityAllocationUpdatedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.originator(currentAuthNUser)
			.action(Action.UPDATE)
			.operationCategory(Operation.COMMUNITY_ALLOCATION)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.newCommunityAllocation.name)
			.dataJson(toJsonDiff(event.oldCommunityAllocation, event.newCommunityAllocation))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	private String toJson(CommunityAllocation communityAllocation) {
		try {
			return objectMapper.writeValueAsString(communityAllocation);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private String toJsonDiff(CommunityAllocation oldCommunityAllocation, CommunityAllocation newCommunityAllocation) {
		Map<String, Object> diffs = new HashMap<>();
		if(!oldCommunityAllocation.name.equals(newCommunityAllocation.name))
			diffs.put("name", newCommunityAllocation.name);
		if(!oldCommunityAllocation.amount.equals(newCommunityAllocation.amount))
			diffs.put("amount", newCommunityAllocation.amount);

		try {
			return objectMapper.writeValueAsString(diffs);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
