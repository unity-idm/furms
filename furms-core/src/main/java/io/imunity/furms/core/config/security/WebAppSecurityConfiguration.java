/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.core.config.security;

import static io.imunity.furms.domain.constant.RoutesConst.FRONT;
import static io.imunity.furms.domain.constant.RoutesConst.FRONT_LOGOUT_URL;
import static io.imunity.furms.domain.constant.RoutesConst.LOGIN_ERROR_URL;
import static io.imunity.furms.domain.constant.RoutesConst.LOGIN_SUCCESS_URL;
import static io.imunity.furms.domain.constant.RoutesConst.LOGIN_URL;
import static io.imunity.furms.domain.constant.RoutesConst.PUBLIC_URL;

import java.lang.invoke.MethodHandles;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.client.RestTemplate;

import io.imunity.furms.core.config.security.oauth.FurmsOAuth2UserService;
import io.imunity.furms.core.config.security.oauth.FurmsOauthLogoutFilter;
import io.imunity.furms.core.config.security.oauth.FurmsOauthTokenExtenderFilter;
import io.imunity.furms.spi.roles.RoleLoader;

@EnableWebSecurity
@Configuration
public class WebAppSecurityConfiguration extends WebSecurityConfigurerAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ClientRegistrationRepository clientRegistrationRepo;
	private final RestTemplate unityRestTemplate;
	private final TokenRevokerHandler tokenRevokerHandler;
	private final RoleLoader roleLoader;
	private final FurmsOauthTokenExtenderFilter furmsOauthTokenExtenderFilter;
	private final FurmsOauthLogoutFilter furmsOauthLogoutFilter;

	WebAppSecurityConfiguration(RestTemplate unityRestTemplate,
	                            ClientRegistrationRepository clientRegistrationRepo,
	                            TokenRevokerHandler tokenRevokerHandler,
	                            RoleLoader roleLoader,
	                            FurmsOauthTokenExtenderFilter furmsOauthTokenExtenderFilter,
	                            FurmsOauthLogoutFilter furmsOauthLogoutFilter) {
		this.unityRestTemplate = unityRestTemplate;
		this.clientRegistrationRepo = clientRegistrationRepo;
		this.tokenRevokerHandler = tokenRevokerHandler;
		this.roleLoader = roleLoader;
		this.furmsOauthTokenExtenderFilter = furmsOauthTokenExtenderFilter;
		this.furmsOauthLogoutFilter = furmsOauthLogoutFilter;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.addFilterAfter(new UserContextSetterFilter(), SecurityContextPersistenceFilter.class)
			.addFilterAfter(furmsOauthLogoutFilter, ConcurrentSessionFilter.class)
			.addFilterAfter(furmsOauthTokenExtenderFilter, FurmsOauthLogoutFilter.class)

			// Allow access to /public.
			.authorizeRequests().requestMatchers(r -> r.getRequestURI().startsWith(PUBLIC_URL)).permitAll()

			// Restrict access to our application, except for DispatcherType.ERROR.
			.and().requestMatchers().requestMatchers(new NonErrorDispatcherTypeRequestMatcher())
			.and().authorizeRequests().anyRequest().authenticated()

			// Not using Spring CSRF, Vaadin has built-in Cross-Site Request Forgery already
			.and().csrf().disable()

			// Configure logout
			.logout()
				.logoutRequestMatcher(new AntPathRequestMatcher(FRONT_LOGOUT_URL, "GET"))
				.logoutSuccessHandler(tokenRevokerHandler)

			// Configure redirect entrypoint
			.and().exceptionHandling().authenticationEntryPoint(new FurmsEntryPoint(LOGIN_URL))

			// Configure the login page.
			.and().oauth2Login().loginPage(LOGIN_URL)
				.defaultSuccessUrl(LOGIN_SUCCESS_URL, true)
				.failureUrl(LOGIN_ERROR_URL).permitAll()

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
			"/css/**",

			// client-side JS code
			"/VAADIN/**", FRONT + "/VAADIN/**",

			// the standard favicon URI
			FRONT + "/favicon.ico", "/favicon.ico",

			// web application manifest
			FRONT + "/manifest.webmanifest", FRONT + "/sw.js", FRONT + "/offline-page.html",

			// icons and images
			FRONT + "/icons/**", FRONT + "/images/**");
	}

	private DefaultAuthorizationCodeTokenResponseClient getAuthorizationTokenResponseClient(RestTemplate restTemplate) {
		DefaultAuthorizationCodeTokenResponseClient responseClient = new DefaultAuthorizationCodeTokenResponseClient();
		responseClient.setRestOperations(restTemplate);
		return responseClient;
	}

	private DefaultOAuth2UserService getOAuth2UserService(RestTemplate restTemplate) {
		return new FurmsOAuth2UserService(restTemplate, roleLoader);
	}

	private static class NonErrorDispatcherTypeRequestMatcher implements RequestMatcher {
		@Override
		public boolean matches(HttpServletRequest request) {
			if (request.getDispatcherType() == DispatcherType.ERROR) {
				LOG.trace("Skipping error dispatched request processing by security filters: {}", request);
				return false;
			}
			return true;
		}
	}
}