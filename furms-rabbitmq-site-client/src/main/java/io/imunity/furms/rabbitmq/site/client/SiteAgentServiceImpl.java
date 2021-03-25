/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import static io.imunity.furms.domain.site_agent.AvailabilityStatus.AVAILABLE;
import static io.imunity.furms.domain.site_agent.AvailabilityStatus.UNAVAILABLE;

import java.util.concurrent.CompletableFuture;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Component;

import io.imunity.furms.domain.site_agent.SiteAgentStatus;
import io.imunity.furms.site.api.SiteAgentService;

@Component
class SiteAgentServiceImpl implements SiteAgentService {
	private final AsyncRabbitTemplate rabbitTemplate;
	private final RabbitAdmin rabbitAdmin;

	SiteAgentServiceImpl(AsyncRabbitTemplate rabbitTemplate, RabbitAdmin rabbitAdmin) {
		this.rabbitTemplate = rabbitTemplate;
		this.rabbitAdmin = rabbitAdmin;
	}

	@Override
	public void initializeSiteConnection(String siteId) {
		Queue queue = new Queue(siteId);
		rabbitAdmin.declareQueue(queue);
	}

	@Override
	public void removeSiteConnection(String siteId) {
		rabbitAdmin.deleteQueue(siteId);
	}

	@Override
	public CompletableFuture<SiteAgentStatus> getStatus(String siteId) {
		CompletableFuture<SiteAgentStatus> future = new CompletableFuture<>();
		AgentPingRequest request = new AgentPingRequest();
		AsyncRabbitTemplate.RabbitConverterFuture<Object> rabbitFuture = rabbitTemplate
				.convertSendAndReceive(siteId, request, new TypeHederAppender(request));
		rabbitFuture.addCallback(
			message -> future.complete(new SiteAgentStatus(AVAILABLE)),
			message -> future.complete(new SiteAgentStatus(UNAVAILABLE)));
		return future;
	}
}
