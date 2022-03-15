/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.core.MockedTransactionManager;
import io.imunity.furms.core.utils.AfterCommitLauncher;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectOperationService;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.project_installation.ProjectOperationRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication(scanBasePackageClasses = AfterCommitLauncher.class)
class SpringBootLauncher {

	@MockBean
	private ProjectOperationRepository repository;
	@MockBean
	private SiteAgentProjectOperationService siteAgentProjectOperationService;
	@MockBean
	private UsersDAO usersDAO;
	@MockBean
	private SiteRepository siteRepository;
	@MockBean
	private CommunityRepository communityRepository;
	@Autowired
	private ApplicationEventPublisher publisher;

	@Bean
	MockedTransactionManager transactionManager() {
		return new MockedTransactionManager();
	}

	@Bean
	ProjectInstallationServiceImpl service() {
		return new ProjectInstallationServiceImpl(repository, siteAgentProjectOperationService, usersDAO,
			siteRepository, communityRepository, publisher);
	}
}
