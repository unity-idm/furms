/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.users;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.AuditLogEvent;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.UserRoleGrantedByRegistrationEvent;
import io.imunity.furms.domain.users.UserRoleGrantedEvent;
import io.imunity.furms.domain.users.UserRoleRevokedEvent;
import io.imunity.furms.spi.users.UsersDAO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;

@Service
class RoleAssignmentAuditLogService {

	private final AuthzService authzService;
	private final UsersDAO usersDAO;
	private final ApplicationEventPublisher publisher;
	private final ObjectMapper objectMapper;

	RoleAssignmentAuditLogService(AuthzService authzService, UsersDAO usersDAO, ApplicationEventPublisher publisher, ObjectMapper objectMapper) {
		this.authzService = authzService;
		this.publisher = publisher;
		this.objectMapper = objectMapper;
		this.usersDAO = usersDAO;
	}

	@EventListener
	void onGenericGroupCreatedEvent(UserRoleRevokedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.originator(currentAuthNUser)
			.action(Action.REVOKE)
			.operationCategory(Operation.ROLE_ASSIGNMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(usersDAO.findById(event.id).get())
			.dataJson(toJson(event.role, event.resourceId, event.resourceName))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onUserRoleGrantedEvent(UserRoleGrantedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.originator(currentAuthNUser)
			.action(Action.GRANT)
			.operationCategory(Operation.ROLE_ASSIGNMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(usersDAO.findById(event.id).get())
			.dataJson(toJson(event.role, event.resourceId, event.resourceName))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onUserRoleGrantedEvent(UserRoleGrantedByRegistrationEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.originator(usersDAO.findById(event.originatorId).get())
			.action(Action.GRANT)
			.operationCategory(Operation.ROLE_ASSIGNMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(currentAuthNUser)
			.dataJson(toJson(event.role, event.resourceId, event.resourceName))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	private String toJson(Role role, ResourceId resourceId, String resourceName) {
		Map<String, Object> diffs = new HashMap<>();
		diffs.put("role", role.name());
		diffs.put("resourceId", resourceId.id);
		diffs.put("resourceType", resourceId.type);
		diffs.put("resourceName", resourceName);

		try {
			return objectMapper.writeValueAsString(diffs);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
