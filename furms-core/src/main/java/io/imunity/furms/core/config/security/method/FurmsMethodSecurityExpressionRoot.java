/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.method;

import io.imunity.furms.api.authz.FURMSUserProvider;
import io.imunity.furms.domain.authz.roles.Capability;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.ResourceType;
import io.imunity.furms.domain.users.FURMSUser;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.AUTHENTICATED;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_LIMITED_READ;
import static io.imunity.furms.domain.authz.roles.Capability.OWNED_SSH_KEY_MANAGMENT;

class FurmsMethodSecurityExpressionRoot extends SecurityExpressionRoot
	implements MethodSecurityExpressionOperations {

	private final UserCapabilityCollector userCapabilityCollector;

	FurmsMethodSecurityExpressionRoot(Authentication authentication, UserCapabilityCollector userCapabilityCollector) {
		super(authentication);
		this.userCapabilityCollector = userCapabilityCollector;
	}

	public boolean hasCapabilityForResource(Capability capability, ResourceType resourceType) {
		return hasCapabilityForResource(capability, resourceType, null);
	}

	public boolean hasCapabilityForResource(Capability capability, ResourceType resourceType, String id) {
		if(!authentication.isAuthenticated() || isAnonymousUser())
			return false;

		FURMSUser principal = ((FURMSUserProvider) authentication.getPrincipal()).getFURMSUser();
		ResourceId resourceId = new ResourceId(id, resourceType);
		Set<Capability> capabilities = userCapabilityCollector.getCapabilities(principal.roles, resourceId);
		capabilities.addAll(List.of(AUTHENTICATED, PROJECT_LIMITED_READ, OWNED_SSH_KEY_MANAGMENT));
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
