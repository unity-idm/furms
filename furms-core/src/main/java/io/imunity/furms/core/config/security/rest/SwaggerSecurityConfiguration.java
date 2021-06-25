/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.core.config.security.rest;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import static io.imunity.furms.core.config.security.rest.RestApiSecurityConfigurationOrders.SWAGGER_ORDER;

@Configuration
@Order(SWAGGER_ORDER)
class SwaggerSecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.antMatcher("/api-docs/**")
			.authorizeRequests().anyRequest().permitAll();
	}
}