/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.api.authz;

import io.imunity.furms.domain.authz.roles.Capability;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;

import java.util.Map;
import java.util.Set;

public interface CapabilityCollector {

	Set<Capability> getCapabilities(Map<ResourceId, Set<Role>> roles, ResourceId resourceId);

}
