/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.communites;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.AuditLogEvent;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityCreatedEvent;
import io.imunity.furms.domain.communities.CommunityRemovedEvent;
import io.imunity.furms.domain.communities.CommunityUpdatedEvent;
import io.imunity.furms.domain.users.FURMSUser;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;

@Service
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
		JsonNode jsonNodes = objectMapper.valueToTree(community);
		((ObjectNode)jsonNodes).remove("logo");
		return jsonNodes.toString();
	}

	private String toJsonDiff(Community oldCommunity, Community newCommunity) {
		Map<String, Object> diffs = new HashMap<>();
		if(!oldCommunity.getName().equals(newCommunity.getName()))
			diffs.put("name", newCommunity.getName());
		if(!oldCommunity.getDescription().equals(newCommunity.getDescription()))
			diffs.put("description", newCommunity.getDescription());
		if(!oldCommunity.getLogo().equals(newCommunity.getLogo()))
			diffs.put("logo", "changed");

		try {
			return objectMapper.writeValueAsString(diffs);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
