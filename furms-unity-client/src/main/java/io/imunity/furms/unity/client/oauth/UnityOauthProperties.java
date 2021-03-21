/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.oauth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties(prefix = "spring.security.oauth2.client.provider.unity")
public class UnityOauthProperties {

	private final String revoke;
	private final String tokenUri;

	public UnityOauthProperties(String revoke, String tokenUri) {
		this.revoke = revoke;
		this.tokenUri = tokenUri;
	}

	public String getRevoke() {
		return revoke;
	}

	public String getTokenUri() {
		return tokenUri;
	}
}
