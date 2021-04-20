/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.site_agent.PendingJob;
import io.imunity.furms.domain.site_agent.SiteAgentException;
import io.imunity.furms.domain.site_agent.SiteAgentStatus;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.rabbitmq.site.models.AgentPingAck;
import io.imunity.furms.rabbitmq.site.models.AgentPingRequest;
import io.imunity.furms.rabbitmq.site.models.Header;
import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.site.api.site_agent.SiteAgentStatusService;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static io.imunity.furms.domain.site_agent.AvailabilityStatus.AVAILABLE;
import static io.imunity.furms.domain.site_agent.AvailabilityStatus.UNAVAILABLE;
import static io.imunity.furms.rabbitmq.site.client.SiteAgentListenerRouter.PUB_FURMS;
import static io.imunity.furms.rabbitmq.site.models.Status.FAILED;
import static io.imunity.furms.rabbitmq.site.models.Status.OK;

@Service
class SiteAgentStatusServiceImpl implements SiteAgentStatusService {

	private final RabbitTemplate rabbitTemplate;
	private final Map<String, PendingJob<SiteAgentStatus>> map = new HashMap<>();

	SiteAgentStatusServiceImpl(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	void receiveAgentPingAck(Payload<AgentPingAck> result) {
		PendingJob<SiteAgentStatus> pendingJob = map.get(result.header.messageCorrelationId);
		if(result.header.status.equals(OK) && pendingJob != null){
			pendingJob.jobFuture.complete(new SiteAgentStatus(AVAILABLE));
		}
		if(result.header.status.equals(FAILED) && pendingJob != null){
			pendingJob.jobFuture.complete(new SiteAgentStatus(UNAVAILABLE));
		}
		map.remove(result.header.messageCorrelationId);
	}

	@Override
	public PendingJob<SiteAgentStatus> getStatus(SiteExternalId externalId) {
		CompletableFuture<SiteAgentStatus> connectionFuture = new CompletableFuture<>();

		String correlationId = UUID.randomUUID().toString();
		AgentPingRequest agentPingRequest = new AgentPingRequest();
		try {
			Header header = new Header("1", correlationId, null, null);
			rabbitTemplate.convertAndSend(externalId.id + PUB_FURMS, new Payload<>(header, agentPingRequest));
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
		failJobIfNoResponse(connectionFuture);

		PendingJob<SiteAgentStatus> pendingJob = new PendingJob<>(connectionFuture, correlationId);
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
