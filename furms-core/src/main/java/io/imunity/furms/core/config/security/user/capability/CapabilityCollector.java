/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.user.capability;

import io.imunity.furms.core.config.security.user.resource.ResourceId;
import io.imunity.furms.core.config.security.user.role.FurmsRole;
import io.imunity.furms.core.config.security.user.role.Role;
import io.imunity.furms.core.config.security.user.role.SpecialRole;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.function.Function.identity;

public class CapabilityCollector {
	public static Stream<Capability> getCapabilities(Map<ResourceId, Set<FurmsRole>> roles, ResourceId resourceId){
		Stream<Capability> additionalCapabilities = roles.values().stream()
			.flatMap(Collection::stream)
			.filter(r -> r == Role.FENIX_ROLE.ADMIN)
			.map(r -> (SpecialRole) r)
			.map(SpecialRole::getAdditionalCapabilities)
			.flatMap(Collection::stream);

		Stream<Capability> capabilityStream = roles.getOrDefault(resourceId, new HashSet<>()).stream()
			.flatMap(x -> x.getCapabilities().stream());

		return Stream.of(additionalCapabilities, capabilityStream)
			.flatMap(identity());
	}
}
