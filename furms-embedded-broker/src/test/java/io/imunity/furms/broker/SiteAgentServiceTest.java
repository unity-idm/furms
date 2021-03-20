/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.broker;

import io.imunity.furms.broker.config.EmbeddedBrokerTest;
import io.imunity.furms.domain.site_agent.AvailabilityStatus;
import io.imunity.furms.domain.site_agent.SiteAgentStatus;
import io.imunity.furms.site.api.SiteAgentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedBrokerTest
class SiteAgentServiceTest {
	@Autowired
	private SiteAgentService siteAgentService;

	@Test
	void shouldReturnOKStatus() throws ExecutionException, InterruptedException {
		CompletableFuture<SiteAgentStatus> ping = siteAgentService.ping("mock");
		assertThat(ping.get().status).isEqualTo(AvailabilityStatus.AVAILABLE);
	}
}
