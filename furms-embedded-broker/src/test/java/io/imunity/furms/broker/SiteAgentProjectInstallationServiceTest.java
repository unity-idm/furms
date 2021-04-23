/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.broker;

import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.ACK;
import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.DONE;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.site.api.message_resolver.ProjectInstallationMessageResolver;
import io.imunity.furms.site.api.message_resolver.SSHKeyOperationMessageResolver;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectInstallationService;

@SpringBootTest
class SiteAgentProjectInstallationServiceTest {
	@Autowired
	private SiteAgentProjectInstallationService siteAgentProjectInstallationService;
	@MockBean
	private ProjectInstallationMessageResolver projectInstallationService;
	@MockBean
	private SSHKeyOperationMessageResolver sshKeyOperationService;

	@Test
	void shouldInstallProject() throws ExecutionException, InterruptedException {
		CorrelationId correlationId = siteAgentProjectInstallationService.installProject(ProjectInstallation.builder()
			.id("id")
			.siteExternalId("mock")
			.leader(FURMSUser.builder()
				.id(new PersistentId("id"))
				.email("email")
				.build())
			.build()
		);

		verify(projectInstallationService, timeout(10000)).updateStatus(correlationId, ACK);
		verify(projectInstallationService, timeout(10000)).updateStatus(correlationId, DONE);
	}
}
