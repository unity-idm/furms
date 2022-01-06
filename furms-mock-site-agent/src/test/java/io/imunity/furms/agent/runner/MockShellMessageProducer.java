/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.agent.runner;

import io.imunity.furms.rabbitmq.site.models.AgentProjectAllocationInstallationResult;
import io.imunity.furms.rabbitmq.site.models.AgentProjectAllocationUpdate;
import io.imunity.furms.rabbitmq.site.models.CumulativeResourceUsageRecord;
import io.imunity.furms.rabbitmq.site.models.Header;
import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.rabbitmq.site.models.Status;
import io.imunity.furms.rabbitmq.site.models.UserResourceUsageRecord;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;

@ShellComponent
class MockShellMessageProducer {
	private final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ISO_DATE_TIME;
	private final RabbitTemplate rabbitTemplate;
	@Value("${queue.res-name}")
	private String responseQueueName;

	MockShellMessageProducer(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@ShellMethod(key = "send usage", value = "send resource usage")
	public void sendCumulativeResourceUsageRecord(
		String projectIdentifier,
		String allocationIdentifier,
		BigDecimal cumulativeConsumption,
		@ShellOption(defaultValue="") String probedAt
	) {
		Header header = getHeader(UUID.randomUUID().toString());
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, new CumulativeResourceUsageRecord(
			projectIdentifier,
			allocationIdentifier,
			cumulativeConsumption,
			probedAt.isEmpty() ? OffsetDateTime.now() : dateTimeFormat.parse(probedAt, OffsetDateTime::from)
		)));
	}

	@ShellMethod(key = "send user usage", value = "send resource usage per user")
	public void sendUserResourceUsageRecord(
		String projectIdentifier,
		String allocationIdentifier,
		String fenixUserId,
		BigDecimal cumulativeConsumption
	) {
		Header header = getHeader(UUID.randomUUID().toString());
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, new UserResourceUsageRecord (
			projectIdentifier,
			allocationIdentifier,
			fenixUserId,
			cumulativeConsumption,
			OffsetDateTime.now()
		)));
	}

	@ShellMethod(key = "create chunk", value = "create chunk")
	public void sendNewChunk(
		String allocationIdentifier,
		String allocationChunkIdentifier,
		BigDecimal amount,
		String validTo,
		String validFrom
	) {
		Header header = getHeader(UUID.randomUUID().toString());
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, AgentProjectAllocationInstallationResult.builder()
			.allocationIdentifier(allocationIdentifier)
			.allocationChunkIdentifier(allocationChunkIdentifier)
			.amount(amount)
			.validTo(dateTimeFormat.parse(validTo, OffsetDateTime::from))
			.validFrom(dateTimeFormat.parse(validFrom, OffsetDateTime::from))
			.build()
		));
	}

	@ShellMethod(key = "update chunk", value = "update chunk")
	public void updateChunk(
		String allocationIdentifier,
		String allocationChunkIdentifier,
		BigDecimal amount,
		String validTo,
		String validFrom
	) {
		Header header = getHeader(UUID.randomUUID().toString());
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, AgentProjectAllocationUpdate.builder()
			.allocationIdentifier(allocationIdentifier)
			.allocationChunkIdentifier(allocationChunkIdentifier)
			.amount(amount)
			.validTo(dateTimeFormat.parse(validTo, OffsetDateTime::from))
			.validFrom(dateTimeFormat.parse(validFrom, OffsetDateTime::from))
			.build()
		));
	}

	private Header getHeader(String messageCorrelationId) {
		return new Header(VERSION, messageCorrelationId, Status.OK, null);
	}
}
