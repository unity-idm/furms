/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.core.config.security.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.imunity.furms.core.users.api.key.AdminApiKeyFinder;
import io.imunity.furms.spi.roles.RoleLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import io.imunity.furms.core.config.security.SecurityProperties;
import io.imunity.furms.core.config.security.UserContextSetterFilter;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.ResourceType;
import io.imunity.furms.domain.authz.roles.Role;

import static io.imunity.furms.core.config.security.rest.RestApiSecurityConfigurationOrders.REST_API_ORDER;

@Configuration
@Order(REST_API_ORDER)
class RESTAPISecurityConfiguration extends WebSecurityConfigurerAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(RESTAPISecurityConfiguration.class);
	private final SecurityProperties configuration;
	private final PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
	private final AdminApiKeyFinder adminApiKeyFinder;
	private final RoleLoader roleLoader;

	RESTAPISecurityConfiguration(
			SecurityProperties configuration,
			AdminApiKeyFinder adminApiKeyFinder,
			RoleLoader roleLoader
	) {
		this.configuration = configuration;
		this.adminApiKeyFinder = adminApiKeyFinder;
		this.roleLoader = roleLoader;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.antMatcher("/rest-api/**")
				.addFilterAfter(new UserContextSetterFilter(), BasicAuthenticationFilter.class)
				.addFilterAt(
						new RestApiBasicAuthenticationFilter(super.authenticationManagerBean(), adminApiKeyFinder, roleLoader),
						BasicAuthenticationFilter.class)
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
				.csrf().disable()
				.authorizeRequests().anyRequest().authenticated().and()
				.httpBasic();
	}

	@Bean
	public UserDetailsService presharedKeyUsers() {

		List<PresetUser> users = new ArrayList<>();
		if (hasCentralIdPUserDefined())
			users.add(createCentralIdPUser());
		return new PresetUsersProvider(users);
	}

	private boolean hasCentralIdPUserDefined() {
		if (configuration.getCentralIdPUsername().isEmpty() || configuration.getCentralIdPSecret().isEmpty()) {
			LOG.warn("Central IdP access credentials are not configured. CIdP REST endpoint won't be accessible.");
			return false;
		}
		return true;
	}

	private PresetUser createCentralIdPUser() {
		Map<ResourceId, Set<Role>> rolesMap = Map.of(new ResourceId((String) null, ResourceType.APP_LEVEL),
				Set.of(Role.CENTRAL_IDP));

		return new PresetUser(configuration.getCentralIdPUsername().get(),
				encoder.encode(configuration.getCentralIdPSecret().get()),
				Collections.emptySet(),
				rolesMap);
	}
}