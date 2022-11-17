/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.project_allocation_installation.ErrorMessage;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.rabbitmq.site.IntegrationTestBase;
import io.imunity.furms.rabbitmq.site.client.mocks.SiteAgentErrorProducerMock;
import io.imunity.furms.rabbitmq.site.models.AgentProjectAllocationInstallationAck;
import io.imunity.furms.rabbitmq.site.models.Error;
import io.imunity.furms.site.api.AgentPendingMessageSiteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

class SiteAgentErrorHandlingInPendingMessageTest extends IntegrationTestBase {
	@Autowired
	private SiteAgentErrorProducerMock siteAgentErrorProducerMock;

	@Autowired
	private AgentPendingMessageSiteService agentPendingMessageSiteService;

	@Test
	void shouldUpdatePendingMessageErrorCodeAndMessageWhenReceivedMessageIsFailed() {
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		AgentProjectAllocationInstallationAck brokenMessage = new AgentProjectAllocationInstallationAck();
		Error errorMsg = new Error("123", "errorMsg");
		siteAgentErrorProducerMock.sendMessageWithErrorMessage(correlationId.id, brokenMessage, errorMsg);

		verify(agentPendingMessageSiteService, timeout(10000)).setAsAcknowledged(correlationId);
		verify(agentPendingMessageSiteService, timeout(10000)).updateErrorMessage(correlationId, new ErrorMessage(errorMsg.code,
			errorMsg.message));
	}
}
