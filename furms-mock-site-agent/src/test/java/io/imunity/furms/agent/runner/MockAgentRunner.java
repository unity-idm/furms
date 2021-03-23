/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.agent.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.imunity.furms.rabbitmq.site.client.AgentPingRequest;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.listener.adapter.ReplyingMessageListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@SpringBootApplication
public class MockAgentRunner {
	public static void main(String[] args) {
		new SpringApplicationBuilder(MockAgentRunner.class)
			.web(WebApplicationType.NONE)
			.run(args);
	}

	@Bean
	public SimpleMessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(getQueuesNames());
		container.setMessageListener(new MessageListenerAdapter((ReplyingMessageListener<AgentPingRequest, String>) data -> {
			return "OK";
		}, messageConverter()));
		return container;
	}

	private Jackson2JsonMessageConverter messageConverter() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		return new Jackson2JsonMessageConverter(objectMapper);
	}

	private String[] getQueuesNames() {
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<QueueName[]> forEntity = restTemplate.getForEntity("http://localhost:55570/api/latest/queue", QueueName[].class, Map.of());
		return ofNullable(forEntity.getBody()).stream()
			.flatMap(Arrays::stream)
			.filter(x -> isUUID(x.name))
			.map(x -> x.name)
			.toArray(String[]::new);
	}

	private boolean isUUID(String s){
		try{
			UUID.fromString(s);
			return true;
		} catch (IllegalArgumentException exception){
			return false;
		}
	}
}
