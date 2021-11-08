/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent.PendingJob;
import io.imunity.furms.domain.site_agent.SiteAgentException;
import io.imunity.furms.domain.site_agent.SiteAgentStatus;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.rabbitmq.site.models.AgentPingAck;
import io.imunity.furms.rabbitmq.site.models.AgentPingRequest;
import io.imunity.furms.rabbitmq.site.models.Header;
import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.site.api.message_resolver.BaseSiteIdResolver;
import io.imunity.furms.site.api.site_agent.SiteAgentStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static io.imunity.furms.domain.site_agent.AvailabilityStatus.AVAILABLE;
import static io.imunity.furms.domain.site_agent.AvailabilityStatus.UNAVAILABLE;
import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getFurmsPublishQueueName;
import static io.imunity.furms.rabbitmq.site.models.Status.FAILED;
import static io.imunity.furms.rabbitmq.site.models.Status.OK;
import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;

@Service
public class SiteAgentStatusServiceImpl implements SiteAgentStatusService, BaseSiteIdResolver {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final RabbitTemplate rabbitTemplate;
	private final Map<String, PendingJob<SiteAgentStatus>> map = new HashMap<>();

	SiteAgentStatusServiceImpl(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@EventListener
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
	public SiteExternalId getSiteId(CorrelationId correlationId) {
		PendingJob<SiteAgentStatus> pendingJob = map.get(correlationId.id);
		return Optional.ofNullable(pendingJob)
			.map(job -> job.siteExternalId)
			.orElse(null);
	}

	@Override
	public PendingJob<SiteAgentStatus> getStatus(SiteExternalId externalId) {
		CompletableFuture<SiteAgentStatus> connectionFuture = new CompletableFuture<>();

		CorrelationId correlationId = CorrelationId.randomID();
		AgentPingRequest agentPingRequest = new AgentPingRequest();

		PendingJob<SiteAgentStatus> pendingJob = new PendingJob<>(connectionFuture, correlationId, externalId);
		map.put(correlationId.id, pendingJob);
		try {
			Header header = new Header(VERSION, correlationId.id, null, null);
			rabbitTemplate.convertAndSend(getFurmsPublishQueueName(externalId), new Payload<>(header, agentPingRequest));
		}catch (AmqpConnectException e){
			map.remove(correlationId.id);
			throw new SiteAgentException("Queue is unavailable", e);
		}
		failJobIfNoResponse(connectionFuture);

		return pendingJob;
	}

	private void failJobIfNoResponse(CompletableFuture<SiteAgentStatus> connectionFuture) {
		new Thread(() -> {
			try {
				TimeUnit.SECONDS.sleep(10);
				if(!connectionFuture.isDone())
					connectionFuture.complete(new SiteAgentStatus(UNAVAILABLE));
			} catch (InterruptedException e) {
				LOG.warn("Failed to complete the task", e);
			}
		}).start();
	}
}
