/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.core.config.security;

import io.imunity.furms.core.config.security.oauth.FurmsOAuth2UserService;
import io.imunity.furms.spi.roles.RoleLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.client.RestTemplate;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import java.lang.invoke.MethodHandles;

import static io.imunity.furms.domain.constant.RoutesConst.FRONT;
import static io.imunity.furms.domain.constant.RoutesConst.LOGIN_ERROR_URL;
import static io.imunity.furms.domain.constant.RoutesConst.LOGIN_URL;
import static io.imunity.furms.domain.constant.RoutesConst.LOGOUT_TRIGGER_URL;
import static io.imunity.furms.domain.constant.RoutesConst.PUBLIC_URL;

@EnableWebSecurity
@Configuration
public class WebAppSecurityConfiguration extends WebSecurityConfigurerAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ClientRegistrationRepository clientRegistrationRepo;
	private final RestTemplate unityRestTemplate;
	private final TokenRevokerHandler tokenRevokerHandler;
	private final FurmsAuthenticationSuccessHandler authenticationSuccessHandler;
	private final RoleLoader roleLoader;

	WebAppSecurityConfiguration(RestTemplate unityRestTemplate,
	                            ClientRegistrationRepository clientRegistrationRepo,
	                            TokenRevokerHandler tokenRevokerHandler,
	                            FurmsAuthenticationSuccessHandler authenticationSuccessHandler,
	                            RoleLoader roleLoader) {
		this.unityRestTemplate = unityRestTemplate;
		this.clientRegistrationRepo = clientRegistrationRepo;
		this.tokenRevokerHandler = tokenRevokerHandler;
		this.authenticationSuccessHandler = authenticationSuccessHandler;
		this.roleLoader = roleLoader;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.addFilterAfter(new UserContextSetterFilter(), SecurityContextPersistenceFilter.class)

			// Allow access to /public.
			.authorizeRequests().requestMatchers(r -> r.getRequestURI().startsWith(PUBLIC_URL)).permitAll()

			// Restrict access to our application, except for DispatcherType.ERROR.
			.and().requestMatchers().requestMatchers(new NonErrorDispatcherTypeRequestMatcher())
			.and().authorizeRequests().anyRequest().authenticated()

			// Not using Spring CSRF, Vaadin has built-in Cross-Site Request Forgery already
			.and().csrf().disable()

			// Configure logout
			.logout()
				.logoutUrl(LOGOUT_TRIGGER_URL)
				.logoutSuccessHandler(tokenRevokerHandler)

			// Configure redirect entrypoint
			.and().exceptionHandling()
				.defaultAuthenticationEntryPointFor(
					(request, response, accessDeniedException) -> response.setStatus(HttpStatus.UNAUTHORIZED.value()),
					new VaadinXHRRequestMatcher()
				)
				.defaultAuthenticationEntryPointFor(new FurmsEntryPoint(LOGIN_URL), new NonVaadinXHRRequestMatcher())

			// Configure the login page.
			.and().oauth2Login().loginPage(LOGIN_URL)
				.successHandler(authenticationSuccessHandler)
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
			FRONT + "/icons/**", FRONT + "/images/**",

			// endpoint for registration flow
			PUBLIC_URL + "/registration"
			);
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
	
	/**
	 * Basic recognition of Vaadin XHR request. For those we do not want to have a redirect to login page, just a 
	 * http error when we are unauthorized. 
	 */
	private static class NonVaadinXHRRequestMatcher implements RequestMatcher {
		@Override
		public boolean matches(HttpServletRequest request) {
			if (request.getDispatcherType() == DispatcherType.REQUEST) {
				LOG.trace("Checking if request is not Vaadin XHR: {}", request.getParameter("v-r") == null);
				return request.getParameter("v-r") == null;
			}
			LOG.trace("Checking if request is not Vaadin XHR: false");
			return false;
		}
	}

	private static class VaadinXHRRequestMatcher implements RequestMatcher {
		@Override
		public boolean matches(HttpServletRequest request) {
			if (request.getDispatcherType() == DispatcherType.REQUEST) {
				LOG.trace("Checking if request is Vaadin XHR: {}", request.getParameter("v-r") != null);
				return request.getParameter("v-r") != null;
			}
			LOG.trace("Checking if request is Vaadin XHR: false");
			return false;
		}
	}
}