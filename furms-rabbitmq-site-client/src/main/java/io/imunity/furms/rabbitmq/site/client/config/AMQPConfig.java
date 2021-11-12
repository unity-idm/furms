/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.config;

import io.imunity.furms.rabbitmq.site.client.message_resolvers_conector.SiteIdResolversConnector;
import io.imunity.furms.rabbitmq.site.models.Body;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;


@Configuration
class AMQPConfig {
	@Bean
	RabbitAdmin rabbitAdmin(RabbitTemplate rabbitTemplate){
		return new RabbitAdmin(rabbitTemplate);
	}

	@Bean
	@Autowired
	Map<Class<? extends Body>, SiteIdResolversConnector> getAuthorizerMap(List<SiteIdResolversConnector> siteIdResolversConnectors){
		return siteIdResolversConnectors.stream()
			.flatMap(authorizer -> authorizer.getApplicableClasses().stream().map(clazz -> Map.entry(clazz, authorizer)))
			.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
}
