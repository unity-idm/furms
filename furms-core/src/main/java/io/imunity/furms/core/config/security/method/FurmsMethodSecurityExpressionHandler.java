/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.method;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;

class FurmsMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {
	private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();
	private final CapabilityCollector capabilityCollector;

	public FurmsMethodSecurityExpressionHandler(CapabilityCollector capabilityCollector) {
		this.capabilityCollector = capabilityCollector;
	}

	@Override
	protected MethodSecurityExpressionOperations createSecurityExpressionRoot(
		Authentication authentication, MethodInvocation invocation) {
		FurmsMethodSecurityExpressionRoot root =
			new FurmsMethodSecurityExpressionRoot(authentication, capabilityCollector);
		root.setPermissionEvaluator(getPermissionEvaluator());
		root.setTrustResolver(this.trustResolver);
		root.setRoleHierarchy(getRoleHierarchy());
		return root;
	}
}
