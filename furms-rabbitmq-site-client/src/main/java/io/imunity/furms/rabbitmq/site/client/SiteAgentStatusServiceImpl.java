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
import io.imunity.furms.rabbitmq.site.models.AgentPingAck;
import io.imunity.furms.rabbitmq.site.models.AgentPingRequest;
import io.imunity.furms.rabbitmq.site.models.AgentPingResult;
import io.imunity.furms.rabbitmq.site.models.converter.TypeHeaderAppender;
import io.imunity.furms.site.api.site_agent.SiteAgentStatusService;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static io.imunity.furms.domain.site_agent.AvailabilityStatus.AVAILABLE;
import static io.imunity.furms.domain.site_agent.AvailabilityStatus.UNAVAILABLE;
import static io.imunity.furms.rabbitmq.site.client.SiteAgentStatusServiceImpl.REPLY_QUEUE;

@Component
@RabbitListener(queues = REPLY_QUEUE)
class SiteAgentStatusServiceImpl implements SiteAgentStatusService {
	static final String REPLY_QUEUE = "reply-queue";

	private final RabbitTemplate rabbitTemplate;
	private final Map<String, PendingJob<SiteAgentStatus>> map = new HashMap<>();

	SiteAgentStatusServiceImpl(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
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

	@Override
	public PendingJob<SiteAgentStatus> getStatus(SiteExternalId externalId) {
		CompletableFuture<SiteAgentStatus> connectionFuture = new CompletableFuture<>();
		CompletableFuture<AckStatus> ackFuture = new CompletableFuture<>();

		String correlationId = UUID.randomUUID().toString();
		AgentPingRequest agentPingRequest = new AgentPingRequest(correlationId, null);
		try {
			rabbitTemplate.convertAndSend(externalId.id, agentPingRequest, new TypeHeaderAppender(agentPingRequest, correlationId));
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
		failJobIfNoResponse(connectionFuture);

		PendingJob<SiteAgentStatus> pendingJob = new PendingJob<>(connectionFuture, ackFuture, correlationId);
		map.put(correlationId, pendingJob);
		return pendingJob;
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
