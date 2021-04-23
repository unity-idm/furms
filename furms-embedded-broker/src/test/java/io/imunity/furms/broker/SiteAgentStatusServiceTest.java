/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.broker;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import io.imunity.furms.domain.site_agent.AckStatus;
import io.imunity.furms.domain.site_agent.AvailabilityStatus;
import io.imunity.furms.domain.site_agent.PendingJob;
import io.imunity.furms.domain.site_agent.SiteAgentStatus;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.site.api.message_resolver.ProjectInstallationMessageResolver;
import io.imunity.furms.site.api.message_resolver.SSHKeyOperationMessageResolver;
import io.imunity.furms.site.api.site_agent.SiteAgentStatusService;

@SpringBootTest
class SiteAgentStatusServiceTest {
	@Autowired
	private SiteAgentStatusService siteAgentStatusService;
	@MockBean
	private ProjectInstallationMessageResolver projectInstallationService;
	@MockBean
	private SSHKeyOperationMessageResolver sshKeyOperationService;
	
	@Test
	void shouldReturnOKStatus() throws ExecutionException, InterruptedException {
		PendingJob<SiteAgentStatus> ping = siteAgentStatusService.getStatus(new SiteExternalId("mock"));
		assertThat(ping.ackFuture.get()).isEqualTo(AckStatus.ACK);
		assertThat(ping.jobFuture.get().status).isEqualTo(AvailabilityStatus.AVAILABLE);
	}
}
