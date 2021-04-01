/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import static io.imunity.furms.domain.site_agent.AvailabilityStatus.AVAILABLE;
import static io.imunity.furms.domain.site_agent.AvailabilityStatus.UNAVAILABLE;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import io.imunity.furms.domain.site_agent.AckStatus;
import io.imunity.furms.domain.site_agent.PendingJob;
import io.imunity.furms.domain.site_agent.SiteAgentException;
import io.imunity.furms.domain.site_agent.SiteAgentStatus;
import io.imunity.furms.site.api.SiteAgentService;

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
	public void initializeSiteConnection(String siteId) {
		Queue queue = new Queue(siteId);
		try {
			rabbitAdmin.declareQueue(queue);
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}

	@Override
	public void removeSiteConnection(String siteId) {
		try {
			rabbitAdmin.deleteQueue(siteId);
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}

	@RabbitListener(queues = REPLY_QUEUE)
	public void receive(Message message) {
		String correlationId = message.getMessageProperties().getCorrelationId();
		String agentStatus = message.getMessageProperties().getHeader("status").toString();
		if(agentStatus.equals("IN_PROGRESS")){
			map.get(correlationId).ackFuture.complete(AckStatus.ACK);
		}
		if(agentStatus.equals("OK")){
			map.get(correlationId).jobFuture.complete(new SiteAgentStatus(AVAILABLE));
			map.remove(correlationId);
		}
		if(agentStatus.equals("FAILED")){
			map.get(correlationId).jobFuture.complete(new SiteAgentStatus(UNAVAILABLE));
			map.remove(correlationId);
		}
	}

	@Override
	public PendingJob<SiteAgentStatus> getStatus(String siteId) {
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
			rabbitTemplate.send(siteId, message);
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}

		PendingJob<SiteAgentStatus> pendingJob = new PendingJob<>(connectionFuture, ackFuture, correlationId);
		map.put(correlationId, pendingJob);
		return pendingJob;
	}
}
