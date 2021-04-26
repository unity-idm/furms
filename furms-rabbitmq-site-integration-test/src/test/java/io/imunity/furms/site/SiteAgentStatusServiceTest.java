/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site;

import io.imunity.furms.domain.site_agent.AvailabilityStatus;
import io.imunity.furms.domain.site_agent.PendingJob;
import io.imunity.furms.domain.site_agent.SiteAgentStatus;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.rabbitmq.site.client.SiteAgentListenerConnector;
import io.imunity.furms.site.api.SiteExternalIdsResolver;
import io.imunity.furms.site.api.message_resolver.ProjectAllocationInstallationMessageResolver;
import io.imunity.furms.site.api.message_resolver.ProjectInstallationMessageResolver;
import io.imunity.furms.site.api.site_agent.SiteAgentStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
	private SiteAgentListenerConnector siteAgentListenerConnector;
	@MockBean
	private ProjectInstallationMessageResolver projectInstallationService;
	@MockBean
	private ProjectAllocationInstallationMessageResolver projectAllocationInstallationMessageResolver;
	@MockBean
	private SiteExternalIdsResolver siteExternalIdsResolver;

	@BeforeEach
	void init(){
		siteAgentListenerConnector.connectListenerToQueue( "mock-site-pub");
	}

	@Test
	void shouldReturnOKStatus() throws ExecutionException, InterruptedException {
		PendingJob<SiteAgentStatus> ping = siteAgentStatusService.getStatus(new SiteExternalId("mock"));
		assertThat(ping.jobFuture.get().status).isEqualTo(AvailabilityStatus.AVAILABLE);
	}
}
