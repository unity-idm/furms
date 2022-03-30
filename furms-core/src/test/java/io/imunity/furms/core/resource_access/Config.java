/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_access;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.resource_access.ResourceAccessService;
import io.imunity.furms.core.MockedTransactionManager;
import io.imunity.furms.core.policy_documents.PolicyNotificationService;
import io.imunity.furms.core.post_commit.PostCommitRunner;
import io.imunity.furms.core.user_site_access.UserSiteAccessInnerService;
import io.imunity.furms.site.api.site_agent.SiteAgentResourceAccessService;
import io.imunity.furms.spi.audit_log.AuditLogRepository;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
class Config {
	@MockBean
	private SiteAgentResourceAccessService siteAgentResourceAccessService;
	@MockBean
	private ResourceAccessRepository repository;
	@MockBean
	private UserOperationRepository userRepository;
	@MockBean
	private PolicyNotificationService policyNotificationService;
	@MockBean
	private UserSiteAccessInnerService userSiteAccessInnerService;
	@MockBean
	private UsersDAO usersDAO;
	@MockBean
	private AuthzService authzService;
	@MockBean
	private AuditLogRepository auditLogRepository;
	@Autowired
	private ApplicationEventPublisher publisher;
	@Autowired
	private PostCommitRunner postCommitRunner;

	@Bean
	@Primary
	ResourceAccessService resourceAccessService() {
		return new ResourceAccessServiceImpl(siteAgentResourceAccessService, repository, userRepository, authzService,
			userSiteAccessInnerService, policyNotificationService, publisher, usersDAO, postCommitRunner);
	}

	@Bean
	MockedTransactionManager transactionManager() {
		return new MockedTransactionManager();
	}
}
