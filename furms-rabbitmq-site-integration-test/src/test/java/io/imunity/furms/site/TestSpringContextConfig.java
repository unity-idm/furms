/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site;


import io.imunity.furms.broker.runner.QpidBrokerJRunner;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication(scanBasePackageClasses = {SiteAgentMock.class}, scanBasePackages = "io.imunity.furms.rabbitmq.site.client")
public class TestSpringContextConfig {

	@Bean
	@Profile("embedded-broker")
	public Declarables createQueue(ConnectionFactory factory) throws Exception {
		QpidBrokerJRunner.run(factory.getPort(), "configuration-test.json");
		return new Declarables(new Queue("mock-furms-pub"), new Queue("mock-site-pub"));
	}
}
