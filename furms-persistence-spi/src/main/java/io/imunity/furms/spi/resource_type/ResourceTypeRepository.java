/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.resource_type;


import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.domain.services.InfraServiceId;
import io.imunity.furms.domain.sites.SiteId;

import java.util.Optional;
import java.util.Set;

public interface ResourceTypeRepository {
	Optional<ResourceType> findById(ResourceTypeId id);

	Set<ResourceType> findAllBySiteId(SiteId siteId);

	Set<ResourceType> findAllByInfraServiceId(InfraServiceId serviceId);

	Set<ResourceType> findAll();

	String create(ResourceType resourceType);

	void update(ResourceType resourceType);

	boolean exists(ResourceTypeId id);

	boolean isNamePresent(String name, SiteId siteId);

	void delete(ResourceTypeId id);

	void deleteAll();
}
