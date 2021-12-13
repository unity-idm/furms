/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.method;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.ExpressionBasedAnnotationAttributeFactory;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.method.DelegatingMethodSecurityMetadataSource;
import org.springframework.security.access.method.MethodSecurityMetadataSource;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
	private final UserCapabilityCollector userCapabilityCollector;

	public MethodSecurityConfig(UserCapabilityCollector userCapabilityCollector) {
		this.userCapabilityCollector = userCapabilityCollector;
	}

	@Override
	protected MethodSecurityExpressionHandler createExpressionHandler() {
		return new FurmsMethodSecurityExpressionHandler(userCapabilityCollector);
	}

	@Override
	public MethodSecurityMetadataSource methodSecurityMetadataSource() {
		DelegatingMethodSecurityMetadataSource methodSecurityMetadataSource =
			(DelegatingMethodSecurityMetadataSource)super.methodSecurityMetadataSource();
		methodSecurityMetadataSource.getMethodSecurityMetadataSources()
			.add(getFurmsAbstractMethodSecurityMetadataSource());
		return methodSecurityMetadataSource;
	}

	private FurmsMethodSecurityMetadataSource getFurmsAbstractMethodSecurityMetadataSource(){
		ExpressionBasedAnnotationAttributeFactory attributeFactory =
			new ExpressionBasedAnnotationAttributeFactory(super.getExpressionHandler());
		return new FurmsMethodSecurityMetadataSource(attributeFactory);
	}
}
