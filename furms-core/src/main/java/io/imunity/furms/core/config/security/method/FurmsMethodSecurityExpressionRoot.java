/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.method;

import io.imunity.furms.core.config.security.user.FurmsOAuth2User;
import io.imunity.furms.core.config.security.user.Role;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

import java.util.List;

import static java.util.Collections.emptyList;

class FurmsMethodSecurityExpressionRoot extends SecurityExpressionRoot
	implements MethodSecurityExpressionOperations {

	FurmsMethodSecurityExpressionRoot(Authentication authentication) {
		super(authentication);
	}

	public boolean hasCapability(String capability) {
		FurmsOAuth2User principal = (FurmsOAuth2User)authentication.getPrincipal();
		List<Role> roles = principal.roles.getOrDefault(principal.currentGroup, emptyList());
		return roles.stream()
			.flatMap(role -> role.getCapabilities().stream())
			.anyMatch(c -> c.equals(capability));
	}

	@Override
	public void setFilterObject(Object o) {

	}

	@Override
	public Object getFilterObject() {
		return null;
	}

	@Override
	public void setReturnObject(Object o) {

	}

	@Override
	public Object getReturnObject() {
		return null;
	}

	@Override
	public Object getThis() {
		return null;
	}
}
