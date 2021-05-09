/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.resource_types;

import java.util.Optional;
import java.util.Set;

import io.imunity.furms.domain.resource_types.ResourceType;

public interface ResourceTypeService {
	
	Optional<ResourceType> findById(String id, String siteId);

	Set<ResourceType> findAll(String siteId);

	Set<ResourceType> findAll();

	void create(ResourceType resourceType);

	void update(ResourceType resourceType);

	void delete(String id, String siteId);
}
