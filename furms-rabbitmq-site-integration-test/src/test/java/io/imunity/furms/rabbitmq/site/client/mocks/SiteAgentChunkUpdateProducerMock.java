/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.mocks;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.rabbitmq.site.models.AgentProjectAllocationUpdate;
import io.imunity.furms.rabbitmq.site.models.AgentProjectResourceAllocationStatusResult;
import io.imunity.furms.rabbitmq.site.models.Error;
import io.imunity.furms.rabbitmq.site.models.Header;
import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.rabbitmq.site.models.Status;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;

@Component
public class SiteAgentChunkUpdateProducerMock {

	private static final String MOCK_SITE_PUB = "mock-site-pub";
	private final RabbitTemplate rabbitTemplate;

	public SiteAgentChunkUpdateProducerMock(RabbitTemplate rabbitTemplate){
		this.rabbitTemplate = rabbitTemplate;
	}

	public void sendAgentProjectAllocationUpdate(AgentProjectAllocationUpdate update) {
		Header header = getHeader(UUID.randomUUID().toString());
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, update));
	}

	public void sendAgentProjectResourceAllocationStatusResult(CorrelationId correlationId,
	                                                           AgentProjectResourceAllocationStatusResult result) {
		Header header = getHeader(correlationId.id);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, result));
	}

	public void sendFailedAgentProjectResourceAllocationStatusResult(CorrelationId correlationId, Error error,
	                                                           AgentProjectResourceAllocationStatusResult result) {
		Header header = new Header(VERSION, correlationId.id, Status.FAILED, error);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, result));
	}

	public void sendAgentProjectAllocationUpdate(AgentProjectAllocationUpdate update, Header header) {
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, update));
	}

	private Header getHeader(String messageCorrelationId) {
		return new Header(VERSION, messageCorrelationId, Status.OK, null);
	}
}
