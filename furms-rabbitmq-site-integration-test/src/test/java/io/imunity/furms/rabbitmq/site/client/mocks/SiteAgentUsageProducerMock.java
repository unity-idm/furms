/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.mocks;

import io.imunity.furms.rabbitmq.site.models.CumulativeResourceUsageRecord;
import io.imunity.furms.rabbitmq.site.models.Header;
import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.rabbitmq.site.models.Status;
import io.imunity.furms.rabbitmq.site.models.UserResourceUsageRecord;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;

@Component
public class SiteAgentUsageProducerMock {

	private static final String MOCK_FURMS_PUB = "mock-furms-pub";
	private final RabbitTemplate rabbitTemplate;

	public SiteAgentUsageProducerMock(RabbitTemplate rabbitTemplate){
		this.rabbitTemplate = rabbitTemplate;
	}

	public void sendCumulativeResourceUsageRecord(CumulativeResourceUsageRecord record) {
		Header header = getHeader(UUID.randomUUID().toString());
		rabbitTemplate.convertAndSend(MOCK_FURMS_PUB, new Payload<>(header, record));
	}

	public void sendUserResourceUsageRecord(UserResourceUsageRecord record) {
		Header header = getHeader(UUID.randomUUID().toString());
		rabbitTemplate.convertAndSend(MOCK_FURMS_PUB, new Payload<>(header, record));
	}

	private Header getHeader(String messageCorrelationId) {
		return new Header(VERSION, messageCorrelationId, Status.OK, null);
	}
}
