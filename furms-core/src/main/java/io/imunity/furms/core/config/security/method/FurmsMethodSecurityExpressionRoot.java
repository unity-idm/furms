/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.method;

import io.imunity.furms.api.authz.FURMSUserProvider;
import io.imunity.furms.domain.Id;
import io.imunity.furms.domain.authz.roles.Capability;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.ResourceType;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.imunity.furms.domain.authz.roles.Capability.AUTHENTICATED;
import static io.imunity.furms.domain.authz.roles.Capability.OWNED_SSH_KEY_MANAGMENT;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_LIMITED_READ;

class FurmsMethodSecurityExpressionRoot
		extends SecurityExpressionRoot
		implements MethodSecurityExpressionOperations {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final List<Capability> ELEMENTARY_CAPABILITIES = List.of(AUTHENTICATED, PROJECT_LIMITED_READ, OWNED_SSH_KEY_MANAGMENT);

	private final UserCapabilityCollector userCapabilityCollector;

	FurmsMethodSecurityExpressionRoot(Authentication authentication, UserCapabilityCollector userCapabilityCollector) {
		super(authentication);
		this.userCapabilityCollector = userCapabilityCollector;
	}

	public boolean hasCapabilityForResource(String method, Capability capability, ResourceType resourceType) {
		return hasCapabilityForResource(method, capability, resourceType, null);
	}

	public boolean hasCapabilityForResources(String method, Capability capability, ResourceType resourceType,
	                                         Collection<Id> ids) {
		if(!authentication.isAuthenticated() || isAnonymousUser())
			return false;

		FURMSUser principal = ((FURMSUserProvider) authentication.getPrincipal()).getFURMSUser();
		List<ResourceId> resourceIds = ids.stream()
			.map(Id::getId)
			.map(id -> new ResourceId(id, resourceType))
			.collect(Collectors.toList());
		Set<Capability> userCapabilities = userCapabilityCollector.getCapabilities(principal.roles, resourceIds, resourceType);
		userCapabilities.addAll(ELEMENTARY_CAPABILITIES);

		final boolean hasCapability = userCapabilities.contains(capability);

		if (!hasCapability) {
			String user = principal.id.map(PersistentId::toString).orElse(principal.email);
			LOG.warn("Access Denied for user \"{}\" with roles: {} when calling \"{}\" with required capability {} for" +
				" resource: {}(id={})", user, principal.roles, method, capability, resourceType, ids);
		}
		return hasCapability;
	}

	public boolean hasCapabilityForResource(String method, Capability capability, ResourceType resourceType, Id id) {
		if(!authentication.isAuthenticated() || isAnonymousUser())
			return false;

		FURMSUser principal = ((FURMSUserProvider) authentication.getPrincipal()).getFURMSUser();
		ResourceId resourceId = new ResourceId(id != null ? id.getId() : null, resourceType);
		Set<Capability> userCapabilities = userCapabilityCollector.getCapabilities(principal.roles, resourceId);
		userCapabilities.addAll(ELEMENTARY_CAPABILITIES);

		final boolean hasCapability = userCapabilities.contains(capability);

		if (!hasCapability) {
			String user = principal.id.map(PersistentId::toString).orElse(principal.email);
			LOG.warn("Access Denied for user \"{}\" with roles: {} when calling \"{}\" with required capability {} for" +
					" resource: {}(id={})", user, principal.roles, method, capability, resourceType, id);
		}
		return hasCapability;
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
