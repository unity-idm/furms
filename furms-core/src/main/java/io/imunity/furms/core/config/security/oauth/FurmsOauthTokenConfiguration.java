/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.oauth;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
class FurmsOauthTokenConfiguration {

	@Bean
	Cache<String, String> oauthTokenCache(@Value("${furms.unity.oAuth.token.refresh-interval:30}") int refreshRate) {
		return CacheBuilder.newBuilder()
				.expireAfterWrite(refreshRate, TimeUnit.SECONDS)
				.build(new CacheLoader<>() {
					@Override
					public String load(String token) {
						return token;
					}
				});
	}

}
