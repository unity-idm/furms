/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.agent.runner;

import io.imunity.furms.rabbitmq.site.models.CumulativeResourceUsageRecord;
import io.imunity.furms.rabbitmq.site.models.Header;
import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.rabbitmq.site.models.Status;
import io.imunity.furms.rabbitmq.site.models.UserResourceUsageRecord;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.time.OffsetDateTime;
import java.util.UUID;

import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;

@ShellComponent
class UsageMockShellProducer {
	private final RabbitTemplate rabbitTemplate;
	@Value("${queue.res-name}")
	private String responseQueueName;

	UsageMockShellProducer(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@ShellMethod(key = "send", value = "send resource usage")
	public void sendCumulativeResourceUsageRecord(
		String projectIdentifier,
		String allocationIdentifier,
		double cumulativeConsumption
	) {
		Header header = getHeader(UUID.randomUUID().toString());
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, new CumulativeResourceUsageRecord(
			projectIdentifier,
			allocationIdentifier,
			cumulativeConsumption,
			OffsetDateTime.now()
		)));
	}

	@ShellMethod(key = "user send", value = "send resource usage per user")
	public void sendUserResourceUsageRecord(
		String projectIdentifier,
		String allocationIdentifier,
		String fenixUserId,
		double cumulativeConsumption
	) {
		Header header = getHeader(UUID.randomUUID().toString());
		rabbitTemplate.convertAndSend(responseQueueName, new Payload<>(header, new UserResourceUsageRecord(
			projectIdentifier,
			allocationIdentifier,
			fenixUserId,
			cumulativeConsumption,
			OffsetDateTime.now()
		)));
	}

	private Header getHeader(String messageCorrelationId) {
		return new Header(VERSION, messageCorrelationId, Status.OK, null);
	}
}
