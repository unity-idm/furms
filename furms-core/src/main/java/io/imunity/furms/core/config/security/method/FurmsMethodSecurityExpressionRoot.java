/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.method;

import io.imunity.furms.core.config.security.FurmsAuthenticatedUser;
import io.imunity.furms.domain.authz.roles.Capability;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.ResourceType;

import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.*;

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
		if(!authentication.isAuthenticated() || isAnonymousUser())
			return false;

		FurmsAuthenticatedUser principal = (FurmsAuthenticatedUser) authentication.getPrincipal();
		ResourceId resourceId = new ResourceId(id, resourceType);
		Set<Capability> capabilities = capabilityCollector.getCapabilities(principal.getRoles(), resourceId);
		capabilities.add(AUTHENTICATED);
		return capabilities.contains(capability);
	}

	private boolean isAnonymousUser() {
		return authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.anyMatch(role -> role.equals("ROLE_ANONYMOUS"));
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
