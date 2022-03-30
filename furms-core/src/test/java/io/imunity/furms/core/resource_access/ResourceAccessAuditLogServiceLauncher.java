/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_access;

import io.imunity.furms.core.audit_log.AuditLogPackageTestExposer;
import io.imunity.furms.core.post_commit.PostCommitRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication(scanBasePackageClasses = {ResourceAccessAuditLogService.class, AuditLogPackageTestExposer.class,
	PostCommitRunner.class})
class ResourceAccessAuditLogServiceLauncher {
}
