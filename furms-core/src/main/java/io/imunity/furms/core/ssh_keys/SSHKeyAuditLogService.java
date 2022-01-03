/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.ssh_keys;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.AuditLogEvent;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.ssh_keys.SSHKeyCreatedEvent;
import io.imunity.furms.domain.ssh_keys.SSHKeyRemovedEvent;
import io.imunity.furms.domain.ssh_keys.SSHKeyUpdatedEvent;
import io.imunity.furms.domain.users.FURMSUser;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;

@Service
class SSHKeyAuditLogService {

	private final AuthzService authzService;
	private final ApplicationEventPublisher publisher;
	private final ObjectMapper objectMapper;

	SSHKeyAuditLogService(AuthzService authzService, ApplicationEventPublisher publisher, ObjectMapper objectMapper) {
		this.authzService = authzService;
		this.publisher = publisher;
		this.objectMapper = objectMapper;
	}

	@EventListener
	void onSSHKeyCreatedEvent(SSHKeyCreatedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.originator(currentAuthNUser)
			.action(Action.CREATE)
			.operationCategory(Operation.SSH_KEYS_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.sshKey.name)
			.dataJson(toJson(event.sshKey))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onSSHKeyRemovedEvent(SSHKeyRemovedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.originator(currentAuthNUser)
			.action(Action.DELETE)
			.operationCategory(Operation.SSH_KEYS_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.sshKey.name)
			.dataJson(toJson(event.sshKey))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onSSHKeyUpdatedEvent(SSHKeyUpdatedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.originator(currentAuthNUser)
			.action(Action.UPDATE)
			.operationCategory(Operation.SSH_KEYS_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.newSSHKey.name)
			.dataJson(toJsonDiff(event.oldSSHKey, event.newSSHKey))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	private String toJson(SSHKey sshKey) {
		JsonNode jsonNodes = objectMapper.valueToTree(sshKey);
		((ObjectNode)jsonNodes).remove("value");
		return jsonNodes.toString();
	}

	private String toJsonDiff(SSHKey oldSshKey, SSHKey newSshKey) {
		Map<String, Object> diffs = new HashMap<>();
		if(!oldSshKey.name.equals(newSshKey.name))
			diffs.put("name", newSshKey.name);
		if(!oldSshKey.value.equals(newSshKey.value))
			diffs.put("value", "changed");
		if(!oldSshKey.ownerId.equals(newSshKey.ownerId))
			diffs.put("ownerId", newSshKey.ownerId);
		if(!oldSshKey.sites.equals(newSshKey.sites))
			diffs.put("sites", newSshKey.sites);

		try {
			return objectMapper.writeValueAsString(diffs);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
