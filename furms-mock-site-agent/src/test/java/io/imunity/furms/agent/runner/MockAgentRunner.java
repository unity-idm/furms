/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.agent.runner;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.util.Optional.ofNullable;

@SpringBootApplication
public class MockAgentRunner {
	public static void main(String[] args) {
		new SpringApplicationBuilder(MockAgentRunner.class)
			.web(WebApplicationType.NONE)
			.run(args);
	}

	private final RabbitTemplate rabbitTemplate;

	public MockAgentRunner(RabbitTemplate rabbitTemplate){
		this.rabbitTemplate = rabbitTemplate;
	}

	@RabbitListener(queues = "#{T(io.imunity.furms.agent.runner.MockAgentRunner).getQueuesNames()}")
	public void receive(Message message) throws InterruptedException {
		String correlationId = message.getMessageProperties().getCorrelationId();

		MessageProperties messageProperties = new MessageProperties();
		messageProperties.setHeader("version", 1);
		messageProperties.setHeader("status", "IN_PROGRESS");
		messageProperties.setCorrelationId(correlationId);
		messageProperties.setHeader("furmsMessageType", "AgentPingResponse");
		Message replyAckMessage = new Message(new byte[]{}, messageProperties);
		rabbitTemplate.send("reply-queue", replyAckMessage);

		TimeUnit.SECONDS.sleep(5);

		MessageProperties messageProperties2 = new MessageProperties();
		messageProperties2.setHeader("version", 1);
		messageProperties2.setHeader("status", "OK");
		messageProperties2.setCorrelationId(correlationId);
		messageProperties2.setHeader("furmsMessageType", "AgentPingResponse");
		Message replyMessage = new Message(new byte[]{}, messageProperties2);
		rabbitTemplate.send("reply-queue", replyMessage);
	}

	public static String[] getQueuesNames() {
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<QueueName[]> forEntity = restTemplate.getForEntity("http://localhost:55570/api/latest/queue", QueueName[].class, Map.of());
		return ofNullable(forEntity.getBody()).stream()
			.flatMap(Arrays::stream)
			.filter(x -> isUUID(x.name))
			.map(x -> x.name)
			.toArray(String[]::new);
	}

	private static boolean isUUID(String s){
		try{
			UUID.fromString(s);
			return true;
		} catch (IllegalArgumentException exception){
			return false;
		}
	}
}
