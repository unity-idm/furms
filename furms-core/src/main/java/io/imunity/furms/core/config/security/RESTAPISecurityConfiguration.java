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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.client.RestTemplate;

import io.imunity.furms.core.config.security.user.FurmsOAuth2UserService;
import io.imunity.furms.spi.roles.RoleLoader;

@Configuration
@Order(1)
class RESTAPISecurityConfiguration extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			// Restrict access to our application, except for DispatcherType.ERROR.
			.antMatcher("/rest-api/**")
			.authorizeRequests().anyRequest().authenticated().and()
			.httpBasic();
	}
	
	@Bean                                                             
	public UserDetailsService presharedKeyUsers() throws Exception {
		UserBuilder users = User.withDefaultPasswordEncoder();
		InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
		manager.createUser(users.username("user").password("password").roles("USER").build());
		manager.createUser(users.username("admin").password("password").roles("USER","ADMIN").build());
		return manager;
	}
}