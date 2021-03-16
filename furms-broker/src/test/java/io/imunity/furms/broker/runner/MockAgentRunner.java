/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.broker.runner;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import static io.imunity.furms.broker.QueuesNamesConst.PING_QUEUE;

@SpringBootApplication
public class MockAgentRunner {
	@RabbitListener(queues = PING_QUEUE)
	public String receive(String ping) {
		return "OK";
	}

	public static void main(String[] args) {
		new SpringApplicationBuilder(MockAgentRunner.class)
			.web(WebApplicationType.NONE)
			.run(args);
	}
}
