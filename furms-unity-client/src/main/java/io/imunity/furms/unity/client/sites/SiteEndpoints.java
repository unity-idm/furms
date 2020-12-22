/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.sites;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties(prefix = "furms.unity.endpoints.site")
public class SiteEndpoints {

	private final String baseDecoded;
	private final String baseEncoded;
	private final String meta;
	private final String users;

	public SiteEndpoints(String baseDecoded, String baseEncoded, String meta, String users) {
		this.baseDecoded = baseDecoded;
		this.baseEncoded = baseEncoded;
		this.meta = meta;
		this.users = users;
	}

	public String getBaseDecoded() {
		return baseDecoded;
	}

	public String getBaseEncoded() {
		return baseEncoded;
	}

	public String getMeta() {
		return meta;
	}

	public String getUsers() {
		return users;
	}
}
