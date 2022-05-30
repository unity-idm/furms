/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.resource_usage;


import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.resource_usage.ResourceUsageByCommunityAllocation;
import io.imunity.furms.domain.resource_usage.ResourceUsageByCredit;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import io.imunity.furms.domain.sites.SiteId;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

public interface ResourceUsageRepository {
	void create(ResourceUsage resourceUsage, ProjectAllocationResolved projectAllocationResolved);
	void create(UserResourceUsage userResourceUsage);
	Set<ResourceUsage> findCurrentResourceUsages(ProjectId projectId);
	Optional<ResourceUsage> findCurrentResourceUsage(ProjectAllocationId projectAllocationId);
	ResourceUsageByCredit findResourceUsagesSumsBySiteId(SiteId siteId);
	ResourceUsageByCommunityAllocation findResourceUsagesSumsByCommunityId(CommunityId communityId);
	Set<UserResourceUsage> findUserResourceUsages(Set<ProjectAllocationId> projectAllocations, LocalDateTime from,
	                                              LocalDateTime to);
	Set<UserResourceUsage> findUserResourceUsagesHistory(ProjectAllocationId projectAllocationId);
	Set<ResourceUsage> findResourceUsagesHistory(ProjectAllocationId projectAllocationId);
	Set<ResourceUsage> findResourceUsagesHistoryByCommunityAllocationId(CommunityAllocationId communityAllocationId);
}
