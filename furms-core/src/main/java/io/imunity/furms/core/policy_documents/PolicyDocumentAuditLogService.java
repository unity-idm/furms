/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.policy_documents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.AuditLogEvent;
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
	void onPolicyDocumentUpdatedEvent(UserAcceptedPolicyEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.originator(currentAuthNUser)
			.action(Action.ACCEPT)
			.operationCategory(Operation.POLICY_DOCUMENTS_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(usersDAO.findById(event.userId).get())
			.dataJson(toJson(event.policyAcceptance))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	private String toJson(PolicyAcceptance policyAcceptance) {
		try {
			return objectMapper.writeValueAsString(policyAcceptance);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private String toJson(PolicyDocument policyDocument) {
		JsonNode jsonNodes = objectMapper.valueToTree(policyDocument);
		((ObjectNode)jsonNodes).remove("htmlText");
		((ObjectNode)jsonNodes).remove("policyFile");
		return jsonNodes.toString();
	}

	private String toJsonDiff(PolicyDocument oldPolicyDocument, PolicyDocument newPolicyDocument) {
		Map<String, Object> diffs = new HashMap<>();
		if(!oldPolicyDocument.name.equals(newPolicyDocument.name))
			diffs.put("name", newPolicyDocument.name);
		if(!oldPolicyDocument.workflow.equals(newPolicyDocument.workflow))
			diffs.put("workflow", newPolicyDocument.workflow);
		if(oldPolicyDocument.revision != newPolicyDocument.revision)
			diffs.put("revision", newPolicyDocument.revision);
		if(!oldPolicyDocument.contentType.equals(newPolicyDocument.contentType))
			diffs.put("contentType", newPolicyDocument.contentType);
		if(!oldPolicyDocument.htmlText.equals(newPolicyDocument.htmlText))
			diffs.put("htmlText", "changed");
		if(!oldPolicyDocument.policyFile.equals(newPolicyDocument.policyFile))
			diffs.put("policyFile", "changed");

		try {
			return objectMapper.writeValueAsString(diffs);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
