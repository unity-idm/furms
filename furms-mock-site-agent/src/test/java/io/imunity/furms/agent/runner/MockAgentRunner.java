/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.agent.runner;

import io.imunity.furms.rabbitmq.site.models.converter.FurmsMessageConverter;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MockAgentRunner {
	public static void main(String[] args) {
		new SpringApplicationBuilder(MockAgentRunner.class)
			.web(WebApplicationType.NONE)
			.run(args);
	}

	@Bean
	FurmsMessageConverter messageConverter() {
		return new FurmsMessageConverter();
	}
}
