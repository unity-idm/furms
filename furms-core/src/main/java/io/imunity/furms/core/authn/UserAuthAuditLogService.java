/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.authn;

import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.AuditLogEvent;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.authn.UserLoggedInEvent;
import io.imunity.furms.domain.authn.UserLoggedOutEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;

@Service
class UserAuthAuditLogService {

	private final ApplicationEventPublisher publisher;

	UserAuthAuditLogService(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	@EventListener
	void onUserLoggedInEvent(UserLoggedInEvent event) {
		AuditLog auditLog = AuditLog.builder()
			.originator(event.user)
			.action(Action.LOGIN)
			.operationCategory(Operation.AUTHENTICATION)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.user)
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onUserLoggedOutEvent(UserLoggedOutEvent event) {
		AuditLog auditLog = AuditLog.builder()
			.originator(event.user)
			.action(Action.LOGOUT)
			.operationCategory(Operation.AUTHENTICATION)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.user)
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}
}
