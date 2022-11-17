/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.mocks;

import io.imunity.furms.rabbitmq.site.models.AgentProjectAllocationInstallationAck;
import io.imunity.furms.rabbitmq.site.models.CumulativeResourceUsageRecord;
import io.imunity.furms.rabbitmq.site.models.Error;
import io.imunity.furms.rabbitmq.site.models.Header;
import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.rabbitmq.site.models.Status;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;

@Component
public class SiteAgentErrorProducerMock {

	private final static String MOCK_SITE_PUB = "mock-site-pub";
	private final RabbitTemplate rabbitTemplate;

	public SiteAgentErrorProducerMock(RabbitTemplate rabbitTemplate){
		this.rabbitTemplate = rabbitTemplate;
	}

	public void sendCumulativeResourceUsageRecord(String messageCorrelationId, CumulativeResourceUsageRecord record) {
		Header header = getHeader(messageCorrelationId);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, record));
	}

	public void sendMessageWithErrorMessage(String messageCorrelationId, AgentProjectAllocationInstallationAck record,
	                                        Error errorMessage) {
		Header header = new Header("1.5", messageCorrelationId, Status.FAILED, errorMessage);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, record));
	}

	public void sendMessageWithNullStatus(String messageCorrelationId, CumulativeResourceUsageRecord record) {
		Header header = new Header(VERSION, messageCorrelationId, null, null);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, record));
	}

	public void sendMessageWithIncorrectVersion(String messageCorrelationId, CumulativeResourceUsageRecord record) {
		Header header = new Header("1.5", messageCorrelationId, Status.OK, null);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, record));
	}

	private Header getHeader(String messageCorrelationId) {
		return new Header(VERSION, messageCorrelationId, Status.FAILED, null);
	}
}
