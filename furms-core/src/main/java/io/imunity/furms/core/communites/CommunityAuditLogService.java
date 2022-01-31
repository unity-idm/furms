/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.communites;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.AuditLogEvent;
import io.imunity.furms.domain.audit_log.AuditLogException;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityCreatedEvent;
import io.imunity.furms.domain.communities.CommunityRemovedEvent;
import io.imunity.furms.domain.communities.CommunityUpdatedEvent;
import io.imunity.furms.domain.users.FURMSUser;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;

@Component
class CommunityAuditLogService {

	private final AuthzService authzService;
	private final ApplicationEventPublisher publisher;
	private final ObjectMapper objectMapper;

	CommunityAuditLogService(AuthzService authzService, ApplicationEventPublisher publisher, ObjectMapper objectMapper) {
		this.authzService = authzService;
		this.publisher = publisher;
		this.objectMapper = objectMapper;
	}

	@EventListener
	void onCommunityCreatedEvent(CommunityCreatedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.resourceId(event.community.getId())
			.originator(currentAuthNUser)
			.action(Action.CREATE)
			.operationCategory(Operation.COMMUNITIES_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.community.getName())
			.dataJson(toJson(event.community))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onCommunityRemovedEvent(CommunityRemovedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.resourceId(event.community.getId())
			.originator(currentAuthNUser)
			.action(Action.DELETE)
			.operationCategory(Operation.COMMUNITIES_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.community.getName())
			.dataJson(toJson(event.community))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onCommunityUpdatedEvent(CommunityUpdatedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.resourceId(event.newCommunity.getId())
			.originator(currentAuthNUser)
			.action(Action.UPDATE)
			.operationCategory(Operation.COMMUNITIES_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.newCommunity.getName())
			.dataJson(toJsonDiff(event.oldCommunity, event.newCommunity))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	private String toJson(Community community) {
		Map<String, Object> json = new HashMap<>();
		json.put("id", community.getId());
		json.put("name", community.getName());
		json.put("description", community.getDescription());

		try {
			return objectMapper.writeValueAsString(json);
		} catch (JsonProcessingException e) {
			throw new AuditLogException(String.format("Community with id %s cannot be parse", community.getId()), e);
		}
	}

	private String toJsonDiff(Community oldCommunity, Community newCommunity) {
		Map<String, Object> diffs = new HashMap<>();
		if(!Objects.equals(oldCommunity.getName(), newCommunity.getName()))
			diffs.put("name", newCommunity.getName());
		if(!Objects.equals(oldCommunity.getDescription(), newCommunity.getDescription()))
			diffs.put("description", newCommunity.getDescription());
		if(!Objects.equals(oldCommunity.getLogo(), newCommunity.getLogo()))
			diffs.put("logo", "CHANGED");

		try {
			return objectMapper.writeValueAsString(diffs);
		} catch (JsonProcessingException e) {
			throw new AuditLogException(String.format("Community with id %s cannot be parse", oldCommunity.getId()), e);
		}
	}
}
