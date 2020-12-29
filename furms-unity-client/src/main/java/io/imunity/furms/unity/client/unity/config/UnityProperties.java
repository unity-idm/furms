/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.unity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties(prefix = "furms.unity")
class UnityProperties {

	private final String adminUrl;
	private final String adminUser;
	private final String adminPassword;

	UnityProperties(String adminUrl, String adminUser, String adminPassword) {
		this.adminUrl = adminUrl;
		this.adminUser = adminUser;
		this.adminPassword = adminPassword;
	}

	public String getAdminUrl() {
		return adminUrl;
	}

	public String getAdminUser() {
		return adminUser;
	}

	public String getAdminPassword() {
		return adminPassword;
	}
}
