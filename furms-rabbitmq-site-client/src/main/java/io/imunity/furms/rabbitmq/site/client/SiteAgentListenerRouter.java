/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.project_allocation_installation.ErrorMessage;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent.InvalidMessageContentException;
import io.imunity.furms.rabbitmq.site.client.config.PlaneRabbitTemplate;
import io.imunity.furms.rabbitmq.site.models.Ack;
import io.imunity.furms.rabbitmq.site.models.AgentMessageErrorInfo;
import io.imunity.furms.rabbitmq.site.models.AgentPingAck;
import io.imunity.furms.rabbitmq.site.models.AgentPolicyUpdateAck;
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
import java.util.Optional;

import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getOppositeDirectionQueue;
import static io.imunity.furms.rabbitmq.site.client.SiteAgentListenerRouter.FURMS_LISTENER;
import static io.imunity.furms.rabbitmq.site.models.Status.FAILED;
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
			assertHeaderIsValid(payload);
			messageAuthorizer.validate(payload, queueName);
			if(payload.header.status.equals(FAILED))
				updateFailedPendingRequest(payload);
			publisher.publishEvent(payload);
			updateOrDeleteSuccessPendingRequests(payload);
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
			sendErrorMessageToSite(queueName, errorPayload.correlationId, "InvalidMessageContent", "The message can " +
				"not be parsed: " + errorPayload.unparsableMessage + errorPayload.reasons.map(msg -> ", Reasons: " + msg).orElse(""));
		} finally {
			MDC.remove(MDCKey.QUEUE_NAME.key);
		}
	}

	private void assertHeaderIsValid(Payload<?> payload) {
		if(!payload.header.version.equals(VERSION))
			throw new InvalidMessageContentException("Unsupported protocol version! Current version: " + VERSION);
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
	 * This method updates or deletes pending message based on arriving message type.
	 * If message is Ack type it should be updated, if message is Result type it should be deleted.
	 * There are three exceptions AgentPingAck, UserPolicyAcceptanceUpdateAck
	 * AgentPolicyUpdateAck. When those messages arrived, pending message should be removed,
	 * because those messages don't have result type.
	 */
	private void updateOrDeleteSuccessPendingRequests(Payload<?> payload) {
		if(payload.body instanceof AgentPingAck || payload.body instanceof UserPolicyAcceptanceUpdateAck
			|| payload.body instanceof AgentPolicyUpdateAck
		)
			agentPendingMessageSiteService.delete(new CorrelationId(payload.header.messageCorrelationId));
		else if(payload.header.status.equals(OK) && payload.body instanceof Ack)
			agentPendingMessageSiteService.setAsAcknowledged(new CorrelationId(payload.header.messageCorrelationId));
		else if(payload.header.status.equals(OK) && payload.body instanceof Result)
			agentPendingMessageSiteService.delete(new CorrelationId(payload.header.messageCorrelationId));
	}

	private void updateFailedPendingRequest(Payload<?> payload) {
		updateAcknowledgedStatus(payload);
		agentPendingMessageSiteService.updateErrorMessage(
			new CorrelationId(payload.header.messageCorrelationId),
			new ErrorMessage(
				Optional.ofNullable(payload.header.error).map(x -> x.code).orElse(null),
				Optional.ofNullable(payload.header.error).map(x -> x.message).orElse(null))
		);
	}

	private void updateAcknowledgedStatus(Payload<?> payload) {
		if(payload.body instanceof AgentPingAck
			|| payload.body instanceof UserPolicyAcceptanceUpdateAck
			|| payload.body instanceof AgentPolicyUpdateAck)
			return;
		if(payload.body instanceof Ack)
			agentPendingMessageSiteService.setAsAcknowledged(new CorrelationId(payload.header.messageCorrelationId));
	}
}
