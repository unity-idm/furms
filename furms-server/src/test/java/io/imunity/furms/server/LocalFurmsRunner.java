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
@EnableScheduling
@ConfigurationPropertiesScan("io.imunity.furms")
public class LocalFurmsRunner {
	public static void main(String[] args) {
		SpringApplication.run(LocalFurmsRunner.class, args);
	}
}
