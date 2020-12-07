/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config;

import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

public class CustomMethodSecurityExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {

	public CustomMethodSecurityExpressionRoot(Authentication authentication) {
		super(authentication);
	}

	public boolean isMember() {
		authentication.getAuthorities();
		FurmsOAuth2User principal = (FurmsOAuth2User)authentication.getPrincipal();

//		User user = ((MyUserPrincipal) this.getPrincipal()).getUser();
//		return user.getOrganization().getId().longValue() == OrganizationId.longValue();
		return true;
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
