/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "io.imunity.furms")
@ConfigurationPropertiesScan("io.imunity.furms")
@EnableScheduling
public class FumrsIntegrationTestsServer {
	public static void main(String[] args) {
		SpringApplication.run(FumrsIntegrationTestsServer.class, args);
	}
}