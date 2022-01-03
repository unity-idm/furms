/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.audit_log;

import io.imunity.furms.api.audit_log.AuditLogService;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.AuditLogEvent;
import io.imunity.furms.spi.audit_log.AuditLogRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Set;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;

@Service
class AuditLogServiceImpl implements AuditLogService {
	private final AuditLogRepository repository;

	AuditLogServiceImpl(AuditLogRepository repository) {
		this.repository = repository;
	}

	@EventListener
	void onAuditLogEvent(AuditLogEvent event) {
		repository.create(event.auditLog);
	}

	@Override
	public Set<AuditLog> findBy(ZonedDateTime from, ZonedDateTime to, Set<String> originatorIds, Set<Integer> actionIds, Set<Integer> operationIds, String subject) {
		return repository.findBy(convertToUTCTime(from), convertToUTCTime(to), originatorIds, actionIds, operationIds, subject);
	}
}
