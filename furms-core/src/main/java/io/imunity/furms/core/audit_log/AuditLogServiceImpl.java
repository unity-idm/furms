/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.audit_log;

import io.imunity.furms.api.audit_log.AuditLogService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.AuditLogEvent;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.spi.audit_log.AuditLogRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.AUDIT_LOG_MANAGEMENT;
import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;
import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;

@Service
class AuditLogServiceImpl implements AuditLogService, AuditLogListener {
	private final AuditLogRepository repository;

	AuditLogServiceImpl(AuditLogRepository repository) {
		this.repository = repository;
	}

	@Async
	@Override
	@EventListener
	public void onAuditLogEvent(AuditLogEvent event) {
		repository.create(event.auditLog);
	}

	@Override
	@FurmsAuthorize(capability = AUDIT_LOG_MANAGEMENT, resourceType = APP_LEVEL)
	public Set<AuditLog> findBy(ZonedDateTime from, ZonedDateTime to, Set<FURMSUser> originators, Set<Integer> actionIds, Set<Integer> operationIds, String subject) {
		return repository.findBy(convertToUTCTime(from), convertToUTCTime(to), originators, actionIds, operationIds, subject);
	}
}
