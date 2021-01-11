/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vaadin.flow.spring.SpringServlet;
import com.vaadin.flow.spring.annotation.EnableVaadin;

import static io.imunity.furms.domain.constant.RoutesConst.FRONT;


@Configuration
@EnableVaadin("io.imunity.furms")
class VaadinConfiguration {
	@Bean
	public ServletRegistrationBean<SpringServlet> configVaadinMapping(ApplicationContext context) {
		return new ServletRegistrationBean<>(new SpringServlet(context, false), FRONT + "/*");
	}
}
