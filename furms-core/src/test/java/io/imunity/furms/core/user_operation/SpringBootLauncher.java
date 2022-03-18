/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.user_operation;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.api.ssh_keys.SSHKeyService;
import io.imunity.furms.core.MockedTransactionManager;
import io.imunity.furms.core.post_commit.PostCommitRunner;
import io.imunity.furms.site.api.site_agent.SiteAgentResourceAccessService;
import io.imunity.furms.site.api.site_agent.SiteAgentUserService;
import io.imunity.furms.site.api.status_updater.UserOperationStatusUpdater;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.user_site_access.UserSiteAccessRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication(scanBasePackageClasses = PostCommitRunner.class)
class SpringBootLauncher {

	@MockBean
	private UserOperationRepository repository;
	@MockBean
	private SiteAgentUserService siteAgentUserService;
	@MockBean
	private SiteRepository siteRepository;
	@MockBean
	private UsersDAO usersDAO;
	@MockBean
	private AuthzService authzService;
	@MockBean
	private SiteService siteService;
	@MockBean
	private PolicyDocumentServiceHelper policyService;
	@MockBean
	private SSHKeyService sshKeyService;
	@MockBean
	private ResourceAccessRepository resourceAccessRepository;
	@MockBean
	private UserSiteAccessRepository userSiteAccessRepository;
	@MockBean
	private SiteAgentResourceAccessService siteAgentResourceAccessService;
	@Autowired
	private PostCommitRunner postCommitRunner;

	@Bean
	MockedTransactionManager transactionManager() {
		return new MockedTransactionManager();
	}

	@Bean
	UserOperationService userOperationService() {
		return new UserOperationService(authzService, siteService, repository, siteAgentUserService, usersDAO,
			policyService, sshKeyService, resourceAccessRepository, userSiteAccessRepository, postCommitRunner);
	}

	@Bean
	UserOperationStatusUpdater userOperationStatusUpdater() {
		return new UserOperationStatusUpdaterImpl(siteAgentResourceAccessService, repository,
			resourceAccessRepository, postCommitRunner);
	}
}
