/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rabbitmq.site.client;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.core.annotation.AnnotationUtils;

class TypeHederAppender implements MessagePostProcessor {

	private static final String FURMS_MESSAGE_TYPE = "furmsMessageType";
	private final Object body;

	TypeHederAppender(Object body) {
		this.body = body;
	}

	@Override
	public Message postProcessMessage(Message message) throws AmqpException {
		FurmsMessage furmsMessage = AnnotationUtils.getAnnotation(body.getClass(), FurmsMessage.class);
		if (furmsMessage != null) {
			message.getMessageProperties().getHeaders().put(FURMS_MESSAGE_TYPE, furmsMessage.type());
		}
		return message;
	}
}
