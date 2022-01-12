/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.ssh_keys;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.AuditLogEvent;
import io.imunity.furms.domain.audit_log.AuditLogException;
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
import java.util.Objects;

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
		Map<String, Object> json = new HashMap<>();
		json.put("id", sshKey.id);
		json.put("name", sshKey.name);
		json.put("createTime", sshKey.createTime);
		json.put("updateTime", sshKey.updateTime);
		json.put("sites", sshKey.sites);

		try {
			return objectMapper.writeValueAsString(json);
		} catch (JsonProcessingException e) {
			throw new AuditLogException(String.format("SSH Key with id %s cannot be parse", sshKey.id), e);
		}
	}

	private String toJsonDiff(SSHKey oldSshKey, SSHKey newSshKey) {
		Map<String, Object> diffs = new HashMap<>();
		if(!Objects.equals(oldSshKey.name, newSshKey.name))
			diffs.put("name", newSshKey.name);
		if(!Objects.equals(oldSshKey.value, newSshKey.value))
			diffs.put("value", "changed");
		if(!Objects.equals(oldSshKey.ownerId, newSshKey.ownerId))
			diffs.put("ownerId", newSshKey.ownerId);
		if(!Objects.equals(oldSshKey.sites, newSshKey.sites))
			diffs.put("sites", newSshKey.sites);

		try {
			return objectMapper.writeValueAsString(diffs);
		} catch (JsonProcessingException e) {
			throw new AuditLogException(String.format("SSH Key with id %s cannot be parse", oldSshKey.id), e);
		}
	}
}
