/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.unity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties(prefix = "furms.unity")
class UnityConfig {

	private final String baseUrl;
	private final String adminUser;
	private final String adminPassword;

	UnityConfig(String baseUrl, String adminUser, String adminPassword) {
		this.baseUrl = baseUrl;
		this.adminUser = adminUser;
		this.adminPassword = adminPassword;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public String getAdminUser() {
		return adminUser;
	}

	public String getAdminPassword() {
		return adminPassword;
	}
}
