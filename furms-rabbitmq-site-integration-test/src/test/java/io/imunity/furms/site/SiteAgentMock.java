/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site;

import io.imunity.furms.rabbitmq.site.models.*;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;

@Component
public class SiteAgentMock {

	private static final String MOCK_SITE_PUB = "mock-site-pub";
	private static final String MOCK_FURMS_PUB = "mock-furms-pub";
	private final RabbitTemplate rabbitTemplate;
	private final ApplicationEventPublisher publisher;
	private static final long OP_SLEEP_MS = 0; 

	public SiteAgentMock(RabbitTemplate rabbitTemplate, ApplicationEventPublisher publisher){
		this.rabbitTemplate = rabbitTemplate;
		this.publisher = publisher;
	}

	@RabbitHandler
	@RabbitListener(queues = MOCK_FURMS_PUB)
	public void receive(Payload<?> payload) {
		publisher.publishEvent(payload);
	}

	@EventListener
	public void receiveAgentPingRequest(Payload<AgentPingRequest> message) throws InterruptedException {
		TimeUnit.MILLISECONDS.sleep(OP_SLEEP_MS);
		Header header = getHeader(message.header);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, new AgentPingAck()));
	}

	@EventListener
	public void receiveAgentProjectInstallationRequest(Payload<AgentProjectInstallationRequest> projectInstallationRequest) throws InterruptedException {
		Header header = getHeader(projectInstallationRequest.header);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, new AgentProjectInstallationRequestAck()));

		TimeUnit.MILLISECONDS.sleep(OP_SLEEP_MS);

		AgentProjectInstallationResult result = new AgentProjectInstallationResult(Map.of("gid", "1"));
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, result));
	}

	@EventListener
	public void receiveAgentProjectUpdateRequest(Payload<AgentProjectUpdateRequest> projectUpdateRequest) throws InterruptedException {
		Header header = getHeader(projectUpdateRequest.header);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, new AgentProjectUpdateRequestAck()));

		TimeUnit.MILLISECONDS.sleep(OP_SLEEP_MS);

		AgentProjectUpdateResult result = new AgentProjectUpdateResult();
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, result));
	}

	@EventListener
	public void receiveAgentProjectRemovalRequest(Payload<AgentProjectRemovalRequest> projectRemovalRequest) throws InterruptedException {
		Header header = getHeader(projectRemovalRequest.header);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, new AgentProjectRemovalRequestAck()));

		TimeUnit.MILLISECONDS.sleep(OP_SLEEP_MS);

		AgentProjectRemovalResult result = new AgentProjectRemovalResult();
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, result));
	}

	@EventListener
	public void receiveAgentSSHKeyAdditionRequest(
			Payload<AgentSSHKeyAdditionRequest> agentSSHKeyInstallationRequest)
			throws InterruptedException {
		String correlationId = agentSSHKeyInstallationRequest.header.messageCorrelationId;
		Header header = new Header(VERSION, correlationId, Status.OK, null);
		rabbitTemplate.convertAndSend("mock-site-pub", new Payload<>(header, new AgentSSHKeyAdditionAck()));

		TimeUnit.MILLISECONDS.sleep(OP_SLEEP_MS);

		AgentSSHKeyAdditionResult result = new AgentSSHKeyAdditionResult();
		rabbitTemplate.convertAndSend("mock-site-pub", new Payload<>(header, result));
	}

	@EventListener
	public void receiveAgentSSHKeyUpdatingRequest(Payload<AgentSSHKeyUpdatingRequest> agentSSHKeyUpdatingRequest) throws InterruptedException {
		String correlationId = agentSSHKeyUpdatingRequest.header.messageCorrelationId;
		Header header = new Header(VERSION, correlationId, Status.OK, null);
		rabbitTemplate.convertAndSend("mock-site-pub", new Payload<>(header, new AgentSSHKeyUpdatingAck()));

		TimeUnit.MILLISECONDS.sleep(OP_SLEEP_MS);

		AgentSSHKeyUpdatingResult result = new AgentSSHKeyUpdatingResult();
		rabbitTemplate.convertAndSend("mock-site-pub", new Payload<>(header, result));
	}

	@EventListener
	public void receiveAgentSSHKeyRemovalRequest(Payload<AgentSSHKeyRemovalRequest> agentSSHKeyRemovalRequest) throws InterruptedException {
		String correlationId = agentSSHKeyRemovalRequest.header.messageCorrelationId;
		Header header = new Header(VERSION, correlationId, Status.OK, null);
		rabbitTemplate.convertAndSend("mock-site-pub", new Payload<>(header, new AgentSSHKeyRemovalAck()));

		TimeUnit.MILLISECONDS.sleep(OP_SLEEP_MS);

		AgentSSHKeyRemovalResult result = new AgentSSHKeyRemovalResult();
		rabbitTemplate.convertAndSend("mock-site-pub", new Payload<>(header, result));
	}

	@EventListener
	public void receiveAgentProjectAllocationInstallationRequest(Payload<AgentProjectAllocationInstallationRequest> projectInstallationRequest) throws InterruptedException {
		Header header = getHeader(projectInstallationRequest.header);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, new AgentProjectAllocationInstallationAck()));

		TimeUnit.MILLISECONDS.sleep(OP_SLEEP_MS);

		AgentProjectAllocationInstallationResult result = AgentProjectAllocationInstallationResult.builder()
			.projectIdentifier(projectInstallationRequest.body.projectIdentifier)
			.allocationIdentifier(projectInstallationRequest.body.allocationIdentifier)
			.allocationChunkIdentifier("1")
			.resourceType(projectInstallationRequest.body.resourceType)
			.amount(projectInstallationRequest.body.amount / 2)
			.validFrom(projectInstallationRequest.body.validFrom)
			.validTo(projectInstallationRequest.body.validTo)
			.build();
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, result));

		TimeUnit.MILLISECONDS.sleep(OP_SLEEP_MS);

		AgentProjectAllocationInstallationResult result1 = AgentProjectAllocationInstallationResult.builder()
			.projectIdentifier(projectInstallationRequest.body.projectIdentifier)
			.allocationIdentifier(projectInstallationRequest.body.allocationIdentifier)
			.allocationChunkIdentifier("2")
			.resourceType(projectInstallationRequest.body.resourceType)
			.amount(projectInstallationRequest.body.amount / 4)
			.validFrom(projectInstallationRequest.body.validFrom)
			.validTo(projectInstallationRequest.body.validTo)
			.build();
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, result1));
	}

	@EventListener
	public void receiveUserProjectAddRequest(Payload<UserProjectAddRequest> payload) throws InterruptedException {
		Header header = getHeader(payload.header);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, new UserProjectAddRequestAck()));

		TimeUnit.MILLISECONDS.sleep(OP_SLEEP_MS);

		UserProjectAddResult result = new UserProjectAddResult(payload.body.user.fenixUserId);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, result));
	}

	@EventListener
	public void receiveUserProjectRemovalRequest(Payload<UserProjectRemovalRequest> payload) throws InterruptedException {
		Header header = getHeader(payload.header);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, new UserProjectRemovalRequestAck()));

		TimeUnit.MILLISECONDS.sleep(OP_SLEEP_MS);

		UserProjectRemovalResult result = new UserProjectRemovalResult();
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, result));
	}

	@EventListener
	public void receiveAgentProjectDeallocationRequest(Payload<AgentProjectDeallocationRequest> payload) {
		Header header = getHeader(payload.header);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, new AgentProjectDeallocationRequestAck()));
	}

	@EventListener
	public void receiveUserAllocationGrantAccessRequest(Payload<UserAllocationGrantAccessRequest> payload) throws InterruptedException {
		Header header = getHeader(payload.header);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, new UserAllocationGrantAccessRequestAck()));

		TimeUnit.MILLISECONDS.sleep(OP_SLEEP_MS);

		UserAllocationGrantAccessResult result = new UserAllocationGrantAccessResult();
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, result));
	}

	@EventListener
	public void receiveUserAllocationBlockAccessRequest(Payload<UserAllocationBlockAccessRequest> payload) throws InterruptedException {
		Header header = getHeader(payload.header);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, new UserAllocationBlockAccessRequestAck()));

		TimeUnit.MILLISECONDS.sleep(OP_SLEEP_MS);

		UserAllocationBlockAccessResult result = new UserAllocationBlockAccessResult();
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, result));
	}

	private Header getHeader(Header header) {
		return new Header(VERSION, header.messageCorrelationId, Status.OK, null);
	}
}
