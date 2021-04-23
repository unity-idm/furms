/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import static io.imunity.furms.rabbitmq.site.models.consts.Headers.CORRELATION_ID;
import static io.imunity.furms.rabbitmq.site.models.consts.Headers.STATUS;
import static io.imunity.furms.rabbitmq.site.models.consts.Headers.ERROR;

import java.util.Map;
import java.util.Optional;

import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent.MessageStatus;
import io.imunity.furms.domain.site_agent.SiteAgentException;
import io.imunity.furms.domain.ssh_key_operation.SSHKeyRemoval;
import io.imunity.furms.domain.ssh_key_operation.SSHKeyAddition;
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
import io.imunity.furms.rabbitmq.site.models.converter.TypeHeaderAppender;
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

	void receive(AgentSSHKeyAdditionAck ack) {
		sshKeyOperationService.addSSHKeyAck(new CorrelationId(ack.correlationId));
	}

	void receive(AgentSSHKeyRemovalAck ack) {
		sshKeyOperationService.removeSSHKeyAck(new CorrelationId(ack.correlationId));
	}

	void receive(AgentSSHKeyUpdatingAck ack) {
		sshKeyOperationService.updateSSHKeyAck(new CorrelationId(ack.correlationId));
	}

	void receive(AgentSSHKeyAdditionResult result, Map<String, Object> headers) {
		String correlationId = headers.get(CORRELATION_ID).toString();
		MessageStatus status = MessageStatus.valueOf(headers.get(STATUS).toString());
		String error = null;
		if (status.equals(MessageStatus.FAILED)) {
			error = headers.get(ERROR).toString();
		}

		sshKeyOperationService.onSSHKeyAddToSite(new CorrelationId(correlationId), status,
				Optional.ofNullable(error));

	}

	void receive(AgentSSHKeyRemovalResult result, Map<String, Object> headers) {
		String correlationId = headers.get(CORRELATION_ID).toString();
		MessageStatus status = MessageStatus.valueOf(headers.get(STATUS).toString());
		String error = null;
		if (status.equals(MessageStatus.FAILED)) {
			error = headers.get(ERROR).toString();
		}

		sshKeyOperationService.onSSHKeyRemovalFromSite(new CorrelationId(correlationId), status,
				Optional.ofNullable(error));
	}

	void receive(AgentSSHKeyUpdatingResult result, Map<String, Object> headers) {
		String correlationId = headers.get(CORRELATION_ID).toString();
		MessageStatus status = MessageStatus.valueOf(headers.get(STATUS).toString());
		String error = null;
		if (status.equals(MessageStatus.FAILED)) {
			error = headers.get(ERROR).toString();
		}
		sshKeyOperationService.onSSHKeyUpdateOnSite(new CorrelationId(correlationId), status,
				Optional.ofNullable(error));
	}

	@Override
	public void addSSHKey(CorrelationId correlationId, SSHKeyAddition installation) {

		AgentSSHKeyAdditionRequest request = SSHKeyOperationMapper.map(installation);
		try {
			rabbitTemplate.convertAndSend(installation.siteExternalId.id, request,
					new TypeHeaderAppender(request, correlationId.id));
		} catch (AmqpConnectException e) {
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}

	@Override
	public void updateSSHKey(CorrelationId correlationId, SSHKeyUpdating updating) {

		AgentSSHKeyUpdatingRequest request = SSHKeyOperationMapper.map(updating);
		try {
			rabbitTemplate.convertAndSend(updating.siteExternalId.id, request,
					new TypeHeaderAppender(request, correlationId.id));
		} catch (AmqpConnectException e) {
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}

	@Override
	public void removeSSHKey(CorrelationId correlationId, SSHKeyRemoval deinstallation) {
		AgentSSHKeyRemovalRequest request = SSHKeyOperationMapper.map(deinstallation);
		try {
			rabbitTemplate.convertAndSend(deinstallation.siteExternalId.id, request,
					new TypeHeaderAppender(request, correlationId.id));
		} catch (AmqpConnectException e) {
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}

}
