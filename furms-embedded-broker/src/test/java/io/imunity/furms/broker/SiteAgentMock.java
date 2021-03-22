/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.broker;

import io.imunity.furms.rabbitmq.site.client.AgentPingRequest;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SiteAgentMock {
	@RabbitHandler
	@RabbitListener(queues = "mock")
	public String receive(AgentPingRequest pingRequest) {
		return "OK";
	}
}
