/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.broker.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@ActiveProfiles(profiles = "embedded-broker")
@SpringBootTest(properties = "spring.rabbitmq.port=${random.int(30000,40000)}")
public @interface EmbeddedBrokerTest {
}
