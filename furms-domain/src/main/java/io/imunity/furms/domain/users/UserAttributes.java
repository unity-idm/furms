/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.domain.users;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.imunity.furms.domain.authz.roles.ResourceId;

public class UserAttributes {
	public final Set<UserAttribute> rootAttributes;
	public final Map<ResourceId, Set<UserAttribute>> attributesByResource;
	
	public UserAttributes(Set<UserAttribute> rootAttributes, Map<ResourceId, Set<UserAttribute>> attributesByResource)
	{
		this.rootAttributes = Set.copyOf(rootAttributes);
		Map<ResourceId, Set<UserAttribute>> byResourceCopy = new HashMap<>(attributesByResource.size());
		attributesByResource.forEach((key, value) -> byResourceCopy.put(key, Set.copyOf(value)));
		this.attributesByResource = Collections.unmodifiableMap(byResourceCopy);
	}
}
