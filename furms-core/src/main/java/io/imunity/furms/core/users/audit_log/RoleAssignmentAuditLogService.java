/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.users.audit_log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.validation.exceptions.UserNotPresentException;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.AuditLogEvent;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.UserRoleGrantedByInvitationEvent;
import io.imunity.furms.domain.users.UserRoleGrantedByRegistrationEvent;
import io.imunity.furms.domain.users.UserRoleGrantedEvent;
import io.imunity.furms.domain.users.UserRoleRevokedEvent;
import io.imunity.furms.spi.users.UsersDAO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
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
	void onUserRoleRevokedEvent(UserRoleRevokedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		FURMSUser user = usersDAO.findById(event.id).get();
		AuditLog auditLog = AuditLog.builder()
			.resourceId(user.id.get().id)
			.originator(currentAuthNUser)
			.action(Action.REVOKE)
			.operationCategory(Operation.ROLE_ASSIGNMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(user)
			.dataJson(toJson(event.role, event.resourceId, event.resourceName))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onUserRoleGrantedByRegistrationEvent(UserRoleGrantedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		FURMSUser user = usersDAO.findById(event.id).get();
		AuditLog auditLog = AuditLog.builder()
			.resourceId(user.id.get().id)
			.originator(currentAuthNUser)
			.action(Action.GRANT)
			.operationCategory(Operation.ROLE_ASSIGNMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(user)
			.dataJson(toJson(event.role, event.resourceId, event.resourceName))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onUserRoleGrantedByRegistrationEvent(UserRoleGrantedByRegistrationEvent event) {
		List<FURMSUser> allUsers = usersDAO.getAllUsers();
		FURMSUser originator = findUserByEmail(allUsers, event.originatorEmail);
		FURMSUser currentUser = findUserByEmail(allUsers, event.userEmail);
		AuditLog auditLog = AuditLog.builder()
			.resourceId(currentUser.id.get().id)
			.originator(originator)
			.action(Action.GRANT)
			.operationCategory(Operation.ROLE_ASSIGNMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(currentUser)
			.dataJson(toJson(event.role, event.resourceId, event.resourceName))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onUserRoleGrantedByInvitationEvent(UserRoleGrantedByInvitationEvent event) {
		List<FURMSUser> allUsers = usersDAO.getAllUsers();
		FURMSUser originator = findUserByEmail(allUsers, event.originatorEmail);
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.resourceId(currentAuthNUser.id.get().id)
			.originator(originator)
			.action(Action.GRANT)
			.operationCategory(Operation.ROLE_ASSIGNMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(currentAuthNUser)
			.dataJson(toJson(event.role, event.resourceId, event.resourceName))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	private FURMSUser findUserByEmail(List<FURMSUser> allUsers, String email){
		return allUsers.stream()
			.filter(usr -> usr.email.equals(email))
			.findAny()
			.orElseThrow(() -> new UserNotPresentException(String.format("User with email %s doesn't exist", email)));
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
