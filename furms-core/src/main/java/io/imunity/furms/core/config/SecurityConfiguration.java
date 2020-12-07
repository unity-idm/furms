/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.core.config;

import io.imunity.furms.core.constant.LoginFlowConst;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@EnableWebSecurity
@Configuration
class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	private static final String REQUEST_TYPE_PARAMETER = "v-r";
	private static final List<String> REQUESTED_TYPES = List.of("uidl", "heartbeat", "push");

	private final ClientRegistrationRepository clientRegistrationRepo;
	private final RestTemplate unityRestTemplate;

	SecurityConfiguration(RestTemplate unityRestTemplate, ClientRegistrationRepository clientRegistrationRepo) {
		this.unityRestTemplate = unityRestTemplate;
		this.clientRegistrationRepo = clientRegistrationRepo;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			// Allow all flow internal requests.
			.authorizeRequests().requestMatchers(SecurityConfiguration::isFrameworkInternalRequest).permitAll()

			// Allow query string for login.
			.and().authorizeRequests().requestMatchers(r -> r.getRequestURI().startsWith(LoginFlowConst.LOGIN_URL)).permitAll()

			// Restrict access to our application.
			.and().authorizeRequests().anyRequest().authenticated()

			// Not using Spring CSRF, Vaadin has built-in Cross-Site Request Forgery already
			.and().csrf().disable()

			// Configure logout
			.logout().logoutUrl(LoginFlowConst.LOGOUT_URL).logoutSuccessUrl(LoginFlowConst.LOGIN_URL)

			// Configure the login page.
			.and().oauth2Login().loginPage(LoginFlowConst.LOGIN_URL).defaultSuccessUrl(LoginFlowConst.LOGIN_SUCCESS_URL, true).permitAll()

			// Configure rest client template.
			.and().oauth2Login()
			.tokenEndpoint().accessTokenResponseClient(getAuthorizationTokenResponseClient(unityRestTemplate))
			.and().userInfoEndpoint().userService(getOAuth2UserService(unityRestTemplate))
			.and().authorizationEndpoint()
				.authorizationRequestResolver(new ParamAuthorizationRequestResolver(clientRegistrationRepo));
	}

	@Override
	public void configure(WebSecurity web) {
		web.ignoring().antMatchers(
			// client-side JS code
			"/VAADIN/**", "/front/VAADIN/**",

			// the standard favicon URI
			"/front/favicon.ico",

			// web application manifest
			"/front/manifest.webmanifest", "/front/sw.js", "/front/offline-page.html",

			// icons and images
			"/front/icons/**", "/front/images/**");
	}

	private static boolean isFrameworkInternalRequest(HttpServletRequest request) {
		String parameterValue = request.getParameter(REQUEST_TYPE_PARAMETER);
		return parameterValue != null && REQUESTED_TYPES.contains(parameterValue);
	}

	private DefaultAuthorizationCodeTokenResponseClient getAuthorizationTokenResponseClient(RestTemplate restTemplate) {
		DefaultAuthorizationCodeTokenResponseClient responseClient = new DefaultAuthorizationCodeTokenResponseClient();
		responseClient.setRestOperations(restTemplate);
		return responseClient;
	}

	private DefaultOAuth2UserService getOAuth2UserService(RestTemplate restTemplate) {
		return new FurmsOAuth2UserService(restTemplate);
	}
}