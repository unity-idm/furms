/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.imunity.furms.rabbitmq.site.models.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;

import java.lang.invoke.MethodHandles;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static java.util.Optional.ofNullable;

public class FurmsPayloadConverter implements MessageConverter {
	private static final String TYPE_ID = "__TypeId__";
	private static final String UTF_8 = "UTF-8";
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
			String contentEncoding = ofNullable(message.getMessageProperties().getContentEncoding()).orElse(UTF_8);
			String contentAsString = new String(message.getBody(), contentEncoding);
			return mapper.readValue(contentAsString, mapper.constructType(Payload.class));
		} catch (Exception e) {
			LOG.info("Message cannot be convert: {}", message);
			LOG.info("This error occurred when message was parsing: ", e);
			return new Object();
		}
	}
}
