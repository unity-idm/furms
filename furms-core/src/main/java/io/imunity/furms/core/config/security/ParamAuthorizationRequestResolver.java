/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security;

import io.imunity.furms.domain.constant.RoutesConst;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

class ParamAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {
	private final OAuth2AuthorizationRequestResolver authorizationRequestResolver;
	private final OAuth2AuthorizationRequestResolver authorizationParamRequestResolver;

	ParamAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
		authorizationRequestResolver =
			new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, RoutesConst.OAUTH_START_URL);
		authorizationParamRequestResolver =
			new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, RoutesConst.OAUTH_START_WITH_AUTOPROXY_URL);
	}

	@Override
	public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
		OAuth2AuthorizationRequest authorizationRequest = authorizationRequestResolver.resolve(request);
		OAuth2AuthorizationRequest authorizationParamRequest = authorizationParamRequestResolver.resolve(request);
		return authorizationRequest != null ? authorizationRequest :
			authorizationParamRequest != null ? autoRedirectAuthorizationRequest(authorizationParamRequest) : null;
	}

	@Override
	public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
		OAuth2AuthorizationRequest authorizationRequest =
			authorizationRequestResolver.resolve(request, clientRegistrationId);
		OAuth2AuthorizationRequest authorizationParamRequest =
			authorizationParamRequestResolver.resolve(request, clientRegistrationId);
		return authorizationRequest != null ? authorizationRequest :
			authorizationParamRequest != null ? autoRedirectAuthorizationRequest(authorizationParamRequest) : null;
	}

	private OAuth2AuthorizationRequest autoRedirectAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest) {
		Map<String, Object> additionalParameters = new LinkedHashMap<>(authorizationRequest.getAdditionalParameters());
		additionalParameters.put("uy_auto_login", "true");
		additionalParameters.put("uy_select_authn", "oauth.local");
		return OAuth2AuthorizationRequest.from(authorizationRequest)
			.additionalParameters(additionalParameters)
			.build();
	}
}
