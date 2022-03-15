/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_access;

import io.imunity.furms.core.audit_log.AuditLogServiceImplTest;
import io.imunity.furms.core.utils.AfterCommitLauncher;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication(scanBasePackageClasses = {ResourceAccessAuditLogService.class, AuditLogServiceImplTest.class,
	AfterCommitLauncher.class})
class ResourceAccessAuditLogServiceLauncher {
}
