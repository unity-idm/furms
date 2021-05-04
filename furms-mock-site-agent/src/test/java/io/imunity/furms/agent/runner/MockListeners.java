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

import io.imunity.furms.rabbitmq.site.models.AgentPingAck;
import io.imunity.furms.rabbitmq.site.models.AgentPingRequest;
import io.imunity.furms.rabbitmq.site.models.AgentProjectInstallationAck;
import io.imunity.furms.rabbitmq.site.models.AgentProjectInstallationRequest;
import io.imunity.furms.rabbitmq.site.models.AgentProjectInstallationResult;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyAdditionAck;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyAdditionRequest;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyAdditionResult;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyRemovalAck;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyRemovalRequest;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyRemovalResult;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyUpdatingAck;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyUpdatingRequest;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyUpdatingResult;
import io.imunity.furms.rabbitmq.site.models.Header;
import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.rabbitmq.site.models.Status;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;

@Component
class MockListeners {
	private final RabbitTemplate rabbitTemplate;
	private final ApplicationEventPublisher publisher;
	@Value("${queue.res-name}")
	private String responseQueueName;

	public MockListeners(RabbitTemplate rabbitTemplate, ApplicationEventPublisher publisher) {
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

		Header header = getHeader(message.header);
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, new AgentPingAck()));
	}

	@EventListener
	public void receiveAgentProjectInstallationRequest(
			Payload<AgentProjectInstallationRequest> projectInstallationRequest)
			throws InterruptedException {
		String correlationId = projectInstallationRequest.header.messageCorrelationId;
		Header header = new Header(VERSION, correlationId, Status.OK, null);
		rabbitTemplate.convertAndSend(responseQueueName,
				new Payload<>(header, new AgentProjectInstallationAck()));

		TimeUnit.SECONDS.sleep(5);

		String i = String.valueOf(new Random().nextInt(1000));
		AgentProjectInstallationResult result = new AgentProjectInstallationResult(
				projectInstallationRequest.body.identifier, Map.of("gid", i));
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, result));
	}

	@EventListener
	public void receiveAgentSSHKeyAdditionRequest(
			Payload<AgentSSHKeyAdditionRequest> agentSSHKeyInstallationRequest)
			throws InterruptedException {
		String correlationId = agentSSHKeyInstallationRequest.header.messageCorrelationId;
		Header header = new Header(VERSION, correlationId, Status.OK, null);
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, new AgentSSHKeyAdditionAck()));

		TimeUnit.SECONDS.sleep(5);

		AgentSSHKeyAdditionResult result = new AgentSSHKeyAdditionResult(
				agentSSHKeyInstallationRequest.body.fenixUserId,
				agentSSHKeyInstallationRequest.body.uid);
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, result));
	}

	@EventListener
	public void receiveAgentSSHKeyUpdatingRequest(Payload<AgentSSHKeyUpdatingRequest> agentSSHKeyUpdatingRequest)
			throws InterruptedException {
		String correlationId = agentSSHKeyUpdatingRequest.header.messageCorrelationId;
		Header header = new Header(VERSION, correlationId, Status.OK, null);
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, new AgentSSHKeyUpdatingAck()));

		TimeUnit.SECONDS.sleep(5);

		AgentSSHKeyUpdatingResult result = new AgentSSHKeyUpdatingResult(
				agentSSHKeyUpdatingRequest.body.fenixUserId, agentSSHKeyUpdatingRequest.body.uid);
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, result));
	}

	@EventListener
	public void receiveAgentSSHKeyRemovalRequest(Payload<AgentSSHKeyRemovalRequest> agentSSHKeyRemovalRequest)
			throws InterruptedException {
		String correlationId = agentSSHKeyRemovalRequest.header.messageCorrelationId;
		Header header = new Header(VERSION, correlationId, Status.OK, null);
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, new AgentSSHKeyRemovalAck()));

		TimeUnit.SECONDS.sleep(5);

		AgentSSHKeyRemovalResult result = new AgentSSHKeyRemovalResult(
				agentSSHKeyRemovalRequest.body.fenixUserId, agentSSHKeyRemovalRequest.body.uid);
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, result));
	}

	@EventListener
	public void receiveAgentProjectAllocationInstallationRequest(Payload<AgentProjectAllocationInstallationRequest> projectInstallationRequest) throws InterruptedException {
		Header header = getHeader(projectInstallationRequest.header);
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
		Header header1 = new Header(VERSION, UUID.randomUUID().toString(), Status.OK, null);
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header1, result1));
	}

	@EventListener
	public void receiveUserProjectAddRequest(Payload<UserProjectAddRequest> payload) throws InterruptedException {
		Header header = getHeader(payload.header);
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, new UserProjectAddRequestAck()));

		TimeUnit.SECONDS.sleep(5);

		UserProjectAddResult result = new UserProjectAddResult(payload.body.user.fenixUserId, "1", payload.body.projectIdentifier);
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, result));
	}

	@EventListener
	public void receiveAgentProjectDeallocationRequest(Payload<AgentProjectDeallocationRequest> payload) {
		Header header = getHeader(payload.header);
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, new AgentProjectDeallocationRequestAck()));
	}

	@EventListener
	public void receiveUserProjectRemovalRequest(Payload<UserProjectRemovalRequest> payload) throws InterruptedException {
		Header header = getHeader(payload.header);
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, new UserProjectRemovalRequestAck()));

		TimeUnit.SECONDS.sleep(5);

		UserProjectRemovalResult result = new UserProjectRemovalResult(payload.body.fenixUserId, "1", payload.body.projectIdentifier);
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, result));
	}

	private Header getHeader(Header header) {
		return new Header(VERSION, header.messageCorrelationId, Status.OK, null);
	}
}
