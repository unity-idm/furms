/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.user.capability;

import io.imunity.furms.core.config.security.user.resource.ResourceId;
import io.imunity.furms.core.config.security.user.role.Role;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CapabilityCollector {
	public static Set<Capability> getCapabilities(Map<ResourceId, Set<Role>> roles, ResourceId resourceId){
		Set<Capability> capabilities = getAdditionalCapabilities(roles);
		for(Role role: roles.getOrDefault(resourceId, new HashSet<>())){
			capabilities.addAll(role.capabilities);
		}
		return capabilities;
	}

	private static Set<Capability> getAdditionalCapabilities(Map<ResourceId, Set<Role>> resourceIdToRoles) {
		Set<Capability> capabilities = new HashSet<>();
		Set<Role> roles = new HashSet<>();

		for(Set<Role> role: resourceIdToRoles.values()){
			roles.addAll(role);
		}
		for(Role r : roles){
			capabilities.addAll(r.additionalCapabilities);
		}
		return capabilities;
	}
}
