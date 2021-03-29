/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.broker;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class SiteAgentMock {

	private final RabbitTemplate rabbitTemplate;

	public SiteAgentMock(RabbitTemplate rabbitTemplate){
		this.rabbitTemplate = rabbitTemplate;
	}

	@RabbitListener(queues = "mock")
	public void receive(Message message) {
		String correlationId = message.getMessageProperties().getCorrelationId();

		MessageProperties messageProperties = new MessageProperties();
		messageProperties.setHeader("version", 1);
		messageProperties.setHeader("status", "IN_PROGRESS");
		messageProperties.setCorrelationId(correlationId);
		Message replyAckMessage = new Message(new byte[]{}, messageProperties);
		rabbitTemplate.send("reply-queue", replyAckMessage);

		MessageProperties messageProperties2 = new MessageProperties();
		messageProperties2.setHeader("version", 1);
		messageProperties2.setHeader("status", "OK");
		messageProperties2.setCorrelationId(correlationId);
		Message replyMessage = new Message(new byte[]{}, messageProperties2);
		rabbitTemplate.send("reply-queue", replyMessage);
	}

}
