/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.api.resource_usage;

import io.imunity.furms.domain.resource_usage.UserResourceUsage;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public interface ResourceUsageService {

	Set<UserResourceUsage> findAllUserUsages(String siteId, Set<UUID> projectAllocations, LocalDateTime from, LocalDateTime to);

}