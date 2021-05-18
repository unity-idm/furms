/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.resource_types;

import io.imunity.furms.domain.resource_types.ResourceType;

class ResourceTypeViewModelMapper {
	static ResourceTypeViewModel map(ResourceType resourceType) {
		return ResourceTypeViewModel.builder()
			.id(resourceType.id)
			.siteId(resourceType.siteId)
			.serviceId(resourceType.serviceId)
			.name(resourceType.name)
			.type(resourceType.type)
			.unit(resourceType.unit)
			.accessible(resourceType.accessibleForAllProjectMembers)
			.build();
	}

	static ResourceType map(ResourceTypeViewModel resourceTypeViewModel){
		return ResourceType.builder()
			.id(resourceTypeViewModel.getId())
			.siteId(resourceTypeViewModel.getSiteId())
			.serviceId(resourceTypeViewModel.getServiceId())
			.name(resourceTypeViewModel.getName())
			.type(resourceTypeViewModel.getType())
			.unit(resourceTypeViewModel.getUnit())
			.accessible(resourceTypeViewModel.isAccessible())
			.build();
	}
}
