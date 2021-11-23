/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.agent.runner;

import io.imunity.furms.rabbitmq.site.models.AgentPingAck;
import io.imunity.furms.rabbitmq.site.models.AgentPingRequest;
import io.imunity.furms.rabbitmq.site.models.AgentPolicyUpdate;
import io.imunity.furms.rabbitmq.site.models.AgentPolicyUpdateAck;
import io.imunity.furms.rabbitmq.site.models.AgentProjectAllocationInstallationAck;
import io.imunity.furms.rabbitmq.site.models.AgentProjectAllocationInstallationRequest;
import io.imunity.furms.rabbitmq.site.models.AgentProjectAllocationInstallationResult;
import io.imunity.furms.rabbitmq.site.models.AgentProjectDeallocationRequest;
import io.imunity.furms.rabbitmq.site.models.AgentProjectDeallocationRequestAck;
import io.imunity.furms.rabbitmq.site.models.AgentProjectInstallationRequest;
import io.imunity.furms.rabbitmq.site.models.AgentProjectInstallationRequestAck;
import io.imunity.furms.rabbitmq.site.models.AgentProjectInstallationResult;
import io.imunity.furms.rabbitmq.site.models.AgentProjectRemovalRequest;
import io.imunity.furms.rabbitmq.site.models.AgentProjectRemovalRequestAck;
import io.imunity.furms.rabbitmq.site.models.AgentProjectRemovalResult;
import io.imunity.furms.rabbitmq.site.models.AgentProjectUpdateRequest;
import io.imunity.furms.rabbitmq.site.models.AgentProjectUpdateRequestAck;
import io.imunity.furms.rabbitmq.site.models.AgentProjectUpdateResult;
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
import io.imunity.furms.rabbitmq.site.models.SetUserStatusRequest;
import io.imunity.furms.rabbitmq.site.models.SetUserStatusRequestAck;
import io.imunity.furms.rabbitmq.site.models.SetUserStatusResult;
import io.imunity.furms.rabbitmq.site.models.Status;
import io.imunity.furms.rabbitmq.site.models.UserAllocationBlockAccessRequest;
import io.imunity.furms.rabbitmq.site.models.UserAllocationBlockAccessRequestAck;
import io.imunity.furms.rabbitmq.site.models.UserAllocationBlockAccessResult;
import io.imunity.furms.rabbitmq.site.models.UserAllocationGrantAccessRequest;
import io.imunity.furms.rabbitmq.site.models.UserAllocationGrantAccessRequestAck;
import io.imunity.furms.rabbitmq.site.models.UserAllocationGrantAccessResult;
import io.imunity.furms.rabbitmq.site.models.UserPolicyAcceptanceUpdate;
import io.imunity.furms.rabbitmq.site.models.UserPolicyAcceptanceUpdateAck;
import io.imunity.furms.rabbitmq.site.models.UserProjectAddRequest;
import io.imunity.furms.rabbitmq.site.models.UserProjectAddRequestAck;
import io.imunity.furms.rabbitmq.site.models.UserProjectAddResult;
import io.imunity.furms.rabbitmq.site.models.UserProjectRemovalRequest;
import io.imunity.furms.rabbitmq.site.models.UserProjectRemovalRequestAck;
import io.imunity.furms.rabbitmq.site.models.UserProjectRemovalResult;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;

