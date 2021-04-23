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
	private static final String STATUS = "status";
	
	private final Object body;
	private final String correlationId;
	private final String status;

	public TypeHeaderAppender(Object body, String correlationId) {
		this.body = body;
		this.correlationId = correlationId;
		this.status = null;
	}
	
	public TypeHeaderAppender(Object body, String correlationId, String status) {
		this.body = body;
		this.correlationId = correlationId;
		this.status = status;
	}
	
	

	@Override
	public Message postProcessMessage(Message message) throws AmqpException {
		FurmsMessage furmsMessage = AnnotationUtils.getAnnotation(body.getClass(), FurmsMessage.class);
		if (furmsMessage != null) {
			message.getMessageProperties().getHeaders().put(FURMS_MESSAGE_TYPE, furmsMessage.type());
		}
		message.getMessageProperties().getHeaders().put(VERSION, 1);
		if (status != null)
		{
			message.getMessageProperties().getHeaders().put(STATUS, status);
		}
		message.getMessageProperties().setCorrelationId(correlationId);
		message.getMessageProperties().setReplyTo(Queues.REPLY_QUEUE);
		return message;
	}
}
