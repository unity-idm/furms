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
		TimeUnit.SECONDS.sleep(5);
		Header header = getHeader(message.header);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, new AgentPingAck()));
	}

	@EventListener
	public void receiveAgentProjectInstallationRequest(Payload<AgentProjectInstallationRequest> projectInstallationRequest) throws InterruptedException {
		Header header = getHeader(projectInstallationRequest.header);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, new AgentProjectInstallationAck()));

		TimeUnit.SECONDS.sleep(5);

		AgentProjectInstallationResult result = new AgentProjectInstallationResult(projectInstallationRequest.body.identifier, Map.of("gid", "1"));
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, result));
	}

	@EventListener
	public void receiveAgentProjectUpdateRequest(Payload<AgentProjectUpdateRequest> projectUpdateRequest) throws InterruptedException {
		Header header = getHeader(projectUpdateRequest.header);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, new AgentProjectUpdateRequestAck()));

		TimeUnit.SECONDS.sleep(5);

		AgentProjectUpdateResult result = new AgentProjectUpdateResult(projectUpdateRequest.body.identifier);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, result));
	}

	@EventListener
	public void receiveAgentProjectRemovalRequest(Payload<AgentProjectRemovalRequest> projectRemovalRequest) throws InterruptedException {
		Header header = getHeader(projectRemovalRequest.header);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, new AgentProjectRemovalRequestAck()));

		TimeUnit.SECONDS.sleep(5);

		AgentProjectRemovalResult result = new AgentProjectRemovalResult(projectRemovalRequest.body.identifier);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, result));
	}

	@EventListener
	public void receiveAgentSSHKeyAdditionRequest(
			Payload<AgentSSHKeyAdditionRequest> agentSSHKeyInstallationRequest)
			throws InterruptedException {
		String correlationId = agentSSHKeyInstallationRequest.header.messageCorrelationId;
		Header header = new Header(VERSION, correlationId, Status.OK, null);
		rabbitTemplate.convertAndSend("mock-site-pub", new Payload<>(header, new AgentSSHKeyAdditionAck()));

		TimeUnit.SECONDS.sleep(5);

		AgentSSHKeyAdditionResult result = new AgentSSHKeyAdditionResult();
		rabbitTemplate.convertAndSend("mock-site-pub", new Payload<>(header, result));
	}

	@EventListener
	public void receiveAgentSSHKeyUpdatingRequest(Payload<AgentSSHKeyUpdatingRequest> agentSSHKeyUpdatingRequest) throws InterruptedException {
		String correlationId = agentSSHKeyUpdatingRequest.header.messageCorrelationId;
		Header header = new Header(VERSION, correlationId, Status.OK, null);
		rabbitTemplate.convertAndSend("mock-site-pub", new Payload<>(header, new AgentSSHKeyUpdatingAck()));

		TimeUnit.SECONDS.sleep(5);

		AgentSSHKeyUpdatingResult result = new AgentSSHKeyUpdatingResult();
		rabbitTemplate.convertAndSend("mock-site-pub", new Payload<>(header, result));
	}

	@EventListener
	public void receiveAgentSSHKeyRemovalRequest(Payload<AgentSSHKeyRemovalRequest> agentSSHKeyRemovalRequest) throws InterruptedException {
		String correlationId = agentSSHKeyRemovalRequest.header.messageCorrelationId;
		Header header = new Header(VERSION, correlationId, Status.OK, null);
		rabbitTemplate.convertAndSend("mock-site-pub", new Payload<>(header, new AgentSSHKeyRemovalAck()));

		TimeUnit.SECONDS.sleep(5);

		AgentSSHKeyRemovalResult result = new AgentSSHKeyRemovalResult();
		rabbitTemplate.convertAndSend("mock-site-pub", new Payload<>(header, result));
	}

	@EventListener
	public void receiveAgentProjectAllocationInstallationRequest(Payload<AgentProjectAllocationInstallationRequest> projectInstallationRequest) throws InterruptedException {
		Header header = getHeader(projectInstallationRequest.header);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, new AgentProjectAllocationInstallationAck()));

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
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, result));

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
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, result1));
	}

	@EventListener
	public void receiveUserProjectAddRequest(Payload<UserProjectAddRequest> payload) throws InterruptedException {
		Header header = getHeader(payload.header);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, new UserProjectAddRequestAck()));

		TimeUnit.SECONDS.sleep(5);

		UserProjectAddResult result = new UserProjectAddResult(payload.body.user.fenixUserId, "1", payload.body.projectIdentifier);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, result));
	}

	@EventListener
	public void receiveUserProjectRemovalRequest(Payload<UserProjectRemovalRequest> payload) throws InterruptedException {
		Header header = getHeader(payload.header);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, new UserProjectRemovalRequestAck()));

		TimeUnit.SECONDS.sleep(5);

		UserProjectRemovalResult result = new UserProjectRemovalResult();
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, result));
	}

	private Header getHeader(Header header) {
		return new Header(VERSION, header.messageCorrelationId, Status.OK, null);
	}
}
