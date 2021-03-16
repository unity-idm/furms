/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.broker;

import io.imunity.furms.domain.site_messages.PingStatus;
import io.imunity.furms.site.api.SiteMessager;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

import static io.imunity.furms.broker.QueuesNamesConst.PING_QUEUE;
import static io.imunity.furms.domain.site_messages.PingStatus.*;

@Component
class SiteMessagerImpl implements SiteMessager {
	private final AsyncRabbitTemplate rabbitTemplate;

	public SiteMessagerImpl(AsyncRabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	public CompletableFuture<PingStatus> ping() {
		CompletableFuture<PingStatus> future = new CompletableFuture<>();
		AsyncRabbitTemplate.RabbitConverterFuture<Object> rabbitFuture = rabbitTemplate.convertSendAndReceive(PING_QUEUE, "ping");
		rabbitFuture.addCallback(x -> future.complete(OK), x -> future.complete(FAILED));
		return future;
	}
}
