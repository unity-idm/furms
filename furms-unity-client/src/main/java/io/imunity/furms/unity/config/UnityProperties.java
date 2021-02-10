/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties(prefix = "furms.unity")
class UnityProperties {

	private final String adminUrl;
	private final String adminUser;
	private final String adminPassword;
	private final String url;

	UnityProperties(String adminUrl, String adminUser, String adminPassword, String url) {
		this.adminUrl = adminUrl;
		this.adminUser = adminUser;
		this.adminPassword = adminPassword;
		this.url = url;
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

	public String getUrl() {
		return url;
	}
}
