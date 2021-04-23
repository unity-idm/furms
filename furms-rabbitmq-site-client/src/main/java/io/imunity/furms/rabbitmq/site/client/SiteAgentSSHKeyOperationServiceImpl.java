/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import static io.imunity.furms.domain.ssh_key_operation.SSHKeyOperationStatus.ACK;
import static io.imunity.furms.domain.ssh_key_operation.SSHKeyOperationStatus.DONE;
import static io.imunity.furms.domain.ssh_key_operation.SSHKeyOperationStatus.FAILED;
import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getFurmsPublishQueueName;
import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;

import java.util.Optional;

import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent.SiteAgentException;
import io.imunity.furms.domain.ssh_key_operation.SSHKeyAddition;
import io.imunity.furms.domain.ssh_key_operation.SSHKeyRemoval;
import io.imunity.furms.domain.ssh_key_operation.SSHKeyUpdating;
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
import io.imunity.furms.site.api.message_resolver.SSHKeyOperationMessageResolver;
import io.imunity.furms.site.api.site_agent.SiteAgentSSHKeyOperationService;

@Service
class SiteAgentSSHKeyOperationServiceImpl implements SiteAgentSSHKeyOperationService {

	private final RabbitTemplate rabbitTemplate;
	private final SSHKeyOperationMessageResolver sshKeyOperationService;

	SiteAgentSSHKeyOperationServiceImpl(RabbitTemplate rabbitTemplate,
			SSHKeyOperationMessageResolver sshKeyInstallationService) {

		this.rabbitTemplate = rabbitTemplate;
		this.sshKeyOperationService = sshKeyInstallationService;
	}

	@EventListener
	void receiveAgentSSHKeyAdditionAck(Payload<AgentSSHKeyAdditionAck> ack) {

		if (ack.header.status.equals(Status.FAILED)) {
			sshKeyOperationService.updateStatus(new CorrelationId(ack.header.messageCorrelationId), FAILED,
					Optional.ofNullable(ack.header.error.message));
		}
		sshKeyOperationService.updateStatus(new CorrelationId(ack.header.messageCorrelationId), ACK,
				Optional.empty());
	}

	@EventListener
	void receiveAgentSSHKeyRemovalAck(Payload<AgentSSHKeyRemovalAck> ack) {
		if (ack.header.status.equals(Status.FAILED)) {
			sshKeyOperationService.updateStatus(new CorrelationId(ack.header.messageCorrelationId), FAILED,
					Optional.ofNullable(ack.header.error.message));
		}
		sshKeyOperationService.updateStatus(new CorrelationId(ack.header.messageCorrelationId), ACK,
				Optional.empty());
	}

	@EventListener
	void receiveAgentSSHKeyUpdatingAck(Payload<AgentSSHKeyUpdatingAck> ack) {
		if (ack.header.status.equals(Status.FAILED)) {
			sshKeyOperationService.updateStatus(new CorrelationId(ack.header.messageCorrelationId), FAILED,
					Optional.ofNullable(ack.header.error.message));
		}
		sshKeyOperationService.updateStatus(new CorrelationId(ack.header.messageCorrelationId), ACK,
				Optional.empty());
	}

	@EventListener
	void receiveAgentSSHKeyAdditionResult(Payload<AgentSSHKeyAdditionResult> result) {
		if (result.header.status.equals(Status.FAILED)) {
			sshKeyOperationService.updateStatus(new CorrelationId(result.header.messageCorrelationId),
					FAILED, Optional.ofNullable(result.header.error.message));
		}
		sshKeyOperationService.updateStatus(new CorrelationId(result.header.messageCorrelationId), DONE,
				Optional.empty());

	}

	@EventListener
	void receiveAgentSSHKeyRemovalResult(Payload<AgentSSHKeyRemovalResult> result) {
		if (result.header.status.equals(Status.FAILED)) {
			sshKeyOperationService.updateStatus(new CorrelationId(result.header.messageCorrelationId),
					FAILED, Optional.ofNullable(result.header.error.message));
		}
		sshKeyOperationService.updateStatus(new CorrelationId(result.header.messageCorrelationId), DONE,
				Optional.empty());
		sshKeyOperationService.onSSHKeyRemovalFromSite(new CorrelationId(result.header.messageCorrelationId));
	}

	@EventListener
	void receiveAgentSSHKeyUpdatingResult(Payload<AgentSSHKeyUpdatingResult> result) {
		if (result.header.status.equals(Status.FAILED)) {
			sshKeyOperationService.updateStatus(new CorrelationId(result.header.messageCorrelationId),
					FAILED, Optional.ofNullable(result.header.error.message));
		}
		sshKeyOperationService.updateStatus(new CorrelationId(result.header.messageCorrelationId), DONE,
				Optional.empty());
	}

	@Override
	public void addSSHKey(CorrelationId correlationId, SSHKeyAddition installation) {

		AgentSSHKeyAdditionRequest request = SSHKeyOperationMapper.map(installation);
		try {
			rabbitTemplate.convertAndSend(getFurmsPublishQueueName(installation.siteExternalId),
					new Payload<>(new Header(VERSION, correlationId.id), request));
		} catch (AmqpConnectException e) {
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}

	@Override
	public void updateSSHKey(CorrelationId correlationId, SSHKeyUpdating updating) {

		AgentSSHKeyUpdatingRequest request = SSHKeyOperationMapper.map(updating);
		try {
			rabbitTemplate.convertAndSend(getFurmsPublishQueueName(updating.siteExternalId),
					new Payload<>(new Header(VERSION, correlationId.id), request));
		} catch (AmqpConnectException e) {
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}

	@Override
	public void removeSSHKey(CorrelationId correlationId, SSHKeyRemoval deinstallation) {
		AgentSSHKeyRemovalRequest request = SSHKeyOperationMapper.map(deinstallation);
		try {
			rabbitTemplate.convertAndSend(getFurmsPublishQueueName(deinstallation.siteExternalId),
					new Payload<>(new Header(VERSION, correlationId.id), request));
		} catch (AmqpConnectException e) {
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}

}
