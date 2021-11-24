/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent.SiteAgentException;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationError;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationResult;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus;
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
import io.imunity.furms.site.api.ssh_keys.SSHKeyAddition;
import io.imunity.furms.site.api.ssh_keys.SSHKeyRemoval;
import io.imunity.furms.site.api.ssh_keys.SSHKeyUpdating;
import io.imunity.furms.site.api.ssh_keys.SiteAgentSSHKeyOperationService;
import io.imunity.furms.site.api.status_updater.SSHKeyOperationStatusUpdater;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getFurmsPublishQueueName;
import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;
import static java.util.Optional.ofNullable;

@Service
class SiteAgentSSHKeyOperationServiceImpl implements SiteAgentSSHKeyOperationService {

	private final RabbitTemplate rabbitTemplate;
	private final SSHKeyOperationStatusUpdater sshKeyOperationService;

	SiteAgentSSHKeyOperationServiceImpl(RabbitTemplate rabbitTemplate,
			SSHKeyOperationStatusUpdater sshKeyInstallationService) {

		this.rabbitTemplate = rabbitTemplate;
		this.sshKeyOperationService = sshKeyInstallationService;
	}

	@EventListener
	void receiveAgentSSHKeyAdditionAck(Payload<AgentSSHKeyAdditionAck> ack) {

		SSHKeyOperationStatus status = ack.header.status.equals(Status.FAILED) ? SSHKeyOperationStatus.FAILED
				: SSHKeyOperationStatus.ACK;
		SSHKeyOperationResult result = new SSHKeyOperationResult(status,
				new SSHKeyOperationError(ofNullable(ack.header.error).map(e -> e.code).orElse(null),
						ofNullable(ack.header.error).map(e -> e.message).orElse(null)));
		sshKeyOperationService.updateStatus(new CorrelationId(ack.header.messageCorrelationId), result);
	}

	@EventListener
	void receiveAgentSSHKeyRemovalAck(Payload<AgentSSHKeyRemovalAck> ack) {
		SSHKeyOperationStatus status = ack.header.status.equals(Status.FAILED) ? SSHKeyOperationStatus.FAILED
				: SSHKeyOperationStatus.ACK;
		SSHKeyOperationResult result = new SSHKeyOperationResult(status,
				new SSHKeyOperationError(ofNullable(ack.header.error).map(e -> e.code).orElse(null),
						ofNullable(ack.header.error).map(e -> e.message).orElse(null)));
		sshKeyOperationService.updateStatus(new CorrelationId(ack.header.messageCorrelationId), result);
	}

	@EventListener
	void receiveAgentSSHKeyUpdatingAck(Payload<AgentSSHKeyUpdatingAck> ack) {
		SSHKeyOperationStatus status = ack.header.status.equals(Status.FAILED) ? SSHKeyOperationStatus.FAILED
				: SSHKeyOperationStatus.ACK;
		SSHKeyOperationResult result = new SSHKeyOperationResult(status,
				new SSHKeyOperationError(ofNullable(ack.header.error).map(e -> e.code).orElse(null),
						ofNullable(ack.header.error).map(e -> e.message).orElse(null)));
		sshKeyOperationService.updateStatus(new CorrelationId(ack.header.messageCorrelationId), result);
	}

	@EventListener
	void receiveAgentSSHKeyAdditionResult(Payload<AgentSSHKeyAdditionResult> result) {
		SSHKeyOperationStatus status = result.header.status.equals(Status.FAILED) ? SSHKeyOperationStatus.FAILED
				: SSHKeyOperationStatus.DONE;
		SSHKeyOperationResult operationResult = new SSHKeyOperationResult(status,
				new SSHKeyOperationError(ofNullable(result.header.error).map(e -> e.code).orElse(null),
						ofNullable(result.header.error).map(e -> e.message).orElse(null)));
		sshKeyOperationService.updateStatus(new CorrelationId(result.header.messageCorrelationId),
				operationResult);

	}

	@EventListener
	void receiveAgentSSHKeyRemovalResult(Payload<AgentSSHKeyRemovalResult> result) {
		SSHKeyOperationStatus status = result.header.status.equals(Status.FAILED) ? SSHKeyOperationStatus.FAILED
				: SSHKeyOperationStatus.DONE;
		SSHKeyOperationResult operationResult = new SSHKeyOperationResult(status,
				new SSHKeyOperationError(ofNullable(result.header.error).map(e -> e.code).orElse(null),
						ofNullable(result.header.error).map(e -> e.message).orElse(null)));
		sshKeyOperationService.updateStatus(new CorrelationId(result.header.messageCorrelationId),
				operationResult);
	}

	@EventListener
	void receiveAgentSSHKeyUpdatingResult(Payload<AgentSSHKeyUpdatingResult> result) {
		SSHKeyOperationStatus status = result.header.status.equals(Status.FAILED) ? SSHKeyOperationStatus.FAILED
				: SSHKeyOperationStatus.DONE;
		SSHKeyOperationResult operationResult = new SSHKeyOperationResult(status,
				new SSHKeyOperationError(ofNullable(result.header.error).map(e -> e.code).orElse(null),
						ofNullable(result.header.error).map(e -> e.message).orElse(null)));
		sshKeyOperationService.updateStatus(new CorrelationId(result.header.messageCorrelationId),
				operationResult);
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
