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
			.accessible(resourceType.accessible)
			.build();
	}

	static ResourceType map(ResourceTypeViewModel resourceTypeViewModel){
		return ResourceType.builder()
			.id(resourceTypeViewModel.id)
			.siteId(resourceTypeViewModel.siteId)
			.serviceId(resourceTypeViewModel.serviceId)
			.name(resourceTypeViewModel.name)
			.type(resourceTypeViewModel.type)
			.unit(resourceTypeViewModel.unit)
			.accessible(resourceTypeViewModel.accessible)
			.build();
	}
}
