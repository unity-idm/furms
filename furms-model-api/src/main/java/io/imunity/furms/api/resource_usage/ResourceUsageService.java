/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.api.resource_usage;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import io.imunity.furms.domain.sites.SiteId;

import java.time.LocalDateTime;
import java.util.Set;

public interface ResourceUsageService {

	Set<UserResourceUsage> findAllUserUsages(SiteId siteId, Set<ProjectAllocationId> projectAllocations, LocalDateTime from, LocalDateTime to);

	Set<UserResourceUsage> findAllUserUsagesHistory(ProjectId projectId, ProjectAllocationId projectAllocationId);

	Set<ResourceUsage> findAllResourceUsageHistory(ProjectId projectId, ProjectAllocationId projectAllocationId);

	Set<ResourceUsage> findAllResourceUsageHistoryByCommunity(CommunityId communityId, CommunityAllocationId communityAllocationId);

}