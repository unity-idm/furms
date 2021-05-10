/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.resource_type;


import io.imunity.furms.domain.resource_types.ResourceType;

import java.util.Optional;
import java.util.Set;

public interface ResourceTypeRepository {
	Optional<ResourceType> findById(String id);

	Set<ResourceType> findAllBySiteId(String siteId);

	Set<ResourceType> findAllByInfraServiceId(String serviceId);

	Set<ResourceType> findAll();

	String create(ResourceType resourceType);

	String update(ResourceType resourceType);

	boolean exists(String id);

	boolean isNamePresent(String name, String siteId);

	void delete(String id);

	void deleteAll();
}
