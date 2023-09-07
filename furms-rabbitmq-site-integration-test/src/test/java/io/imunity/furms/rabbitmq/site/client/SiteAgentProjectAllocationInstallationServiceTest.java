/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ErrorMessage;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationStatus;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_credits.ResourceCreditId;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.rabbitmq.site.IntegrationTestBase;
import io.imunity.furms.rabbitmq.site.client.mocks.SiteAgentChunkUpdateProducerMock;
import io.imunity.furms.rabbitmq.site.client.mocks.SiteAgentMessageErrorInfoReceiverMock;
import io.imunity.furms.rabbitmq.site.models.AgentMessageErrorInfo;
import io.imunity.furms.rabbitmq.site.models.AgentProjectAllocationUpdate;
import io.imunity.furms.rabbitmq.site.models.AgentProjectResourceAllocationStatusResult;
import io.imunity.furms.rabbitmq.site.models.Error;
import io.imunity.furms.rabbitmq.site.models.Header;
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

import static io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus.FAILED;
import static io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus.INSTALLED;
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
			.id(new ProjectAllocationId(UUID.randomUUID()))
			.projectId(new ProjectId(UUID.randomUUID()))
			.amount(BigDecimal.TEN)
			.site(Site.builder()
				.id(new SiteId(UUID.randomUUID().toString(), new SiteExternalId("mock")))
				.build()
			)
			.resourceType(ResourceType.builder()
				.name("name")
				.build())
			.resourceCredit(ResourceCredit.builder()
				.id(new ResourceCreditId(UUID.randomUUID()))
				.utcStartTime(LocalDateTime.now())
				.utcEndTime(LocalDateTime.now())
				.build())
			.build();

		when(projectAllocationInstallationStatusUpdater.isWaitingForInstallationConfirmation(projectAllocationResolved.id))
			.thenReturn(false);

		siteAgentProjectAllocationInstallationService.allocateProject(correlationId, projectAllocationResolved);

		verify(projectAllocationInstallationStatusUpdater, timeout(10000)).updateStatusToAcknowledged(correlationId);
		verify(projectAllocationInstallationStatusUpdater, timeout(15000)).updateStatus(correlationId,
			ProjectAllocationInstallationStatus.INSTALLED, Optional.empty());
		verify(projectAllocationInstallationStatusUpdater, timeout(15000).times(2)).createChunk(any());
	}

	@Test
	void shouldSendErrorMessageWhenChunkIsReceivedBeforeInstallingAllocation() {
		CorrelationId correlationId = CorrelationId.randomID();
		ProjectAllocationResolved projectAllocationResolved = ProjectAllocationResolved.builder()
			.id(new ProjectAllocationId(UUID.randomUUID()))
			.projectId(new ProjectId(UUID.randomUUID()))
			.amount(BigDecimal.TEN)
			.site(Site.builder()
				.id(new SiteId(UUID.randomUUID().toString(), new SiteExternalId("mock")))
				.build()
			)
			.resourceType(ResourceType.builder()
				.name("name")
				.build())
			.resourceCredit(ResourceCredit.builder()
				.id(new ResourceCreditId(UUID.randomUUID()))
				.utcStartTime(LocalDateTime.now())
				.utcEndTime(LocalDateTime.now())
				.build())
			.build();

		when(projectAllocationInstallationStatusUpdater.isWaitingForInstallationConfirmation(projectAllocationResolved.id))
			.thenReturn(true);

		siteAgentProjectAllocationInstallationService.allocateProject(correlationId, projectAllocationResolved);

		verify(receiverMock, timeout(15000).times(2)).process(
			new AgentMessageErrorInfo(correlationId.id, "IllegalStateTransition",
				"Allocation "+  projectAllocationResolved.id.id +" waiting for ProjectResourceAllocationStatusResult")
		);
	}

	@Test
	void shouldNotUpdateProjectAllocationChunkIfIsFailed() {
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
				"Update of chunk of allocation " + update.allocationIdentifier + " can not be set to failed")
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
	void shouldSendErrorMessageWhenChunkUpdateIsReceivedBeforeInstallingAllocation() {
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());
		AgentProjectAllocationUpdate update = AgentProjectAllocationUpdate.builder()
			.allocationIdentifier(projectAllocationId.id.toString())
			.allocationChunkIdentifier("chunkId")
			.amount(BigDecimal.TEN)
			.validTo(OffsetDateTime.now().minusDays(5))
			.validFrom(OffsetDateTime.now().plusDays(5))
			.build();

		when(projectAllocationInstallationStatusUpdater.isWaitingForInstallationConfirmation(projectAllocationId)).thenReturn(true);

		String correlationId = producerMock.sendAgentProjectAllocationUpdate(update);

		verify(receiverMock, timeout(15000)).process(
			new AgentMessageErrorInfo(correlationId, "IllegalStateTransition",
				"Allocation "+  projectAllocationId.id +" waiting for ProjectResourceAllocationStatusResult")
		);
	}

	@Test
	void shouldConfirmInstallation() {
		CorrelationId correlationId = CorrelationId.randomID();

		producerMock.sendAgentProjectResourceAllocationStatusResult(correlationId,
			new AgentProjectResourceAllocationStatusResult());

		verify(projectAllocationInstallationStatusUpdater, timeout(10000)).updateStatus(correlationId, INSTALLED,
			Optional.empty());
	}

	@Test
	void shouldNotConfirmInstallation() {
		CorrelationId correlationId = CorrelationId.randomID();
		Error error = new Error("1", "error");

		producerMock.sendFailedAgentProjectResourceAllocationStatusResult(correlationId, error,
			new AgentProjectResourceAllocationStatusResult());

		verify(projectAllocationInstallationStatusUpdater, timeout(10000)).updateStatus(correlationId, FAILED,
			Optional.of(new ErrorMessage(error.code, error.message)));
	}
	@Test
	void shouldDeallocateProject() {
		CorrelationId correlationId = CorrelationId.randomID();
		ProjectAllocationResolved projectAllocationResolved = ProjectAllocationResolved.builder()
			.id(new ProjectAllocationId(UUID.randomUUID()))
			.projectId(new ProjectId(UUID.randomUUID()))
			.amount(BigDecimal.TEN)
			.site(Site.builder()
				.id(new SiteId(UUID.randomUUID().toString(), new SiteExternalId("mock")))
				.build()
			)
			.resourceType(ResourceType.builder()
				.name("name")
				.build())
			.resourceCredit(ResourceCredit.builder()
				.id(new ResourceCreditId(UUID.randomUUID()))
				.utcStartTime(LocalDateTime.now())
				.utcEndTime(LocalDateTime.now())
				.build())
			.build();
		siteAgentProjectAllocationInstallationService.deallocateProject(correlationId, projectAllocationResolved);

		verify(projectAllocationInstallationStatusUpdater, timeout(10000)).updateStatus(correlationId, ProjectDeallocationStatus.ACKNOWLEDGED, Optional.empty());
	}
}
