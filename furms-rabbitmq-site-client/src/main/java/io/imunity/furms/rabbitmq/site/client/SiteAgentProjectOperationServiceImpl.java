/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.project_installation.ProjectInstallationStatus;
import io.imunity.furms.domain.project_installation.ProjectRemovalStatus;
import io.imunity.furms.domain.project_installation.ProjectUpdateStatus;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent.SiteAgentException;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.rabbitmq.site.models.*;
import io.imunity.furms.site.api.message_resolver.ProjectInstallationMessageResolver;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectOperationService;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getFurmsPublishQueueName;
import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;

@Service
class SiteAgentProjectOperationServiceImpl implements SiteAgentProjectOperationService {
	private final RabbitTemplate rabbitTemplate;
	private final ProjectInstallationMessageResolver projectInstallationService;

	SiteAgentProjectOperationServiceImpl(RabbitTemplate rabbitTemplate, ProjectInstallationMessageResolver projectInstallationService) {
		this.rabbitTemplate = rabbitTemplate;
		this.projectInstallationService = projectInstallationService;
	}

	@EventListener
	void receiveAgentProjectInstallationAck(Payload<AgentProjectInstallationAck> ack) {
		if(ack.header.status.equals(Status.FAILED)){
			projectInstallationService.update(new CorrelationId(ack.header.messageCorrelationId), ProjectInstallationStatus.FAILED);
			return;
		}
		projectInstallationService.update(new CorrelationId(ack.header.messageCorrelationId), ProjectInstallationStatus.ACKNOWLEDGED);
	}

	@EventListener
	void receiveAgentProjectInstallationResult(Payload<AgentProjectInstallationResult> result) {
		String correlationId = result.header.messageCorrelationId;
		if(result.header.status.equals(Status.FAILED)){
			projectInstallationService.update(new CorrelationId(correlationId), ProjectInstallationStatus.FAILED);
			return;
		}
		projectInstallationService.update(new CorrelationId(correlationId), ProjectInstallationStatus.INSTALLED);
	}

	@EventListener
	void receiveAgentProjectUpdateAck(Payload<AgentProjectUpdateRequestAck> ack) {
		if(ack.header.status.equals(Status.FAILED)){
			projectInstallationService.update(new CorrelationId(ack.header.messageCorrelationId), ProjectUpdateStatus.FAILED);
			return;
		}
		projectInstallationService.update(new CorrelationId(ack.header.messageCorrelationId), ProjectUpdateStatus.ACKNOWLEDGED);
	}

	@EventListener
	void receiveAgentProjectUpdateResult(Payload<AgentProjectUpdateResult> result) {
		String correlationId = result.header.messageCorrelationId;
		if(result.header.status.equals(Status.FAILED)){
			projectInstallationService.update(new CorrelationId(correlationId), ProjectUpdateStatus.FAILED);
			return;
		}
		projectInstallationService.update(new CorrelationId(correlationId), ProjectUpdateStatus.UPDATED);
	}

	@EventListener
	void receiveAgentProjectRemovalRequestAck(Payload<AgentProjectRemovalRequestAck> ack) {
		if(ack.header.status.equals(Status.FAILED)){
			projectInstallationService.update(new CorrelationId(ack.header.messageCorrelationId), ProjectRemovalStatus.FAILED);
			return;
		}
		projectInstallationService.update(new CorrelationId(ack.header.messageCorrelationId), ProjectRemovalStatus.ACKNOWLEDGED);
	}

	@EventListener
	void receiveAgentProjectRemovalResult(Payload<AgentProjectRemovalResult> result) {
		String correlationId = result.header.messageCorrelationId;
		if(result.header.status.equals(Status.FAILED)){
			projectInstallationService.update(new CorrelationId(correlationId), ProjectRemovalStatus.FAILED);
			return;
		}
		projectInstallationService.update(new CorrelationId(correlationId), ProjectRemovalStatus.REMOVED);
	}

	@Override
	@Transactional(propagation = Propagation.NESTED)
	public void installProject(CorrelationId correlationId, ProjectInstallation installation) {
		AgentProjectInstallationRequest request = ProjectInstallationMapper.map(installation);
		try {
			rabbitTemplate.convertAndSend(getFurmsPublishQueueName(installation.siteExternalId), new Payload<>(new Header(VERSION, correlationId.id), request));
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.NESTED)
	public void updateProject(CorrelationId correlationId, SiteExternalId siteExternalId, Project project, FURMSUser user) {
		AgentProjectUpdateRequest request = ProjectInstallationMapper.map(project, user);
		try {
			rabbitTemplate.convertAndSend(getFurmsPublishQueueName(siteExternalId), new Payload<>(new Header(VERSION, correlationId.id), request));
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.NESTED)
	public void removeProject(CorrelationId correlationId, SiteExternalId siteId, String projectId) {
		AgentProjectRemovalRequest request = new AgentProjectRemovalRequest(projectId);
		try {
			rabbitTemplate.convertAndSend(getFurmsPublishQueueName(siteId), new Payload<>(new Header(VERSION, correlationId.id), request));
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}
}
