/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models.converter;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.imunity.furms.rabbitmq.site.models.ErrorPayload;
import io.imunity.furms.rabbitmq.site.models.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
		registerSubtypes(mapper);
		mapper.disable(WRITE_DATES_AS_TIMESTAMPS);
		this.jackson2JsonMessageConverter = new Jackson2JsonMessageConverter(mapper);
	}

	private void registerSubtypes(ObjectMapper objectMapper){
		ClassPathScanningCandidateComponentProvider scanner =
			new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(JsonTypeName.class));
		Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents("io.imunity.furms.rabbitmq.site.models");

		Set<String> definedNames = new HashSet<>();
		for (BeanDefinition bd : candidateComponents) {
			try {
				Class<?> clazz = Class.forName(bd.getBeanClassName());
				JsonTypeName typeName = clazz.getAnnotation(JsonTypeName.class);
				if (typeName != null) {
					if (!definedNames.add(typeName.value()))
						throw new IllegalStateException("The Json type name " + typeName.value() +
							" was used multiple times! It is a bug");
					objectMapper.registerSubtypes(clazz);
				}
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public Message toMessage(Object o, MessageProperties messageProperties) throws MessageConversionException {
		Message message = jackson2JsonMessageConverter.toMessage(o, messageProperties);
		message.getMessageProperties().getHeaders().remove(TYPE_ID);
		return message;
	}

	@Override
	public Object fromMessage(Message message) throws MessageConversionException {
		String contentAsString = null;
		try {
			String contentEncoding = ofNullable(message.getMessageProperties().getContentEncoding()).orElse(UTF_8);
			contentAsString = new String(message.getBody(), contentEncoding);
		} catch (UnsupportedEncodingException e) {
			LOG.error("Message cannot be convert: {}", message);
			return new ErrorPayload(null, message.toString());
		}

		try {
			return mapper.readValue(contentAsString, mapper.constructType(Payload.class));
		} catch (Exception e) {
			LOG.error("Message cannot be convert: {}", message);
			LOG.error("This error occurred when message was parsing: ", e);
			return new ErrorPayload(getCorrelationId(contentAsString), contentAsString);
		}
	}

	private String getCorrelationId(String content) {
		try {
			return Optional.ofNullable(mapper.readTree(content))
				.flatMap(value -> Optional.ofNullable(value.get("header")))
				.flatMap(value -> Optional.ofNullable(value.get("messageCorrelationId")))
				.map(JsonNode::textValue)
				.orElse(null);
		} catch (Exception e) {
			LOG.error("Correlation Id cannot be found", e);
			return null;
		}
	}
}
