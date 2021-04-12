/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.project_installation.ProjectInstallationJob;
import io.imunity.furms.domain.site_agent.*;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.rabbitmq.site.models.*;
import io.imunity.furms.rabbitmq.site.models.converter.TypeHeaderAppender;
import io.imunity.furms.site.api.ProjectInstallationService;
import io.imunity.furms.site.api.SiteAgentService;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.ACK;
import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.DONE;
import static io.imunity.furms.domain.site_agent.AvailabilityStatus.AVAILABLE;
import static io.imunity.furms.domain.site_agent.AvailabilityStatus.UNAVAILABLE;
import static io.imunity.furms.rabbitmq.site.client.SiteAgentServiceImpl.REPLY_QUEUE;
import static io.imunity.furms.rabbitmq.site.models.consts.Headers.CORRELATION_ID;

@Component
@RabbitListener(queues = REPLY_QUEUE)
class SiteAgentServiceImpl implements SiteAgentService {

	static final String REPLY_QUEUE = "reply-queue";

	private final RabbitTemplate rabbitTemplate;
	private final RabbitAdmin rabbitAdmin;
	private final ProjectInstallationService projectInstallationService;
	private final Map<String, PendingJob<SiteAgentStatus>> map = new HashMap<>();

	SiteAgentServiceImpl(RabbitTemplate rabbitTemplate, RabbitAdmin rabbitAdmin, ProjectInstallationService projectInstallationService) {
		this.rabbitTemplate = rabbitTemplate;
		this.rabbitAdmin = rabbitAdmin;
		this.projectInstallationService = projectInstallationService;
	}

	@Override
	public void initializeSiteConnection(SiteExternalId externalId) {
		try {
			rabbitAdmin.declareQueue(new org.springframework.amqp.core.Queue(externalId.id));
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}

	@Override
	public void removeSiteConnection(SiteExternalId externalId) {
		try {
			rabbitAdmin.deleteQueue(externalId.id);
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}

	@RabbitHandler
	public void receive(AgentPingAck ack) {
		PendingJob<SiteAgentStatus> pendingJob = map.get(ack.correlationId);
		if(pendingJob != null)
			pendingJob.ackFuture.complete(AckStatus.ACK);
	}

	@RabbitHandler
	public void receive(AgentPingResult result) {
		PendingJob<SiteAgentStatus> pendingJob = map.get(result.correlationId);
		if(result.status.equals("OK") && pendingJob != null){
			pendingJob.jobFuture.complete(new SiteAgentStatus(AVAILABLE));
		}
		if(result.status.equals("FAILED") && pendingJob != null){
			pendingJob.jobFuture.complete(new SiteAgentStatus(UNAVAILABLE));
		}
		map.remove(result.correlationId);
	}

	@RabbitHandler
	public void receive(AgentProjectInstallationAck ack) {
		ProjectInstallationJob projectInstallationJob = new ProjectInstallationJob(new CorrelationId(ack.correlationId), ACK);
		projectInstallationService.update(projectInstallationJob);
	}

	@RabbitHandler
	public void receive(AgentProjectInstallationResult result, @Headers Map<String,Object> headers) {
		String correlationId = headers.get(CORRELATION_ID).toString();
		ProjectInstallationJob projectInstallationJob = new ProjectInstallationJob(new CorrelationId(correlationId), DONE);
		projectInstallationService.update(projectInstallationJob);
	}

	@Override
	public PendingJob<SiteAgentStatus> getStatus(SiteExternalId externalId) {
		CompletableFuture<SiteAgentStatus> connectionFuture = new CompletableFuture<>();
		CompletableFuture<AckStatus> ackFuture = new CompletableFuture<>();

		String correlationId = UUID.randomUUID().toString();
		MessageProperties messageProperties = new MessageProperties();
		messageProperties.setCorrelationId(correlationId);
		messageProperties.setReplyTo(REPLY_QUEUE);
		messageProperties.setHeader("version", 1);
		messageProperties.setHeader("furmsMessageType", "AgentPingRequest");

		Message message = new Message(new byte[]{}, messageProperties);
		try {
			rabbitTemplate.send(externalId.id, message);
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
		failJobIfNoResponse(connectionFuture);

		PendingJob<SiteAgentStatus> pendingJob = new PendingJob<>(connectionFuture, ackFuture, correlationId);
		map.put(correlationId, pendingJob);
		return pendingJob;
	}

	@Override
	public CorrelationId installProject(ProjectInstallation installation) {
		String correlationId = UUID.randomUUID().toString();
		AgentProjectInstallationRequest request = ProjectInstallationMapper.map(installation);
		try {
			rabbitTemplate.convertAndSend(installation.siteExternalId, request, new TypeHeaderAppender(request, correlationId));
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
		return new CorrelationId(correlationId);
	}

	private void failJobIfNoResponse(CompletableFuture<SiteAgentStatus> connectionFuture) {
		new Thread(() -> {
			try {
				TimeUnit.SECONDS.sleep(10);
				if(!connectionFuture.isDone())
					connectionFuture.complete(new SiteAgentStatus(UNAVAILABLE));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
	}
}
