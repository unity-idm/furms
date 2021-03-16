/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.broker;

import io.imunity.furms.domain.site_messages.PingStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles(profiles = "embedded-broker")
@SpringBootTest(properties = "spring.rabbitmq.port=${random.int(30000,40000)}")
class SiteMessagerTest {
	@Autowired
	private SiteMessagerImpl siteMessager;

	@Test
	void shouldReturnOKStatus() throws ExecutionException, InterruptedException {
		CompletableFuture<PingStatus> ping = siteMessager.ping();
		assertThat(ping.get()).isEqualTo(PingStatus.OK);
	}
}
