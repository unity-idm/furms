/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation_installation;

import io.imunity.furms.core.MockedTransactionManager;
import io.imunity.furms.core.utils.AfterCommitLauncher;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectAllocationInstallationService;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.project_allocation_installation.ProjectAllocationInstallationRepository;
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
	private ProjectAllocationInstallationRepository repository;
	@MockBean
	private ProjectAllocationRepository projectAllocationRepository;
	@MockBean
	private SiteAgentProjectAllocationInstallationService siteAgentProjectAllocationInstallationService;
	@Autowired
	private ApplicationEventPublisher publisher;

	@Bean
	MockedTransactionManager transactionManager() {
		return new MockedTransactionManager();
	}

	@Bean
	ProjectAllocationInstallationService service() {
		return new ProjectAllocationInstallationService(repository, projectAllocationRepository,
			siteAgentProjectAllocationInstallationService, publisher);
	}
}
