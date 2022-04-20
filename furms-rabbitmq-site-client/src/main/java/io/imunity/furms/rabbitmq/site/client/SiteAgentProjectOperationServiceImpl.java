/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.project_installation.Error;
import io.imunity.furms.domain.project_installation.*;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent.SiteAgentException;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.rabbitmq.site.models.*;
import io.imunity.furms.site.api.status_updater.ProjectInstallationStatusUpdater;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectOperationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Optional;

import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getFurmsPublishQueueName;
import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;
import static java.util.Optional.ofNullable;

@Service
class SiteAgentProjectOperationServiceImpl implements SiteAgentProjectOperationService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


	private final RabbitTemplate rabbitTemplate;
	private final ProjectInstallationStatusUpdater projectInstallationService;

	SiteAgentProjectOperationServiceImpl(RabbitTemplate rabbitTemplate, ProjectInstallationStatusUpdater projectInstallationService) {
		this.rabbitTemplate = rabbitTemplate;
		this.projectInstallationService = projectInstallationService;
	}

	@EventListener
	void receiveAgentProjectInstallationAck(Payload<AgentProjectInstallationRequestAck> ack) {
		ProjectInstallationStatus status = ack.header.status.equals(Status.FAILED) ? ProjectInstallationStatus.FAILED : ProjectInstallationStatus.ACKNOWLEDGED;
		ProjectInstallationResult projectInstallationResult = new ProjectInstallationResult(
			Map.of(),
			status,
			new Error(ofNullable(ack.header.error).map(e -> e.code).orElse(null), ofNullable(ack.header.error).map(e -> e.message).orElse(null)));
		projectInstallationService.update(new CorrelationId(ack.header.messageCorrelationId), projectInstallationResult);
	}

	@EventListener
	void receiveAgentProjectInstallationResult(Payload<AgentProjectInstallationResult> result) {
		ProjectInstallationStatus status = result.header.status.equals(Status.FAILED) ? ProjectInstallationStatus.FAILED : ProjectInstallationStatus.INSTALLED;
		ProjectInstallationResult projectInstallationResult = new ProjectInstallationResult(
			Optional.ofNullable(result.body.attributes).orElseGet(Map::of),
			status,
			new Error(ofNullable(result.header.error).map(e -> e.code).orElse(null), ofNullable(result.header.error).map(e -> e.message).orElse(null)));
		projectInstallationService.update(new CorrelationId(result.header.messageCorrelationId), projectInstallationResult);
	}

	@EventListener
	void receiveAgentProjectUpdateAck(Payload<AgentProjectUpdateRequestAck> ack) {
		ProjectUpdateStatus status = ack.header.status.equals(Status.FAILED) ? ProjectUpdateStatus.FAILED : ProjectUpdateStatus.ACKNOWLEDGED;
		ProjectUpdateResult projectInstallationResult = new ProjectUpdateResult(
			status,
			new Error(ofNullable(ack.header.error).map(e -> e.code).orElse(null), ofNullable(ack.header.error).map(e -> e.message).orElse(null)));
		projectInstallationService.update(new CorrelationId(ack.header.messageCorrelationId), projectInstallationResult);
	}

	@EventListener
	void receiveAgentProjectUpdateResult(Payload<AgentProjectUpdateResult> result) {
		ProjectUpdateStatus status = result.header.status.equals(Status.FAILED) ? ProjectUpdateStatus.FAILED : ProjectUpdateStatus.UPDATED;
		ProjectUpdateResult projectInstallationResult = new ProjectUpdateResult(
			status,
			new Error(ofNullable(result.header.error).map(e -> e.code).orElse(null), ofNullable(result.header.error).map(e -> e.message).orElse(null)));
		projectInstallationService.update(new CorrelationId(result.header.messageCorrelationId), projectInstallationResult);
	}

	@EventListener
	void receiveAgentProjectRemovalRequestAck(Payload<AgentProjectRemovalRequestAck> ack) {
		ProjectRemovalStatus status = ack.header.status.equals(Status.FAILED) ? ProjectRemovalStatus.FAILED : ProjectRemovalStatus.ACKNOWLEDGED;
		LOG.info("Project Removal Job status with correlation id {} is {}", ack.header.messageCorrelationId, status);
	}

	@EventListener
	void receiveAgentProjectRemovalResult(Payload<AgentProjectRemovalResult> result) {
		ProjectRemovalStatus status = result.header.status.equals(Status.FAILED) ? ProjectRemovalStatus.FAILED : ProjectRemovalStatus.REMOVED;
		LOG.info("Project Removal Job status with correlation id {} is {}", result.header.messageCorrelationId, status);
	}

	@Override
	public void installProject(CorrelationId correlationId, ProjectInstallation installation) {
		AgentProjectInstallationRequest request = ProjectInstallationMapper.map(installation);
		try {
			rabbitTemplate.convertAndSend(getFurmsPublishQueueName(installation.siteId.externalId),
				new Payload<>(new Header(VERSION,
				correlationId.id), request));
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}

	@Override
	public void updateProject(CorrelationId correlationId, SiteExternalId siteExternalId, Project project, FURMSUser user) {
		AgentProjectUpdateRequest request = ProjectInstallationMapper.map(project, user);
		try {
			rabbitTemplate.convertAndSend(getFurmsPublishQueueName(siteExternalId), new Payload<>(new Header(VERSION, correlationId.id), request));
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}

	@Override
	public void removeProject(CorrelationId correlationId, SiteExternalId siteId, String projectId) {
		AgentProjectRemovalRequest request = new AgentProjectRemovalRequest(projectId);
		try {
			rabbitTemplate.convertAndSend(getFurmsPublishQueueName(siteId), new Payload<>(new Header(VERSION, correlationId.id), request));
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}
}
