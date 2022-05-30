/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.resource_credits;

import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.resource_types.ResourceTypeId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

class ResourceTypeComboBoxModelResolver {
	private final Map<ResourceTypeId, ResourceTypeComboBoxModel> map;

	public ResourceTypeComboBoxModelResolver(Set<ResourceType> resourceTypes) {
		map = resourceTypes.stream()
			.map(resourceType -> new ResourceTypeComboBoxModel(resourceType.id, resourceType.name, resourceType.unit))
			.collect(toMap(x -> x.id, x -> x));
	}

	public List<ResourceTypeComboBoxModel> getResourceTypes(){
		return new ArrayList<>(map.values());
	}

	public String getName(ResourceTypeId id){
		return map.get(id).name;
	}

	public ResourceTypeComboBoxModel getResourceType(ResourceTypeId id){
		return map.get(id);
	}
}
