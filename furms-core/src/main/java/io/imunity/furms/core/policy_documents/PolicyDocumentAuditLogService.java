/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.policy_documents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.AuditLogEvent;
import io.imunity.furms.domain.audit_log.AuditLogException;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyDocumentCreatedEvent;
import io.imunity.furms.domain.policy_documents.PolicyDocumentRemovedEvent;
import io.imunity.furms.domain.policy_documents.PolicyDocumentUpdatedEvent;
import io.imunity.furms.domain.policy_documents.UserAcceptedPolicyEvent;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.spi.users.UsersDAO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;

@Service
class PolicyDocumentAuditLogService {

	private final AuthzService authzService;
	private final UsersDAO usersDAO;
	private final ApplicationEventPublisher publisher;
	private final ObjectMapper objectMapper;

	PolicyDocumentAuditLogService(AuthzService authzService, UsersDAO usersDAO, ApplicationEventPublisher publisher, ObjectMapper objectMapper) {
		this.authzService = authzService;
		this.usersDAO = usersDAO;
		this.publisher = publisher;
		this.objectMapper = objectMapper;
	}

	@EventListener
	void onPolicyDocumentCreatedEvent(PolicyDocumentCreatedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.resourceId(event.policyDocument.id.id)
			.originator(currentAuthNUser)
			.action(Action.CREATE)
			.operationCategory(Operation.POLICY_DOCUMENTS_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.policyDocument.name)
			.dataJson(toJson(event.policyDocument))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onPolicyDocumentRemovedEvent(PolicyDocumentRemovedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.resourceId(event.policyDocument.id.id)
			.originator(currentAuthNUser)
			.action(Action.DELETE)
			.operationCategory(Operation.POLICY_DOCUMENTS_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.policyDocument.name)
			.dataJson(toJson(event.policyDocument))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onPolicyDocumentUpdatedEvent(PolicyDocumentUpdatedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.resourceId(event.newPolicyDocument.id.id)
			.originator(currentAuthNUser)
			.action(Action.UPDATE)
			.operationCategory(Operation.POLICY_DOCUMENTS_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.newPolicyDocument.name)
			.dataJson(toJsonDiff(event.oldPolicyDocument, event.newPolicyDocument))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onUserAcceptedPolicyEvent(UserAcceptedPolicyEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		FURMSUser user = usersDAO.findById(event.userId).get();
		AuditLog auditLog = AuditLog.builder()
			.resourceId(user.id.get().id)
			.originator(currentAuthNUser)
			.action(Action.ACCEPT)
			.operationCategory(Operation.POLICY_ACCEPTANCE)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(user)
			.dataJson(toJson(event.policyAcceptance))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	private String toJson(PolicyAcceptance policyAcceptance) {
		Map<String, Object> json = new HashMap<>();
		json.put("id", policyAcceptance.policyDocumentId.asRawString());
		json.put("policyDocumentRevision", policyAcceptance.policyDocumentRevision);
		json.put("acceptanceStatus", policyAcceptance.acceptanceStatus);
		json.put("decisionTs", policyAcceptance.decisionTs);

		try {
			return objectMapper.writeValueAsString(json);
		} catch (JsonProcessingException e) {
			throw new AuditLogException(String.format("Policy acceptance with id %s cannot be parse", policyAcceptance.policyDocumentId), e);
		}
	}

	private String toJson(PolicyDocument policyDocument) {
		Map<String, Object> json = new HashMap<>();
		json.put("id", policyDocument.id.asRawString());
		json.put("name", policyDocument.name);
		if (policyDocument.siteId != null)
			json.put("siteId", policyDocument.siteId.asRawString());
		json.put("workflow", policyDocument.workflow);
		json.put("revision", policyDocument.revision);
		json.put("contentType", policyDocument.contentType);

		try {
			return objectMapper.writeValueAsString(json);
		} catch (JsonProcessingException e) {
			throw new AuditLogException(String.format("Policy document with id %s cannot be parse", policyDocument.id), e);
		}
	}

	private String toJsonDiff(PolicyDocument oldPolicyDocument, PolicyDocument newPolicyDocument) {
		Map<String, Object> diffs = new HashMap<>();
		if(!Objects.equals(oldPolicyDocument.name, newPolicyDocument.name))
			diffs.put("name", newPolicyDocument.name);
		if(!Objects.equals(oldPolicyDocument.workflow, newPolicyDocument.workflow))
			diffs.put("workflow", newPolicyDocument.workflow);
		if(oldPolicyDocument.revision != newPolicyDocument.revision)
			diffs.put("revision", newPolicyDocument.revision);
		if(!Objects.equals(oldPolicyDocument.contentType, newPolicyDocument.contentType))
			diffs.put("contentType", newPolicyDocument.contentType);
		if(!Objects.equals(oldPolicyDocument.htmlText, newPolicyDocument.htmlText))
			diffs.put("htmlText", "CHANGED");
		if(!Objects.equals(oldPolicyDocument.policyFile, newPolicyDocument.policyFile))
			diffs.put("policyFile", "CHANGED");

		try {
			return objectMapper.writeValueAsString(diffs);
		} catch (JsonProcessingException e) {
			throw new AuditLogException(String.format("Policy document with id %s cannot be parse", oldPolicyDocument.id), e);
		}
	}
}
