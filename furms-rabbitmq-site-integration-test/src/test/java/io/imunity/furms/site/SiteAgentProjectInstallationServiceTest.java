/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site;

import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.rabbitmq.site.client.SiteAgentListenerConnector;
import io.imunity.furms.site.api.SiteExternalIdsResolver;
import io.imunity.furms.site.api.message_resolver.ProjectInstallationMessageResolver;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectInstallationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.ACK;
import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.INSTALLED;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
class SiteAgentProjectInstallationServiceTest {
	@Autowired
	private SiteAgentProjectInstallationService siteAgentProjectInstallationService;
	@Autowired
	private SiteAgentListenerConnector siteAgentListenerConnector;
	@MockBean
	private ProjectInstallationMessageResolver projectInstallationService;
	@MockBean
	private SiteExternalIdsResolver siteExternalIdsResolver;

	@BeforeEach
	void init(){
		siteAgentListenerConnector.connectListenerToQueue( "mock-site-pub");
	}

	@Test
	void shouldInstallProject() throws ExecutionException, InterruptedException {
		CorrelationId correlationId = CorrelationId.randomID();
		ProjectInstallation projectInstallation = ProjectInstallation.builder()
			.id("id")
			.siteExternalId("mock")
			.validityStart(LocalDateTime.now())
			.validityEnd(LocalDateTime.now())
			.leader(FURMSUser.builder()
				.id(new PersistentId("id"))
				.email("email")
				.build())
			.build();
		siteAgentProjectInstallationService.installProject(correlationId, projectInstallation);

		verify(projectInstallationService, timeout(10000)).updateStatus(correlationId, ACK);
		verify(projectInstallationService, timeout(10000)).updateStatus(correlationId, INSTALLED);
	}
}