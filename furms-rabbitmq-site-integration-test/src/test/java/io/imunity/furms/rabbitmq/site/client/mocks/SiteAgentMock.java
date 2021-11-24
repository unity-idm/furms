/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.mocks;

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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;

@Component
public class SiteAgentMock {

	public static final String MOCK_SITE_PUB = "mock-site-pub";
	public static final String MOCK_FURMS_PUB = "mock-furms-pub";
	private final RabbitTemplate rabbitTemplate;
	private final ApplicationEventPublisher publisher;
	private final SiteAgentPolicyDocumentReceiverMock siteAgentPolicyDocumentReceiverMock;
	private static final long OP_SLEEP_MS = 0;

	public SiteAgentMock(RabbitTemplate rabbitTemplate, ApplicationEventPublisher publisher, SiteAgentPolicyDocumentReceiverMock siteAgentPolicyDocumentReceiverMock){
		this.rabbitTemplate = rabbitTemplate;
		this.publisher = publisher;
		this.siteAgentPolicyDocumentReceiverMock = siteAgentPolicyDocumentReceiverMock;
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
	public void receiveAgentPolicyUpdate(Payload<AgentPolicyUpdate> message) throws InterruptedException {
		TimeUnit.MILLISECONDS.sleep(OP_SLEEP_MS);
		siteAgentPolicyDocumentReceiverMock.process(message.body);
	}

	@EventListener
	public void receiveUserPolicyAcceptanceUpdate(Payload<UserPolicyAcceptanceUpdate> message) throws InterruptedException {
		TimeUnit.MILLISECONDS.sleep(OP_SLEEP_MS);
		siteAgentPolicyDocumentReceiverMock.process(message.body);
	}

	@EventListener
	public void receivePolicyUpdate(Payload<AgentPolicyUpdate> message) throws InterruptedException {
		TimeUnit.SECONDS.sleep(OP_SLEEP_MS);

		Header header = getHeader(message.header);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, new AgentPolicyUpdateAck()));
	}

	@EventListener
	public void receivePolicyAcceptanceUpdate(Payload<UserPolicyAcceptanceUpdate> message) throws InterruptedException {
		TimeUnit.SECONDS.sleep(OP_SLEEP_MS);

		Header header = getHeader(message.header);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, new UserPolicyAcceptanceUpdateAck()));
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
			.allocationIdentifier(projectInstallationRequest.body.allocationIdentifier)
			.allocationChunkIdentifier("1")
			.resourceType(projectInstallationRequest.body.resourceType)
			.amount(projectInstallationRequest.body.amount.divide(BigDecimal.valueOf(2), RoundingMode.CEILING))
			.validFrom(projectInstallationRequest.body.validFrom)
			.validTo(projectInstallationRequest.body.validTo)
			.build();
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, result));

		TimeUnit.MILLISECONDS.sleep(OP_SLEEP_MS);

		AgentProjectAllocationInstallationResult result1 = AgentProjectAllocationInstallationResult.builder()
			.allocationIdentifier(projectInstallationRequest.body.allocationIdentifier)
			.allocationChunkIdentifier("2")
			.resourceType(projectInstallationRequest.body.resourceType)
			.amount(projectInstallationRequest.body.amount.divide(BigDecimal.valueOf(4), RoundingMode.CEILING))
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

	@EventListener
	public void receiveSetUserStatusRequest(Payload<SetUserStatusRequest> payload) throws InterruptedException {
		Header header = getHeader(payload.header);
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, new SetUserStatusRequestAck()));

		TimeUnit.MILLISECONDS.sleep(OP_SLEEP_MS);

		SetUserStatusResult result = new SetUserStatusResult();
		rabbitTemplate.convertAndSend(MOCK_SITE_PUB, new Payload<>(header, result));
	}

	private Header getHeader(Header header) {
		return new Header(VERSION, header.messageCorrelationId, Status.OK, null);
	}
}
