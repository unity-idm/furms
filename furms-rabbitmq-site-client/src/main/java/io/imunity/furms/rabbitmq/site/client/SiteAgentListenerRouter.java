/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent.InvalidMessageContentException;
import io.imunity.furms.rabbitmq.site.client.config.PlaneRabbitTemplate;
import io.imunity.furms.rabbitmq.site.models.Ack;
import io.imunity.furms.rabbitmq.site.models.AgentMessageErrorInfo;
import io.imunity.furms.rabbitmq.site.models.AgentPingAck;
import io.imunity.furms.rabbitmq.site.models.AgentPolicyUpdateAck;
import io.imunity.furms.rabbitmq.site.models.AgentProjectAllocationInstallationAck;
import io.imunity.furms.rabbitmq.site.models.ErrorPayload;
import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.rabbitmq.site.models.Result;
import io.imunity.furms.rabbitmq.site.models.UserPolicyAcceptanceUpdateAck;
import io.imunity.furms.site.api.AgentPendingMessageSiteService;
import io.imunity.furms.utils.MDCKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getOppositeDirectionQueue;
import static io.imunity.furms.rabbitmq.site.client.SiteAgentListenerRouter.FURMS_LISTENER;
import static io.imunity.furms.rabbitmq.site.models.Status.OK;
import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;
import static org.springframework.amqp.support.AmqpHeaders.CONSUMER_QUEUE;

@Component
@RabbitListener(id = FURMS_LISTENER)
class SiteAgentListenerRouter {

	static final String FURMS_LISTENER = "FURMS_LISTENER";
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ApplicationEventPublisher publisher;
	private final MessageAuthorizer messageAuthorizer;
	private final AgentPendingMessageSiteService agentPendingMessageSiteService;
	private final PlaneRabbitTemplate rabbitTemplate;


	SiteAgentListenerRouter(ApplicationEventPublisher publisher, MessageAuthorizer validator,
			AgentPendingMessageSiteService agentPendingMessageSiteService, PlaneRabbitTemplate rabbitTemplate) {
		this.publisher = publisher;
		this.messageAuthorizer = validator;
		this.agentPendingMessageSiteService = agentPendingMessageSiteService;
		this.rabbitTemplate = rabbitTemplate;
	}

	@RabbitHandler
	public void receive(Payload<?> payload, @Header(CONSUMER_QUEUE) String queueName) {
		MDC.put(MDCKey.QUEUE_NAME.key, queueName);
		try {
			validContent(payload);
			messageAuthorizer.validate(payload, queueName);
			publisher.publishEvent(payload);
			updateOrDeletePendingRequests(payload);
			LOG.info("Received payload {}", payload);
		} catch (Exception e) {
			LOG.error("This error occurred while processing payload: {} from queue {}", payload, queueName, e);
			sendErrorMessageToSite(
				queueName,
				payload.header.messageCorrelationId,
				convertExceptionToErrorCode(e.getClass()),
				e.getMessage()
			);
		} finally {
			MDC.remove(MDCKey.QUEUE_NAME.key);
		}
	}

	@RabbitHandler(isDefault = true)
	public void receive(ErrorPayload errorPayload, @Header(CONSUMER_QUEUE) String queueName) {
		MDC.put(MDCKey.QUEUE_NAME.key, queueName);
		try {
			LOG.info("Received object, which cannot be processed {}", errorPayload);
			sendErrorMessageToSite(queueName, errorPayload.correlationId, "InvalidMessageContent", "The message can not be parsed: " + errorPayload.unparsableMessage);
		} finally {
			MDC.remove(MDCKey.QUEUE_NAME.key);
		}
	}

	private void validContent(Payload<?> payload) {
		if(payload.header.version == null)
			throw new InvalidMessageContentException("Version property is required!");
		if(payload.header.status == null)
			throw new InvalidMessageContentException("Status property is required!");
	}

	private void sendErrorMessageToSite(String queueName, String correlationId, String errorType, String description) {
		rabbitTemplate.convertAndSend(
			getOppositeDirectionQueue(queueName),
			new Payload<>(
				new io.imunity.furms.rabbitmq.site.models.Header(VERSION, null),
				new AgentMessageErrorInfo(correlationId, errorType, description)
			)
		);
	}

	private String convertExceptionToErrorCode(Class<? extends Throwable> clazz) {
		return clazz.getSimpleName().replace("Exception", "");
	}

	/**
	 * This method update or delete pending message based on arriving message type.
	 * If message is Ack type it should be update, if message is Result type it should be delete.
	 * There are four exceptions AgentProjectAllocationInstallationAck, AgentPingAck, UserPolicyAcceptanceUpdateAck
	 * AgentPolicyUpdateAck. When those messages arrived, pending message should be removed,
	 * because those messages don't have result type.
	 */
	private void updateOrDeletePendingRequests(Payload<?> payload) {
		if(payload.body instanceof AgentPingAck || payload.body instanceof AgentProjectAllocationInstallationAck
			|| payload.body instanceof UserPolicyAcceptanceUpdateAck || payload.body instanceof AgentPolicyUpdateAck
		)
			agentPendingMessageSiteService.delete(new CorrelationId(payload.header.messageCorrelationId));
		else if(payload.header.status.equals(OK) && payload.body instanceof Ack)
			agentPendingMessageSiteService.setAsAcknowledged(new CorrelationId(payload.header.messageCorrelationId));
		else if(payload.header.status.equals(OK) && payload.body instanceof Result)
			agentPendingMessageSiteService.delete(new CorrelationId(payload.header.messageCorrelationId));
	}
}
