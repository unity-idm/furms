/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.imunity.furms.rabbitmq.site.models.EmptyBodyResponse;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.data.util.AnnotatedTypeScanner;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Optional;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;


public class FurmsMessageConverter implements MessageConverter {

	private final Jackson2JsonMessageConverter converter = getJackson2JsonMessageConverter();

	private final Map<String, Class<?>> types = new AnnotatedTypeScanner(false, FurmsMessage.class)
		.findTypes("io.imunity.furms.rabbitmq.site.models")
		.stream().collect(toMap(x -> x.getAnnotation(FurmsMessage.class).type(), identity()));

	@Override
	public Message toMessage(Object o, MessageProperties messageProperties) throws MessageConversionException {
		if(EmptyBodyResponse.class.isAssignableFrom(o.getClass()))
			return new Message(new byte[]{}, messageProperties);
		return converter.toMessage(o, messageProperties);
	}

	@Override
	public Object fromMessage(Message message) throws MessageConversionException {
		String furmsMessageType = message.getMessageProperties().getHeader("furmsMessageType").toString();
		String correlationId = message.getMessageProperties().getCorrelationId();
		String status =  message.getMessageProperties().getHeader("status");
		return Optional.ofNullable(types.get(furmsMessageType))
			.filter(EmptyBodyResponse.class::isAssignableFrom)
			.flatMap(aClass -> initialize(aClass, correlationId, status))
			.orElseGet(() -> converter.fromMessage(message));
	}

	private Optional<Object> initialize(Class<?> aClass, String correlationId, String status) {
		try {
			Constructor<?> declaredConstructor = aClass.getDeclaredConstructor(String.class, String.class);
			declaredConstructor.setAccessible(true);
			return Optional.of(declaredConstructor.newInstance(correlationId, status));
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	private Jackson2JsonMessageConverter getJackson2JsonMessageConverter() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		return new Jackson2JsonMessageConverter(mapper);
	}
}
