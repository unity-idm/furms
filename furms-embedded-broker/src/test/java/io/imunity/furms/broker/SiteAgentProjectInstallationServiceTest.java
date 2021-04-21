/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.broker;

import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.site.api.message_resolver.ProjectInstallationMessageResolver;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectInstallationService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.ACK;
import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.DONE;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
class SiteAgentProjectInstallationServiceTest {
	@Autowired
	private SiteAgentProjectInstallationService siteAgentProjectInstallationService;
	@Autowired
	private RabbitListenerEndpointRegistry endpointRegistry;
	@MockBean
	private ProjectInstallationMessageResolver projectInstallationService;

	@Test
	void shouldInstallProject() throws ExecutionException, InterruptedException {
		AbstractMessageListenerContainer container = (AbstractMessageListenerContainer)endpointRegistry.getListenerContainer("FURMS_LISTENER");
		container.addQueueNames("mock-pub-site");
		CorrelationId correlationId = new CorrelationId();
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
		verify(projectInstallationService, timeout(10000)).updateStatus(correlationId, DONE);
	}
}
