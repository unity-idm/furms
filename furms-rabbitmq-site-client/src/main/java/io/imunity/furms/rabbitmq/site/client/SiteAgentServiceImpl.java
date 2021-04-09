/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.site_agent.AckStatus;
import io.imunity.furms.domain.site_agent.PendingJob;
import io.imunity.furms.domain.site_agent.SiteAgentException;
import io.imunity.furms.domain.site_agent.SiteAgentStatus;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.site.api.SiteAgentService;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static io.imunity.furms.domain.site_agent.AvailabilityStatus.AVAILABLE;
import static io.imunity.furms.domain.site_agent.AvailabilityStatus.UNAVAILABLE;

@Component
class SiteAgentServiceImpl implements SiteAgentService {
	
	private static final String REPLY_QUEUE = "reply-queue";
	
	private final RabbitTemplate rabbitTemplate;
	private final RabbitAdmin rabbitAdmin;
	private final Map<String, PendingJob<SiteAgentStatus>> map = new HashMap<>();

	SiteAgentServiceImpl(RabbitTemplate rabbitTemplate, RabbitAdmin rabbitAdmin) {
		this.rabbitTemplate = rabbitTemplate;
		this.rabbitAdmin = rabbitAdmin;
	}

	@Override
	public void initializeSiteConnection(SiteExternalId externalId) {
		Queue queue = new Queue(externalId.id);
		try {
			rabbitAdmin.declareQueue(queue);
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

	@RabbitListener(queues = REPLY_QUEUE)
	public void receive(Message message) {
		String correlationId = message.getMessageProperties().getCorrelationId();
		String agentStatus = message.getMessageProperties().getHeader("status").toString();
		PendingJob<SiteAgentStatus> pendingJob = map.get(correlationId);
		if(agentStatus.equals("IN_PROGRESS") && pendingJob != null){
			pendingJob.ackFuture.complete(AckStatus.ACK);
			return;
		}
		if(agentStatus.equals("OK") && pendingJob != null){
			pendingJob.jobFuture.complete(new SiteAgentStatus(AVAILABLE));
		}
		if(agentStatus.equals("FAILED") && pendingJob != null){
			pendingJob.jobFuture.complete(new SiteAgentStatus(UNAVAILABLE));
		}
		map.remove(correlationId);

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

	public void installProject(ProjectInstallationRequest request, SiteExternalId externalId){
		String correlationId = UUID.randomUUID().toString();
		try {
			rabbitTemplate.convertAndSend(externalId.id, request, new TypeHeaderAppender(request, correlationId));
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
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
