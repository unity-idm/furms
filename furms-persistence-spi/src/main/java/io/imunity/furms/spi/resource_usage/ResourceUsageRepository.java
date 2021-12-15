/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.resource_usage;


import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.resource_usage.ResourceUsageByCommunityAllocation;
import io.imunity.furms.domain.resource_usage.ResourceUsageByCredit;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ResourceUsageRepository {
	void create(ResourceUsage resourceUsage, ProjectAllocationResolved projectAllocationResolved);
	void create(UserResourceUsage userResourceUsage);
	Set<ResourceUsage> findCurrentResourceUsages(String projectId);
	Optional<ResourceUsage> findCurrentResourceUsage(String projectAllocationId);
	ResourceUsageByCredit findResourceUsagesSumsBySiteId(String siteId);
	ResourceUsageByCommunityAllocation findResourceUsagesSumsByCommunityId(String communityId);
	Set<UserResourceUsage> findUserResourceUsages(Set<UUID> projectAllocations, LocalDateTime from, LocalDateTime to);
	Set<ResourceUsage> findResourceUsagesHistory(UUID projectAllocationId);
	Set<ResourceUsage> findResourceUsagesHistoryByCommunityAllocationId(UUID communityAllocationId);
}
