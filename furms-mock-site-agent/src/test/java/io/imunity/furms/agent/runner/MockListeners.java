/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.agent.runner;

import io.imunity.furms.rabbitmq.site.models.AgentPingRequest;
import io.imunity.furms.rabbitmq.site.models.AgentProjectInstallationRequest;
import io.imunity.furms.rabbitmq.site.models.AgentProjectInstallationResult;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyAdditionRequest;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyAdditionResult;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyAdditionResult.AgentSSHKeyAdditionResultBuilder;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyRemovalResult.AgentSSHKeyRemovalResultBuilder;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyUpdatingRequest;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyUpdatingResult;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyUpdatingResult.AgentSSHKeyUpdatingResultBuilder;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyRemovalRequest;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyRemovalResult;
import io.imunity.furms.rabbitmq.site.models.converter.TypeHeaderAppender;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static io.imunity.furms.rabbitmq.site.models.consts.Headers.CORRELATION_ID;

@Component
@RabbitListener(queues = "${queue.name}")
class MockListeners {


	private final RabbitTemplate rabbitTemplate;

	public MockListeners(RabbitTemplate rabbitTemplate){
		this.rabbitTemplate = rabbitTemplate;
	}

	@RabbitHandler
	public void receive(AgentPingRequest message) throws InterruptedException {
		String correlationId = message.correlationId;

		MessageProperties messageProperties = new MessageProperties();
		messageProperties.setHeader("version", 1);
		messageProperties.setHeader("status", "IN_PROGRESS");
		messageProperties.setCorrelationId(correlationId);
		messageProperties.setHeader("furmsMessageType", "AgentPingAck");
		Message replyAckMessage = new Message(new byte[]{}, messageProperties);
		rabbitTemplate.send("reply-queue", replyAckMessage);

		TimeUnit.SECONDS.sleep(5);

		MessageProperties messageProperties2 = new MessageProperties();
		messageProperties2.setHeader("version", 1);
		messageProperties2.setHeader("status", "OK");
		messageProperties2.setCorrelationId(correlationId);
		messageProperties2.setHeader("furmsMessageType", "AgentPingResult");
		Message replyMessage = new Message(new byte[]{}, messageProperties2);
		rabbitTemplate.send("reply-queue", replyMessage);
	}


	@RabbitHandler
	public void receive(AgentProjectInstallationRequest projectInstallationRequest, @Headers Map<String,Object> headers) throws InterruptedException {
		String correlationId = headers.get(CORRELATION_ID).toString();
		MessageProperties messageProperties = new MessageProperties();
		messageProperties.setHeader("version", 1);
		messageProperties.setHeader("status", "IN_PROGRESS");
		messageProperties.setCorrelationId(correlationId);
		messageProperties.setHeader("furmsMessageType", "ProjectInstallationAck");
		Message replyAckMessage = new Message(new byte[]{}, messageProperties);
		rabbitTemplate.send("reply-queue", replyAckMessage);

		TimeUnit.SECONDS.sleep(5);

		String i = String.valueOf(new Random().nextInt(1000));
		AgentProjectInstallationResult result = new AgentProjectInstallationResult(projectInstallationRequest.id, Map.of("gid", i));
		rabbitTemplate.convertAndSend("reply-queue", result, new TypeHeaderAppender(result, correlationId));
	}
	
	@RabbitHandler
	public void receive(AgentSSHKeyAdditionRequest agentSSHKeyInstallationRequest, @Headers Map<String,Object> headers) throws InterruptedException {
		String correlationId = headers.get(CORRELATION_ID).toString();
		MessageProperties messageProperties = new MessageProperties();
		messageProperties.setHeader("version", 1);
		messageProperties.setHeader("status", "IN_PROGRESS");
		messageProperties.setCorrelationId(correlationId);
		messageProperties.setHeader("furmsMessageType", "UserSSHKeyAddAck");
		Message replyAckMessage = new Message(new byte[]{}, messageProperties);
		rabbitTemplate.send("reply-queue", replyAckMessage);

		TimeUnit.SECONDS.sleep(5);
		
		AgentSSHKeyAdditionResult result = AgentSSHKeyAdditionResultBuilder
				.anAgentSSHKeyAdditionResultBuilder()
				.fenixUserId(agentSSHKeyInstallationRequest.fenixUserId)
				.uid(agentSSHKeyInstallationRequest.uid).build();
		rabbitTemplate.convertAndSend("reply-queue", result, new TypeHeaderAppender(result, correlationId, "OK"));
	}
	
	@RabbitHandler
	public void receive(AgentSSHKeyUpdatingRequest agentSSHKeyUpdatingRequest, @Headers Map<String,Object> headers) throws InterruptedException {
		String correlationId = headers.get(CORRELATION_ID).toString();
		MessageProperties messageProperties = new MessageProperties();
		messageProperties.setHeader("version", 1);
		messageProperties.setHeader("status", "IN_PROGRESS");
		messageProperties.setCorrelationId(correlationId);
		messageProperties.setHeader("furmsMessageType", "UserSSHKeyUpdateAck");
		Message replyAckMessage = new Message(new byte[]{}, messageProperties);
		rabbitTemplate.send("reply-queue", replyAckMessage);

		TimeUnit.SECONDS.sleep(5);
		
		AgentSSHKeyUpdatingResult result = AgentSSHKeyUpdatingResultBuilder
				.anAgentSSHKeyUpdatingResultBuilder()
				.fenixUserId(agentSSHKeyUpdatingRequest.fenixUserId)
				.uid(agentSSHKeyUpdatingRequest.uid).build();
		rabbitTemplate.convertAndSend("reply-queue", result, new TypeHeaderAppender(result, correlationId, "OK"));
	}
	
	
	@RabbitHandler
	public void receive(AgentSSHKeyRemovalRequest agentSSHKeyRemovalRequest, @Headers Map<String,Object> headers) throws InterruptedException {
		String correlationId = headers.get(CORRELATION_ID).toString();
		MessageProperties messageProperties = new MessageProperties();
		messageProperties.setHeader("version", 1);
		messageProperties.setHeader("status", "IN_PROGRESS");
		messageProperties.setCorrelationId(correlationId);
		messageProperties.setHeader("furmsMessageType", "UserSSHKeyRemovalAck");
		Message replyAckMessage = new Message(new byte[]{}, messageProperties);
		rabbitTemplate.send("reply-queue", replyAckMessage);

		TimeUnit.SECONDS.sleep(5);
		
		AgentSSHKeyRemovalResult result = AgentSSHKeyRemovalResultBuilder
				.anAgentSSHKeyRemovalResultBuilder()
				.fenixUserId(agentSSHKeyRemovalRequest.fenixUserId)
				.uid(agentSSHKeyRemovalRequest.uid).build();
		rabbitTemplate.convertAndSend("reply-queue", result, new TypeHeaderAppender(result, correlationId, "OK"));
	}
}
