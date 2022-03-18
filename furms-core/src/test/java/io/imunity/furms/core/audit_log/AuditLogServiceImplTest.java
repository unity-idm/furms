/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.audit_log;

import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.AuditLogEvent;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.spi.audit_log.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {
	@Mock
	private AuditLogRepository repository;

	@InjectMocks
	private AuditLogServiceImpl auditLogService;

	@Test
	void shouldCreate() {
		AuditLog auditLog = AuditLog.builder().build();
		auditLogService.onAuditLogEvent(new AuditLogEvent(auditLog));
		Mockito.verify(repository).create(auditLog);
	}

	@Test
	void shouldFind() {
		LocalDateTime start = LocalDate.now().atStartOfDay();
		LocalDateTime stop = LocalDate.now().atStartOfDay().plusDays(2);
		Set<FURMSUser> users = Set.of(FURMSUser.builder()
			.email("email")
			.build());
		auditLogService.findBy(start.atZone(ZoneOffset.UTC), stop.atZone(ZoneOffset.UTC), users, Set.of(1), Set.of(2));
		Mockito.verify(repository).findBy(start, stop, users, Set.of(1), Set.of(2));
	}
}
