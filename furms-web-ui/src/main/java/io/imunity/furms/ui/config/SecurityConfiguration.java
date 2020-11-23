/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.config;

import com.vaadin.flow.shared.ApplicationConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.endpoint.*;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.web.client.RestTemplate;


import javax.servlet.http.HttpServletRequest;
import java.util.stream.Stream;

import static com.vaadin.flow.server.HandlerHelper.*;
import static io.imunity.furms.ui.constant.LoginFlowConst.*;

@EnableWebSecurity
@Configuration
class SecurityConfiguration extends WebSecurityConfigurerAdapter
{
	private static final String LOGOUT_SUCCESS_URL = "/front/hello";
	public static final String LOGIN_REGEX = LOGIN_URL + "??(?:&?[^=&]*=[^=&]*)*";

	private final ClientRegistrationRepository clientRegistrationRepository;

	public SecurityConfiguration(ClientRegistrationRepository clientRegistrationRepository)
	{
		this.clientRegistrationRepository = clientRegistrationRepository;
	}

	private final RestTemplate unityRestTemplate;

	SecurityConfiguration(RestTemplate unityRestTemplate)
	{
		this.unityRestTemplate = unityRestTemplate;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception
	{
		http
				// Allow all flow internal requests.
				.authorizeRequests().requestMatchers(SecurityConfiguration::isFrameworkInternalRequest).permitAll()

				// Allow query string for login.
				.and().authorizeRequests().regexMatchers(LOGIN_REGEX).permitAll()

				// Restrict access to our application.
				.and().authorizeRequests().anyRequest().authenticated()

				// Not using Spring CSRF here to be able to use plain HTML for the login page
				.and().csrf().disable()

				// Configure logout
				.logout().logoutUrl(LOGOUT_URL).logoutSuccessUrl(LOGOUT_SUCCESS_URL)

				// Configure the login page.
				.and().oauth2Login().loginPage(LOGIN_URL).defaultSuccessUrl(LOGIN_SUCCESS_URL, true).permitAll()

				// Configure rest client template.
				.and().oauth2Login()
					.tokenEndpoint().accessTokenResponseClient(getAuthorizationTokenResponseClient(unityRestTemplate))
					.and().userInfoEndpoint().userService(getOAuth2UserService(unityRestTemplate));
					.and().authorizationEndpoint()
					.authorizationRequestResolver(new ParamAuthorizationRequestResolver(clientRegistrationRepository));
	}

	@Override
	public void configure(WebSecurity web)
	{
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

	private static boolean isFrameworkInternalRequest(HttpServletRequest request)
	{
		final String parameterValue = request
				.getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER);
		return parameterValue != null &&
				Stream.of(RequestType.values())
						.anyMatch(r -> r.getIdentifier().equals(parameterValue));
	}

	private DefaultAuthorizationCodeTokenResponseClient getAuthorizationTokenResponseClient(RestTemplate restTemplate){
		DefaultAuthorizationCodeTokenResponseClient responseClient = new DefaultAuthorizationCodeTokenResponseClient();
		responseClient.setRestOperations(restTemplate);
		return responseClient;
	}

	private DefaultOAuth2UserService getOAuth2UserService(RestTemplate restTemplate){
		DefaultOAuth2UserService defaultOAuth2UserService = new DefaultOAuth2UserService();
		defaultOAuth2UserService.setRestOperations(restTemplate);
		return defaultOAuth2UserService;
	}
}