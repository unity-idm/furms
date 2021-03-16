/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.broker;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SiteAgentMock {
	@RabbitListener(queues = "ping-queue")
	public String receive(String car) {
		return "OK";
	}
}
