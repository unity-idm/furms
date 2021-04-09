/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.agent.runner;

import io.imunity.furms.rabbitmq.site.client.ProjectInstallationRequest;
import io.imunity.furms.rabbitmq.site.client.TypeHeaderAppender;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.messaging.handler.annotation.Header;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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

	@RabbitListener(queues = "${queue.name}")
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


	@RabbitHandler
	@RabbitListener(queues = "${queue.name}")
	public void receive(ProjectInstallationRequest projectInstallationRequest, @Header("correlationId") String correlationId) throws InterruptedException {
		MessageProperties messageProperties = new MessageProperties();
		messageProperties.setHeader("version", 1);
		messageProperties.setHeader("status", "IN_PROGRESS");
		messageProperties.setCorrelationId(correlationId);
		messageProperties.setHeader("furmsMessageType", "ProjectInstallationResult");
		Message replyAckMessage = new Message(new byte[]{}, messageProperties);
		rabbitTemplate.send("reply-queue", replyAckMessage);

		TimeUnit.SECONDS.sleep(5);

		String i = String.valueOf(new Random().nextInt(1000));
		ProjectInstallationResult result = new ProjectInstallationResult(projectInstallationRequest.id, Map.of("gid", i));
		rabbitTemplate.convertAndSend("reply-queue", result, new TypeHeaderAppender(result, correlationId));
	}
}
