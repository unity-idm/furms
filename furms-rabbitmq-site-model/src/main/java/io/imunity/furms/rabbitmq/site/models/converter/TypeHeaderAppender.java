/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rabbitmq.site.models.converter;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.core.annotation.AnnotationUtils;

import io.imunity.furms.rabbitmq.site.models.consts.Queues;

public class TypeHeaderAppender implements MessagePostProcessor {

	private static final String FURMS_MESSAGE_TYPE = "furmsMessageType";
	private static final String VERSION = "version";
	private final Object body;
	private final String correlationId;

	public TypeHeaderAppender(Object body, String correlationId) {
		this.body = body;
		this.correlationId = correlationId;
	}

	@Override
	public Message postProcessMessage(Message message) throws AmqpException {
		FurmsMessage furmsMessage = AnnotationUtils.getAnnotation(body.getClass(), FurmsMessage.class);
		if (furmsMessage != null) {
			message.getMessageProperties().getHeaders().put(FURMS_MESSAGE_TYPE, furmsMessage.type());
		}
		message.getMessageProperties().getHeaders().put(VERSION, 1);
		message.getMessageProperties().setCorrelationId(correlationId);
		message.getMessageProperties().setReplyTo(Queues.REPLY_QUEUE);
		return message;
	}
}
