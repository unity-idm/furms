/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class AMQPConfig {
	@Bean
	Queue replyQueue(){
		return new Queue("reply-queue");
	}

	@Bean
	RabbitAdmin rabbitAdmin(RabbitTemplate rabbitTemplate){
		return new RabbitAdmin(rabbitTemplate);
	}
}
