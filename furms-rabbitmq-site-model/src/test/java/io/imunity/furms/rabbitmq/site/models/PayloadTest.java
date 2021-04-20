/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

public class PayloadTest {

	private Jackson2JsonMessageConverter getJackson2JsonMessageConverter() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false);
		mapper.registerModule(new JavaTimeModule());
		Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter(mapper);
		return jackson2JsonMessageConverter;
	}

	@Test
	void name() {
		Payload aaa = new Payload(null, new AgentPingAckTest("aaa", 5));
		Jackson2JsonMessageConverter jackson2JsonMessageConverter = getJackson2JsonMessageConverter();
		Message message1 = jackson2JsonMessageConverter.toMessage(new Payload(null, new AgentPingAckTest("aaa", 5)), new MessageProperties());
		System.out.println(message1);
	}
}