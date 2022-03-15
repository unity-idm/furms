/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.alarms;

import io.imunity.furms.core.MockedTransactionManager;
import io.imunity.furms.core.audit_log.AuditLogServiceImplTest;
import io.imunity.furms.core.utils.AfterCommitLauncher;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication(scanBasePackageClasses = {AlarmAuditLogService.class, AuditLogServiceImplTest.class, AfterCommitLauncher.class})
class SpringBootLauncher {

	@Bean
	MockedTransactionManager transactionManager() {
		return new MockedTransactionManager();
	}
}
