/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.broker;

import io.imunity.furms.broker.runner.QpidBrokerJRunner;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication(scanBasePackageClasses = SiteAgentMock.class)
public class TestSpringContextConfig {

	@Bean
	@Profile("embedded-broker")
	public Queue createQueue(ConnectionFactory factory) throws Exception {
		QpidBrokerJRunner.run(factory.getPort(), "configuration-test.json");
		return new Queue("ping-queue");
	}
}
