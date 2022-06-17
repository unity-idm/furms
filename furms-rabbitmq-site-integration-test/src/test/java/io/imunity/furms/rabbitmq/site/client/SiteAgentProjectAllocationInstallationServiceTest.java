/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ErrorMessage;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationStatus;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.rabbitmq.site.IntegrationTestBase;
import io.imunity.furms.rabbitmq.site.client.mocks.SiteAgentChunkUpdateProducerMock;
import io.imunity.furms.rabbitmq.site.client.mocks.SiteAgentMessageErrorInfoReceiverMock;
import io.imunity.furms.rabbitmq.site.models.AgentMessageErrorInfo;
import io.imunity.furms.rabbitmq.site.models.AgentProjectAllocationInstallationRequest;
import io.imunity.furms.rabbitmq.site.models.AgentProjectAllocationUpdate;
import io.imunity.furms.rabbitmq.site.models.Error;
import io.imunity.furms.rabbitmq.site.models.Header;
import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.rabbitmq.site.models.Status;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectAllocationInstallationService;
import io.imunity.furms.site.api.status_updater.ProjectAllocationInstallationStatusUpdater;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getFurmsPublishQueueName;
import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SiteAgentProjectAllocationInstallationServiceTest extends IntegrationTestBase {

	@Autowired
	private SiteAgentProjectAllocationInstallationService siteAgentProjectAllocationInstallationService;
	@Autowired
	private ProjectAllocationInstallationStatusUpdater projectAllocationInstallationStatusUpdater;
	@Autowired
	private SiteAgentChunkUpdateProducerMock producerMock;
	@Autowired
	private RabbitTemplate rabbitTemplate;
	@Autowired
	private SiteAgentMessageErrorInfoReceiverMock receiverMock;

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

		when(projectAllocationInstallationStatusUpdater.isFirstChunk(projectAllocationResolved.id))
			.thenReturn(true)
			.thenReturn(false);

		siteAgentProjectAllocationInstallationService.allocateProject(correlationId, projectAllocationResolved);

		verify(projectAllocationInstallationStatusUpdater, timeout(10000)).updateStatus(correlationId, ProjectAllocationInstallationStatus.ACKNOWLEDGED, Optional.empty());
		verify(projectAllocationInstallationStatusUpdater, timeout(15000)).updateStatus(projectAllocationResolved.id,
			ProjectAllocationInstallationStatus.INSTALLED, Optional.empty());
		verify(projectAllocationInstallationStatusUpdater, timeout(15000).times(2)).createChunk(any());
	}

	@Test
	void shouldSavedProjectAllocationAsFailedIfFirstAndSecondChunkFailed() {
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

		when(projectAllocationInstallationStatusUpdater.isFirstChunk(projectAllocationResolved.id))
			.thenReturn(true)
			.thenReturn(false);

		AgentProjectAllocationInstallationRequest request = ProjectAllocationMapper.mapAllocation(projectAllocationResolved);
		String queueName = getFurmsPublishQueueName(projectAllocationResolved.site.getExternalId());
		rabbitTemplate.convertAndSend(queueName, new Payload<>(new Header(VERSION, correlationId.id, Status.FAILED,
			new Error("1", "FAILED")), request));

		verify(projectAllocationInstallationStatusUpdater, timeout(10000)).updateStatus(correlationId, ProjectAllocationInstallationStatus.ACKNOWLEDGED, Optional.empty());
		verify(projectAllocationInstallationStatusUpdater, timeout(15000)).updateStatus(projectAllocationResolved.id,
			ProjectAllocationInstallationStatus.FAILED, Optional.of(new ErrorMessage("1", "FAILED")));
		verify(receiverMock, timeout(15000)).process(
			new AgentMessageErrorInfo(correlationId.id, "UnsupportedFailedChunk", "Status of chuck request of allocation id is failed - this is not supported")
		);
		verify(projectAllocationInstallationStatusUpdater, timeout(15000).times(0)).createChunk(any());
	}

	@Test
	void shouldNotUpdateProjectAllocationChunkIfStatusFailed() {
		CorrelationId correlationId = CorrelationId.randomID();
		AgentProjectAllocationUpdate update = AgentProjectAllocationUpdate.builder()
			.allocationIdentifier(UUID.randomUUID().toString())
			.allocationChunkIdentifier("chunkId")
			.amount(BigDecimal.TEN)
			.validTo(OffsetDateTime.now().minusDays(5))
			.validFrom(OffsetDateTime.now().plusDays(5))
			.build();

		producerMock.sendAgentProjectAllocationUpdate(
			update,
			new Header(VERSION, correlationId.id, Status.FAILED, new Error("1", "FAILED"))
		);

		verify(receiverMock, timeout(15000)).process(
			new AgentMessageErrorInfo(correlationId.id, "UnsupportedFailedChunk",
				"Status of chuck update of allocation " + update.allocationIdentifier + " is failed - this is not supported")
		);
		verify(projectAllocationInstallationStatusUpdater, timeout(10000).times(0)).updateChunk(any());
	}

	@Test
	void shouldUpdateProjectAllocationChunk() {
		AgentProjectAllocationUpdate update = AgentProjectAllocationUpdate.builder()
			.allocationIdentifier(UUID.randomUUID().toString())
			.allocationChunkIdentifier("chunkId")
			.amount(BigDecimal.TEN)
			.validTo(OffsetDateTime.now().minusDays(5))
			.validFrom(OffsetDateTime.now().plusDays(5))
			.build();
		producerMock.sendAgentProjectAllocationUpdate(update);

		verify(projectAllocationInstallationStatusUpdater, timeout(10000)).updateChunk(any());
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
