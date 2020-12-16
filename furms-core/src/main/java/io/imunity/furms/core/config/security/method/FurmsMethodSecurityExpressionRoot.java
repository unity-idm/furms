/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.method;

import io.imunity.furms.core.config.security.user.FurmsUserContext;
import io.imunity.furms.core.config.security.user.capability.Capability;
import io.imunity.furms.core.config.security.user.resource.ResourceId;
import io.imunity.furms.core.config.security.user.resource.ResourceType;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

import static io.imunity.furms.core.config.security.user.capability.CapabilityCollector.getCapabilities;

class FurmsMethodSecurityExpressionRoot extends SecurityExpressionRoot
	implements MethodSecurityExpressionOperations {

	FurmsMethodSecurityExpressionRoot(Authentication authentication) {
		super(authentication);
	}

	public boolean hasCapability(Capability capability, ResourceType resourceType) {
		return hasCapability(capability, resourceType, null);
	}

	public boolean hasCapability(Capability capability, ResourceType resourceType, String id) {
		FurmsUserContext principal = (FurmsUserContext)authentication.getPrincipal();
		ResourceId resourceId = new ResourceId(id, resourceType);

		return getCapabilities(principal.roles, resourceId)
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
