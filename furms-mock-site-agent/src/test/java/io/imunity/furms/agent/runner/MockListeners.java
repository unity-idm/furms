/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.agent.runner;

import io.imunity.furms.rabbitmq.site.models.*;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
class MockListeners {
	private final RabbitTemplate rabbitTemplate;
	private final ApplicationEventPublisher publisher;
	@Value("${queue.res-name}")
	private String responseQueueName;
	public MockListeners(RabbitTemplate rabbitTemplate, ApplicationEventPublisher publisher){
		this.rabbitTemplate = rabbitTemplate;
		this.publisher = publisher;
	}

	@RabbitHandler
	@RabbitListener(queues = "${queue.req-name}")
	public void receive(Payload<?> payload) {
		publisher.publishEvent(payload);
	}

	@EventListener
	public void receiveAgentPingRequest(Payload<AgentPingRequest> message) throws InterruptedException {
		TimeUnit.SECONDS.sleep(5);

		String correlationId = message.header.messageCorrelationId;
		Header header = new Header("1", correlationId, Status.OK, null);
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, new AgentPingAck()));
	}

	@EventListener
	public void receiveAgentProjectInstallationRequest(Payload<AgentProjectInstallationRequest> projectInstallationRequest) throws InterruptedException {
		String correlationId = projectInstallationRequest.header.messageCorrelationId;
		Header header = new Header("1", correlationId, Status.OK, null);
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, new AgentProjectInstallationAck()));

		TimeUnit.SECONDS.sleep(5);

		String i = String.valueOf(new Random().nextInt(1000));
		AgentProjectInstallationResult result = new AgentProjectInstallationResult(projectInstallationRequest.body.identifier, Map.of("gid", i));
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, result));
	}

	@EventListener
	public void receiveAgentProjectAllocationInstallationRequest(Payload<AgentProjectAllocationInstallationRequest> projectInstallationRequest) throws InterruptedException {
		String correlationId = projectInstallationRequest.header.messageCorrelationId;
		Header header = new Header("1", correlationId, Status.OK, null);
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, new AgentProjectAllocationInstallationAck()));

		TimeUnit.SECONDS.sleep(5);

		AgentProjectAllocationInstallationResult result = AgentProjectAllocationInstallationResult.builder()
			.projectIdentifier(projectInstallationRequest.body.projectIdentifier)
			.allocationIdentifier(projectInstallationRequest.body.allocationIdentifier)
			.allocationChunkIdentifier("1")
			.resourceType(projectInstallationRequest.body.resourceType)
			.amount(projectInstallationRequest.body.amount / 2)
			.validFrom(projectInstallationRequest.body.validFrom)
			.validTo(projectInstallationRequest.body.validTo)
			.build();
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, result));

		TimeUnit.SECONDS.sleep(5);

		AgentProjectAllocationInstallationResult result1 = AgentProjectAllocationInstallationResult.builder()
			.projectIdentifier(projectInstallationRequest.body.projectIdentifier)
			.allocationIdentifier(projectInstallationRequest.body.allocationIdentifier)
			.allocationChunkIdentifier("2")
			.resourceType(projectInstallationRequest.body.resourceType)
			.amount(projectInstallationRequest.body.amount / 4)
			.validFrom(projectInstallationRequest.body.validFrom)
			.validTo(projectInstallationRequest.body.validTo)
			.build();
		Header header1 = new Header("1", UUID.randomUUID().toString(), Status.OK, null);
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header1, result1));
	}
}
