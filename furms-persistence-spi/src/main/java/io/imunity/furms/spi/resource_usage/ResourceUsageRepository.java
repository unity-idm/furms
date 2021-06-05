/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.resource_usage;


import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;

import java.util.Optional;
import java.util.Set;

public interface ResourceUsageRepository {
	void create(ResourceUsage resourceUsage);
	void create(UserResourceUsage userResourceUsage);
	Set<ResourceUsage> findNewestResourceUsages(String projectId);
	Optional<ResourceUsage> findNewestResourceUsage(String projectAllocationId);
}
