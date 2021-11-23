/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site;

import io.imunity.furms.broker.runner.QpidBrokerJRunner;
import io.imunity.furms.rabbitmq.site.client.mocks.SiteAgentMock;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@SpringBootApplication(
		scanBasePackageClasses = {SiteAgentMock.class},
		scanBasePackages = "io.imunity.furms.rabbitmq.site.client")
public class TestSpringContextConfig {
	public static final Instant testTime = Instant.now();

	@Configuration
	@Profile("embedded-broker")
	public static class QpidBorkerStarter {
		public QpidBorkerStarter(ConnectionFactory factory) throws Exception {
			QpidBrokerJRunner.run(factory.getPort(), "configuration-test.json");
		}
	}

	@Bean
	public Declarables createQueues() {
		return new Declarables(new Queue("mock-furms-pub"), new Queue("mock-site-pub"));
	}

	@Bean
	public Clock createClock(){
		return Clock.fixed(testTime, ZoneId.systemDefault());
	}
}
