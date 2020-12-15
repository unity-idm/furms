/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.method;

import io.imunity.furms.core.config.security.user.FurmsRole;
import io.imunity.furms.core.config.security.user.FurmsUserContext;
import io.imunity.furms.core.config.security.user.ResourceId;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

class FurmsMethodSecurityExpressionRoot extends SecurityExpressionRoot
	implements MethodSecurityExpressionOperations {

	FurmsMethodSecurityExpressionRoot(Authentication authentication) {
		super(authentication);
	}

	public boolean hasCapability(String capability) {
		FurmsUserContext principal = (FurmsUserContext)authentication.getPrincipal();
		ResourceId resourceId = new ResourceId(null, null);

		Map<FurmsRole, List<ResourceId>> roles = principal.roles;

		return true;
//		Map<ResourceId, Set<Capability>> roles = principal.roles;
//
//
//		return roles.keySet().stream()
//			.filter(r -> r.getCapabilities().contains(capability))
//			.anyMatch(r -> roles.get(r).contains(resourceId));
//			roles.stream()
//			.flatMap(role -> role.getCapabilities().stream())
//			.anyMatch(c -> c.equals(capability));
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
