/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms;

import com.google.common.cache.Cache;
import io.imunity.furms.core.config.security.oauth.TokenRefreshHandler;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;

@SpringBootApplication(scanBasePackages = {"io.imunity.furms"})
public class TestSpringBootSecurityConfig {

	@MockBean
	private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

	@MockBean
	private TokenRefreshHandler tokenRefreshHandler;

	@MockBean
	private Cache<String, String> cache;

}
