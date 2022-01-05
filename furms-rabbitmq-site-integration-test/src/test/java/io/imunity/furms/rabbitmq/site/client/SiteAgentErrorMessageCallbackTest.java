/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.rabbitmq.site.IntegrationTestBase;
import io.imunity.furms.rabbitmq.site.client.mocks.SiteAgentErrorProducerMock;
import io.imunity.furms.rabbitmq.site.client.mocks.SiteAgentMessageErrorInfoReceiverMock;
import io.imunity.furms.rabbitmq.site.models.AgentMessageErrorInfo;
import io.imunity.furms.rabbitmq.site.models.CumulativeResourceUsageRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

class SiteAgentErrorMessageCallbackTest extends IntegrationTestBase {

	@Autowired
	private SiteAgentMessageErrorInfoReceiverMock receiverMock;
	@Autowired
	private SiteAgentErrorProducerMock siteAgentErrorProducerMock;

	@Test
	void shouldSendMessageErrorInfoWhenReceivedMessageIsBroken() {
		String correlationId = UUID.randomUUID().toString();
		CumulativeResourceUsageRecord brokenMessage = new CumulativeResourceUsageRecord("id", "pid", BigDecimal.ONE, OffsetDateTime.now());
		siteAgentErrorProducerMock.sendCumulativeResourceUsageRecord(correlationId, brokenMessage);

		verify(receiverMock, timeout(10000)).process(
			new AgentMessageErrorInfo(correlationId, "IllegalStateTransition", "Usage is failed - it's not supported")
		);
	}
}
