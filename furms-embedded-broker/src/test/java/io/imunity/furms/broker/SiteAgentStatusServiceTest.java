/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.broker;

import io.imunity.furms.domain.site_agent.AvailabilityStatus;
import io.imunity.furms.domain.site_agent.PendingJob;
import io.imunity.furms.domain.site_agent.SiteAgentStatus;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.site.api.message_resolver.ProjectAllocationInstallationMessageResolver;
import io.imunity.furms.site.api.message_resolver.ProjectInstallationMessageResolver;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectAllocationInstallationService;
import io.imunity.furms.site.api.site_agent.SiteAgentStatusService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SiteAgentStatusServiceTest {
	@Autowired
	private SiteAgentStatusService siteAgentStatusService;
	@Autowired
	private RabbitListenerEndpointRegistry endpointRegistry;
	@MockBean
	private ProjectInstallationMessageResolver projectInstallationService;
	@MockBean
	private ProjectAllocationInstallationMessageResolver projectAllocationInstallationMessageResolver;
	@Autowired
	private SiteAgentProjectAllocationInstallationService siteAgentProjectAllocationInstallationService;

	@Test
	void shouldReturnOKStatus() throws ExecutionException, InterruptedException {
		AbstractMessageListenerContainer container = (AbstractMessageListenerContainer)endpointRegistry.getListenerContainer("FURMS_LISTENER");
		container.addQueueNames("mock-pub-site");

		PendingJob<SiteAgentStatus> ping = siteAgentStatusService.getStatus(new SiteExternalId("mock"));
		assertThat(ping.jobFuture.get().status).isEqualTo(AvailabilityStatus.AVAILABLE);
	}
}
