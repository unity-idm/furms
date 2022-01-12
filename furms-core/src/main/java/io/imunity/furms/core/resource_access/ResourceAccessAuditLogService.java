/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_access;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.AuditLogEvent;
import io.imunity.furms.domain.audit_log.AuditLogException;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.resource_access.UserGrantAddedEvent;
import io.imunity.furms.domain.resource_access.UserGrantRemovedEvent;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.spi.users.UsersDAO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;

@Service
class ResourceAccessAuditLogService {

	private final AuthzService authzService;
	private final ApplicationEventPublisher publisher;
	private final UsersDAO usersDAO;
	private final ObjectMapper objectMapper;

	ResourceAccessAuditLogService(AuthzService authzService, ApplicationEventPublisher publisher, ObjectMapper objectMapper, UsersDAO usersDAO) {
		this.authzService = authzService;
		this.publisher = publisher;
		this.objectMapper = objectMapper;
		this.usersDAO = usersDAO;
	}

	@EventListener
	void onUserGrantAddedEvent(UserGrantAddedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.originator(currentAuthNUser)
			.action(Action.GRANT)
			.operationCategory(Operation.PROJECT_RESOURCE_ASSIGNMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(usersDAO.findById(event.grantAccess.fenixUserId).get())
			.dataJson(toJson(event.grantAccess))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onUserGrantRemovedEvent(UserGrantRemovedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.originator(currentAuthNUser)
			.action(Action.REVOKE)
			.operationCategory(Operation.PROJECT_RESOURCE_ASSIGNMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(usersDAO.findById(event.grantAccess.fenixUserId).get())
			.dataJson(toJson(event.grantAccess))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	private String toJson(GrantAccess grantAccess) {
		try {
			return objectMapper.writeValueAsString(grantAccess);
		} catch (JsonProcessingException e) {
			throw new AuditLogException(String.format("Grant access for %s with allocation id %s cannot be parse", grantAccess.fenixUserId, grantAccess.allocationId), e);
		}
	}

}
