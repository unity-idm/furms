/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_types;

import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.services.InfraServiceId;
import io.imunity.furms.spi.services.InfraServiceRepository;
import org.springframework.stereotype.Component;

@Component
public class ResourceTypeConverter {

	private final InfraServiceRepository infraServiceRepository;

	public ResourceTypeConverter(InfraServiceRepository infraServiceRepository) {
		this.infraServiceRepository = infraServiceRepository;
	}

	public ResourceType toResourceType(ResourceTypeEntity entity) {
		final ResourceType.ResourceTypeBuilder builder = ResourceType.builder()
				.id(entity.getId().toString())
				.siteId(entity.siteId.toString())
				.name(entity.name)
				.type(entity.type)
				.unit(entity.unit)
				.accessibleForAllProjectMembers(entity.accessible);
		infraServiceRepository.findById(new InfraServiceId(entity.serviceId))
				.ifPresent(infraService -> builder
						.serviceId(infraService.id)
						.serviceName(infraService.name));
		return builder.build();
	}
}
