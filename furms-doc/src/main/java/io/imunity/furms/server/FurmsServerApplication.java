/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "io.imunity.furms")
@ConfigurationPropertiesScan("io.imunity.furms")
@EnableScheduling
public class FurmsServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(FurmsServerApplication.class, args);
	}
}
