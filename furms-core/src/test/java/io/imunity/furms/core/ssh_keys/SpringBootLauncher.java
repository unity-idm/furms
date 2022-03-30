/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.ssh_keys;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.core.MockedTransactionManager;
import io.imunity.furms.core.audit_log.AuditLogPackageTestExposer;
import io.imunity.furms.core.post_commit.PostCommitRunner;
import io.imunity.furms.site.api.ssh_keys.SiteAgentSSHKeyOperationService;
import io.imunity.furms.spi.audit_log.AuditLogRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.ssh_key_history.SSHKeyHistoryRepository;
import io.imunity.furms.spi.ssh_key_installation.InstalledSSHKeyRepository;
import io.imunity.furms.spi.ssh_key_operation.SSHKeyOperationRepository;
import io.imunity.furms.spi.ssh_keys.SSHKeyRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication(scanBasePackageClasses = {SSHKeyAuditLogService.class, AuditLogPackageTestExposer.class, PostCommitRunner.class})
class SpringBootLauncher {

	@MockBean
	private SSHKeyRepository repository;
	@MockBean
	private SiteRepository siteRepository;
	@MockBean
	private UsersDAO usersDAO;
	@MockBean
	private SSHKeyOperationRepository sshKeyOperationRepository;
	@MockBean
	private SiteAgentSSHKeyOperationService siteAgentSSHKeyInstallationService;
	@MockBean
	private SSHKeyHistoryRepository sshKeyHistoryRepository;
	@MockBean
	private UserOperationRepository userOperationRepository;
	@MockBean
	private InstalledSSHKeyRepository installedSSHKeyRepository;;
	@MockBean
	private TaskScheduler taskScheduler;

	@MockBean
	private AuthzService authzService;
	@MockBean
	private ObjectMapper objectMapper;
	@Autowired
	private ApplicationEventPublisher publisher;
	@MockBean
	private AuditLogRepository auditLogRepository;

	@Bean
	MockedTransactionManager transactionManager() {
		return new MockedTransactionManager();
	}
}
