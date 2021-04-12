/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.broker;

import io.imunity.furms.broker.config.EmbeddedBrokerTest;
import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.project_installation.ProjectInstallationJob;
import io.imunity.furms.domain.site_agent.*;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.site.api.ProjectInstallationService;
import io.imunity.furms.site.api.SiteAgentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.concurrent.ExecutionException;

import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.ACK;
import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.DONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@EmbeddedBrokerTest
class SiteAgentServiceTest {
	@Autowired
	private SiteAgentService siteAgentService;
	@MockBean
	private ProjectInstallationService projectInstallationService;

	@Test
	void shouldReturnOKStatus() throws ExecutionException, InterruptedException {
		PendingJob<SiteAgentStatus> ping = siteAgentService.getStatus(new SiteExternalId("mock"));
		assertThat(ping.ackFuture.get()).isEqualTo(AckStatus.ACK);
		assertThat(ping.jobFuture.get().status).isEqualTo(AvailabilityStatus.AVAILABLE);
	}

	@Test
	void shouldInstallProject() throws ExecutionException, InterruptedException {
		CorrelationId correlationId = siteAgentService.installProject(ProjectInstallation.builder()
			.id("id")
			.siteExternalId("mock")
			.leader(FURMSUser.builder()
				.id(new PersistentId("id"))
				.email("email")
				.build())
			.build()
		);

		verify(projectInstallationService, timeout(10000)).update(new ProjectInstallationJob(correlationId, ACK));
		verify(projectInstallationService, timeout(10000)).update(new ProjectInstallationJob(correlationId, DONE));
	}
}
