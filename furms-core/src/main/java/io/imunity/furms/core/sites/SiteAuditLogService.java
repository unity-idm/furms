/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.sites;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.AuditLogEvent;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteCreatedEvent;
import io.imunity.furms.domain.sites.SiteRemovedEvent;
import io.imunity.furms.domain.sites.SiteUpdatedEvent;
import io.imunity.furms.domain.users.FURMSUser;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;

@Service
class SiteAuditLogService {

	private final AuthzService authzService;
	private final ApplicationEventPublisher publisher;
	private final ObjectMapper objectMapper;

	SiteAuditLogService(AuthzService authzService, ApplicationEventPublisher publisher, ObjectMapper objectMapper) {
		this.authzService = authzService;
		this.publisher = publisher;
		this.objectMapper = objectMapper;
	}

	@EventListener
	void onSiteCreatedEvent(SiteCreatedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.originator(currentAuthNUser)
			.action(Action.CREATE)
			.operationCategory(Operation.SITES_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.site.getName())
			.dataJson(toJson(event.site))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onSiteRemovedEvent(SiteRemovedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.originator(currentAuthNUser)
			.action(Action.DELETE)
			.operationCategory(Operation.SITES_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.site.getName())
			.dataJson(toJson(event.site))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onSiteUpdatedEvent(SiteUpdatedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.originator(currentAuthNUser)
			.action(Action.UPDATE)
			.operationCategory(Operation.SITES_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.newSite.getName())
			.dataJson(toJsonDiff(event.oldSite, event.newSite))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	private String toJson(Site site) {
		JsonNode jsonNodes = objectMapper.valueToTree(site);
		((ObjectNode)jsonNodes).remove("logo");
		((ObjectNode)jsonNodes).remove("sshKeyHistoryLength");
		return jsonNodes.toString();
	}

	private String toJsonDiff(Site oldSite, Site newSite) {
		Map<String, Object> diffs = new HashMap<>();
		if(!oldSite.getName().equals(newSite.getName()))
			diffs.put("name", newSite.getName());
		if(!oldSite.getConnectionInfo().equals(newSite.getConnectionInfo()))
			diffs.put("connectionInfo", newSite.getConnectionInfo());
		if(!oldSite.isSshKeyFromOptionMandatory().equals(newSite.isSshKeyFromOptionMandatory()))
			diffs.put("sshKeyFromOptionMandatory", newSite.isSshKeyFromOptionMandatory());
		if(!oldSite.getPolicyId().equals(newSite.getPolicyId()))
			diffs.put("policyId", newSite.getPolicyId());
		if(!oldSite.getLogo().equals(newSite.getLogo()))
			diffs.put("logo", "changed");

		try {
			return objectMapper.writeValueAsString(diffs);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
