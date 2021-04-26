/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.imunity.furms.rabbitmq.site.models.Payload;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;

import java.io.UnsupportedEncodingException;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

public class FurmsPayloadConverter implements MessageConverter {
	private static final String TYPE_ID = "__TypeId__";
	public final ObjectMapper mapper;
	public final Jackson2JsonMessageConverter jackson2JsonMessageConverter;

	public FurmsPayloadConverter() {
		this.mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.registerModule(new ParameterNamesModule());
		mapper.disable(WRITE_DATES_AS_TIMESTAMPS);
		this.jackson2JsonMessageConverter = new Jackson2JsonMessageConverter(mapper);
	}

	@Override
	public Message toMessage(Object o, MessageProperties messageProperties) throws MessageConversionException {
		Message message = jackson2JsonMessageConverter.toMessage(o, messageProperties);
		message.getMessageProperties().getHeaders().remove(TYPE_ID);
		return message;
	}

	@Override
	public Object fromMessage(Message message) throws MessageConversionException {
		try {
			String contentAsString = new String(message.getBody(), message.getMessageProperties().getContentEncoding());
			return mapper.readValue(contentAsString, mapper.constructType(Payload.class));
		} catch (JsonProcessingException | UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Message cannot be convert: ", e);
		}
	}
}
