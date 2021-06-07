/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationStatus;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.site.api.message_resolver.ProjectAllocationInstallationStatusUpdater;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectAllocationInstallationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
class SiteAgentProjectAllocationInstallationServiceTest {
	@Autowired
	private SiteAgentProjectAllocationInstallationService siteAgentProjectAllocationInstallationService;
	@Autowired
	private SiteAgentListenerConnector siteAgentListenerConnector;
	@Autowired
	private ProjectAllocationInstallationStatusUpdater projectAllocationInstallationStatusUpdater;

	@BeforeEach
	void init(){
		siteAgentListenerConnector.connectListenerToQueue( "mock-site-pub");
	}

	@Test
	void shouldInstallProjectAllocation() {
		CorrelationId correlationId = CorrelationId.randomID();
		ProjectAllocationResolved projectAllocationResolved = ProjectAllocationResolved.builder()
			.id("id")
			.projectId("id")
			.amount(BigDecimal.TEN)
			.site(Site.builder()
				.id("id")
				.externalId(new SiteExternalId("mock"))
				.build()
			)
			.resourceType(ResourceType.builder()
				.name("name")
				.build())
			.resourceCredit(ResourceCredit.builder()
				.id("id")
				.utcStartTime(LocalDateTime.now())
				.utcEndTime(LocalDateTime.now())
				.build())
			.build();
		siteAgentProjectAllocationInstallationService.allocateProject(correlationId, projectAllocationResolved);

		verify(projectAllocationInstallationStatusUpdater, timeout(10000)).updateStatus(correlationId, ProjectAllocationInstallationStatus.ACKNOWLEDGED, Optional.empty());
		verify(projectAllocationInstallationStatusUpdater, timeout(15000).times(2)).createChunk(any());
	}

	@Test
	void shouldDeallocateProject() {
		CorrelationId correlationId = CorrelationId.randomID();
		ProjectAllocationResolved projectAllocationResolved = ProjectAllocationResolved.builder()
			.id("id")
			.projectId("id")
			.amount(BigDecimal.TEN)
			.site(Site.builder()
				.id("id")
				.externalId(new SiteExternalId("mock"))
				.build()
			)
			.resourceType(ResourceType.builder()
				.name("name")
				.build())
			.resourceCredit(ResourceCredit.builder()
				.id("id")
				.utcStartTime(LocalDateTime.now())
				.utcEndTime(LocalDateTime.now())
				.build())
			.build();
		siteAgentProjectAllocationInstallationService.deallocateProject(correlationId, projectAllocationResolved);

		verify(projectAllocationInstallationStatusUpdater, timeout(10000)).updateStatus(correlationId, ProjectDeallocationStatus.ACKNOWLEDGED, Optional.empty());
	}
}