@Component
class MockListeners {
	private final RabbitTemplate rabbitTemplate;
	private final ApplicationEventPublisher publisher;
	private final Set<String> allocationIdentifiers = new HashSet<>();
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
	public void receivePolicyUpdate(Payload<AgentPolicyUpdate> message) throws InterruptedException {
		TimeUnit.SECONDS.sleep(5);

		Header header = getHeader(message.header);
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, new AgentPolicyUpdateAck()));
	}

	@EventListener
	public void receivePolicyAcceptanceUpdate(Payload<UserPolicyAcceptanceUpdate> message) throws InterruptedException {
		TimeUnit.SECONDS.sleep(5);

		Header header = getHeader(message.header);
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, new UserPolicyAcceptanceUpdateAck()));
	}

	@EventListener
	public void receiveAgentProjectInstallationRequest(
			Payload<AgentProjectInstallationRequest> projectInstallationRequest)
			throws InterruptedException {
		String correlationId = projectInstallationRequest.header.messageCorrelationId;
		Header header = new Header(VERSION, correlationId, Status.OK, null);
		rabbitTemplate.convertAndSend(responseQueueName,
				new Payload<>(header, new AgentProjectInstallationRequestAck()));

		TimeUnit.SECONDS.sleep(5);

		String i = String.valueOf(new Random().nextInt(1000));
		AgentProjectInstallationResult result = new AgentProjectInstallationResult(Map.of("gid", i));
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, result));
	}

	@EventListener
	public void receiveAgentProjectUpdateRequest(Payload<AgentProjectUpdateRequest> projectUpdateRequest) throws InterruptedException {
		Header header = getHeader(projectUpdateRequest.header);
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, new AgentProjectUpdateRequestAck()));

		TimeUnit.SECONDS.sleep(5);

		AgentProjectUpdateResult result = new AgentProjectUpdateResult();
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, result));
	}

	@EventListener
	public void receiveAgentProjectRemovalRequest(Payload<AgentProjectRemovalRequest> projectRemovalRequest) throws InterruptedException {
		Header header = getHeader(projectRemovalRequest.header);
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, new AgentProjectRemovalRequestAck()));

		TimeUnit.SECONDS.sleep(5);

		AgentProjectRemovalResult result = new AgentProjectRemovalResult();
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

		AgentSSHKeyAdditionResult result = new AgentSSHKeyAdditionResult();
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, result));
	}

	@EventListener
	public void receiveAgentSSHKeyUpdatingRequest(Payload<AgentSSHKeyUpdatingRequest> agentSSHKeyUpdatingRequest)
			throws InterruptedException {
		String correlationId = agentSSHKeyUpdatingRequest.header.messageCorrelationId;
		Header header = new Header(VERSION, correlationId, Status.OK, null);
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, new AgentSSHKeyUpdatingAck()));

		TimeUnit.SECONDS.sleep(5);

		AgentSSHKeyUpdatingResult result = new AgentSSHKeyUpdatingResult();
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, result));
	}

	@EventListener
	public void receiveAgentSSHKeyRemovalRequest(Payload<AgentSSHKeyRemovalRequest> agentSSHKeyRemovalRequest)
			throws InterruptedException {
		String correlationId = agentSSHKeyRemovalRequest.header.messageCorrelationId;
		Header header = new Header(VERSION, correlationId, Status.OK, null);
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, new AgentSSHKeyRemovalAck()));

		TimeUnit.SECONDS.sleep(5);

		AgentSSHKeyRemovalResult result = new AgentSSHKeyRemovalResult();
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, result));
	}

	@EventListener
	public void receiveAgentProjectAllocationInstallationRequest(Payload<AgentProjectAllocationInstallationRequest> projectInstallationRequest) throws InterruptedException {
		Header header = getHeader(projectInstallationRequest.header);
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, new AgentProjectAllocationInstallationAck()));
		if(allocationIdentifiers.contains(projectInstallationRequest.body.allocationIdentifier))
			return;
		allocationIdentifiers.add(projectInstallationRequest.body.allocationIdentifier);

		TimeUnit.SECONDS.sleep(5);

		AgentProjectAllocationInstallationResult result = AgentProjectAllocationInstallationResult.builder()
			.allocationIdentifier(projectInstallationRequest.body.allocationIdentifier)
			.allocationChunkIdentifier("1")
			.resourceType(projectInstallationRequest.body.resourceType)
			.amount(projectInstallationRequest.body.amount.divide(BigDecimal.valueOf(2), RoundingMode.CEILING))
			.validFrom(projectInstallationRequest.body.validFrom)
			.validTo(projectInstallationRequest.body.validTo)
			.build();
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, result));

		TimeUnit.SECONDS.sleep(5);

		AgentProjectAllocationInstallationResult result1 = AgentProjectAllocationInstallationResult.builder()
			.allocationIdentifier(projectInstallationRequest.body.allocationIdentifier)
			.allocationChunkIdentifier("2")
			.resourceType(projectInstallationRequest.body.resourceType)
			.amount(projectInstallationRequest.body.amount.divide(BigDecimal.valueOf(4), RoundingMode.CEILING))
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

		UserProjectAddResult result = new UserProjectAddResult("unix-user" + payload.body.user.fenixUserId.hashCode());
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

		UserProjectRemovalResult result = new UserProjectRemovalResult();
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, result));
	}

	@EventListener
	public void receiveUserAllocationGrantAccessRequest(Payload<UserAllocationGrantAccessRequest> payload) throws InterruptedException {
		Header header = getHeader(payload.header);
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, new UserAllocationGrantAccessRequestAck()));

		TimeUnit.SECONDS.sleep(5);

		UserAllocationGrantAccessResult result = new UserAllocationGrantAccessResult();
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, result));
	}

	@EventListener
	public void receiveUserAllocationBlockAccessRequest(Payload<UserAllocationBlockAccessRequest> payload) throws InterruptedException {
		Header header = getHeader(payload.header);
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, new UserAllocationBlockAccessRequestAck()));

		TimeUnit.SECONDS.sleep(5);

		UserAllocationBlockAccessResult result = new UserAllocationBlockAccessResult();
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, result));
	}

	@EventListener
	public void receiveSetUserStatusRequest(Payload<SetUserStatusRequest> payload) throws InterruptedException {
		Header header = getHeader(payload.header);
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, new SetUserStatusRequestAck()));

		TimeUnit.SECONDS.sleep(5);

		SetUserStatusResult result = new SetUserStatusResult();
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, result));
	}

	private Header getHeader(Header header) {
		return new Header(VERSION, header.messageCorrelationId, Status.OK, null);
	}
}
