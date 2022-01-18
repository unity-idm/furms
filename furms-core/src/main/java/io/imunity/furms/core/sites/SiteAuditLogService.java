/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.sites;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.AuditLogEvent;
import io.imunity.furms.domain.audit_log.AuditLogException;
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
import java.util.Objects;

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
		Map<String, Object> json = new HashMap<>();
		json.put("id", site.getId());
		json.put("name", site.getName());
		json.put("connectionInfo", site.getConnectionInfo());
		json.put("sshKeyFromOptionMandatory", site.isSshKeyFromOptionMandatory());
		json.put("policyId", site.getPolicyId());

		try {
			return objectMapper.writeValueAsString(json);
		} catch (JsonProcessingException e) {
			throw new AuditLogException(String.format("Site with id %s cannot be parse", site.getId()), e);
		}
	}

	private String toJsonDiff(Site oldSite, Site newSite) {
		Map<String, Object> diffs = new HashMap<>();
		if(!Objects.equals(oldSite.getName(), newSite.getName()))
			diffs.put("name", newSite.getName());
		if(!Objects.equals(oldSite.getConnectionInfo(), newSite.getConnectionInfo()))
			diffs.put("connectionInfo", newSite.getConnectionInfo());
		if(!Objects.equals(oldSite.isSshKeyFromOptionMandatory(), newSite.isSshKeyFromOptionMandatory()))
			diffs.put("sshKeyFromOptionMandatory", newSite.isSshKeyFromOptionMandatory());
		if(!Objects.equals(oldSite.getPolicyId(), newSite.getPolicyId()))
			diffs.put("policyId", newSite.getPolicyId());
		if(!Objects.equals(oldSite.getLogo(), newSite.getLogo()))
			diffs.put("logo", "changed");

		try {
			return objectMapper.writeValueAsString(diffs);
		} catch (JsonProcessingException e) {
			throw new AuditLogException(String.format("Site with id %s cannot be parse", oldSite.getId()), e);
		}
	}
}
