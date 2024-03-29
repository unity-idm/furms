/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.rabbitmq.site.IntegrationTestBase;
import io.imunity.furms.rabbitmq.site.client.mocks.SiteAgentMessageErrorInfoReceiverMock;
import io.imunity.furms.rabbitmq.site.client.mocks.SiteAgentUsageProducerMock;
import io.imunity.furms.rabbitmq.site.models.AgentMessageErrorInfo;
import io.imunity.furms.rabbitmq.site.models.CumulativeResourceUsageRecord;
import io.imunity.furms.rabbitmq.site.models.UserResourceUsageRecord;
import io.imunity.furms.site.api.message_resolver.ResourceUsageUpdater;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

class SiteAgentResourceUsageServiceTest extends IntegrationTestBase {

	@Autowired
	private ResourceUsageUpdater resourceUsageUpdater;
	@Autowired
	private SiteAgentUsageProducerMock producerMock;
	@Autowired
	private SiteAgentMessageErrorInfoReceiverMock receiverMock;

	@Test
	void shouldReceivedResourceUsage() {
		CumulativeResourceUsageRecord record = new CumulativeResourceUsageRecord(
			UUID.randomUUID().toString(),
			UUID.randomUUID().toString(),
			BigDecimal.ONE,
			OffsetDateTime.now()
		);
		producerMock.sendCumulativeResourceUsageRecord(record);

		verify(resourceUsageUpdater, timeout(10000)).updateUsage(
			ResourceUsage.builder()
				.projectAllocationId(record.allocationIdentifier)
				.projectId(record.projectIdentifier)
				.cumulativeConsumption(record.cumulativeConsumption)
				.probedAt(convertToUTCTime(record.probedAt))
				.build()
		);
	}

	@Test
	void shouldReceiveAgentMessageErrorInfoMessageWhenCumulativeResourceUsageAmountIsNull() {
		CumulativeResourceUsageRecord record = new CumulativeResourceUsageRecord(
			UUID.randomUUID().toString(),
			UUID.randomUUID().toString(),
			null,
			OffsetDateTime.now()
		);
		producerMock.sendCumulativeResourceUsageRecord(record);

		verify(resourceUsageUpdater, timeout(10000).times(0)).updateUsage(
			ResourceUsage.builder()
				.projectAllocationId(record.allocationIdentifier)
				.projectId(record.projectIdentifier)
				.cumulativeConsumption(record.cumulativeConsumption)
				.probedAt(convertToUTCTime(record.probedAt))
				.build()
		);

		ArgumentCaptor<AgentMessageErrorInfo> argumentCaptor = ArgumentCaptor.forClass(AgentMessageErrorInfo.class);
		verify(receiverMock, timeout(10000)).process(argumentCaptor.capture());
		assertThat(argumentCaptor.getValue().description).contains("Reasons: [body.cumulativeConsumption must not be null]");
	}

	@Test
	void shouldReceivedUserResourceUsage() {
		UserResourceUsageRecord record = new UserResourceUsageRecord(
			UUID.randomUUID().toString(),
			UUID.randomUUID().toString(),
			"fenixUserId",
			BigDecimal.ONE,
			OffsetDateTime.now()
		);
		producerMock.sendUserResourceUsageRecord(record);

		verify(resourceUsageUpdater, timeout(10000)).updateUsage(
			UserResourceUsage.builder()
				.projectAllocationId(record.allocationIdentifier)
				.projectId(record.projectIdentifier)
				.fenixUserId(new FenixUserId(record.fenixUserId))
				.cumulativeConsumption(record.cumulativeConsumption)
				.consumedUntil(convertToUTCTime(record.consumedUntil))
				.build()
		);
	}
}
