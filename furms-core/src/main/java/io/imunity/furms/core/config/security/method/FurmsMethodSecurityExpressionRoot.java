/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.method;

import io.imunity.furms.core.config.security.user.FurmsAuthenticatedUser;
import io.imunity.furms.core.config.security.user.capability.CapabilityCollector;
import io.imunity.furms.domain.authz.roles.Capability;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.ResourceType;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

class FurmsMethodSecurityExpressionRoot extends SecurityExpressionRoot
	implements MethodSecurityExpressionOperations {

	private final CapabilityCollector capabilityCollector;

	FurmsMethodSecurityExpressionRoot(Authentication authentication, CapabilityCollector capabilityCollector) {
		super(authentication);
		this.capabilityCollector = capabilityCollector;
	}

	public boolean hasCapabilityForResource(Capability capability, ResourceType resourceType) {
		return hasCapabilityForResource(capability, resourceType, null);
	}

	public boolean hasCapabilityForResource(Capability capability, ResourceType resourceType, String id) {
		FurmsAuthenticatedUser principal = (FurmsAuthenticatedUser)authentication.getPrincipal();
		ResourceId resourceId = new ResourceId(id, resourceType);

		return capabilityCollector.getCapabilities(principal.roles, resourceId).contains(capability);
	}

	/***
	 * The filtering in this class is not supported, so method is empty.
	 */
	@Override
	public void setFilterObject(Object o) {
	}

	/***
	 * The filtering in this class is not supported, so method returns null.
	 */
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
