/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.site_agent.AvailabilityStatus;
import io.imunity.furms.domain.site_agent.PendingJob;
import io.imunity.furms.domain.site_agent.SiteAgentStatus;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.rabbitmq.site.IntegrationTestBase;
import io.imunity.furms.site.api.site_agent.SiteAgentStatusService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

class SiteAgentStatusServiceTest extends IntegrationTestBase {

	@Autowired
	private SiteAgentStatusService siteAgentStatusService;

	@Test
	void shouldReturnOKStatus() throws ExecutionException, InterruptedException {
		PendingJob<SiteAgentStatus> ping = siteAgentStatusService.getStatus(new SiteExternalId("mock"));
		assertThat(ping.jobFuture.get().status).isEqualTo(AvailabilityStatus.AVAILABLE);
	}
}
