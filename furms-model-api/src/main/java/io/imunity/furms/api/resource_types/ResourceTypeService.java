/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.resource_types;

import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.domain.sites.SiteId;

import java.util.Optional;
import java.util.Set;

public interface ResourceTypeService {
	
	Optional<ResourceType> findById(ResourceTypeId id, SiteId siteId);

	Set<ResourceType> findAll(SiteId siteId);

	Set<ResourceType> findAll();

	void create(ResourceType resourceType);

	void update(ResourceType resourceType);

	void delete(ResourceTypeId id, SiteId siteId);
}
