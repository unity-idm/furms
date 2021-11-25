/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.config;

import io.imunity.furms.rabbitmq.site.models.Payload;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getSiteId;

@Primary
@Component
class DefaultRabbitTemplate extends RabbitTemplate {

	private final MessageSaver messageSaver;

	DefaultRabbitTemplate(MessageSaver messageSaver, ConnectionFactory connectionFactory, MessageConverter converter) {
		super(connectionFactory);
		setMessageConverter(converter);
		this.messageSaver = messageSaver;
	}

	@Override
	public void convertAndSend(String routingKey, Object object){
		Payload<?> payload = (Payload<?>) object;
		messageSaver.save(getSiteId(routingKey), payload);
		super.convertAndSend(routingKey, object);
	}
}
