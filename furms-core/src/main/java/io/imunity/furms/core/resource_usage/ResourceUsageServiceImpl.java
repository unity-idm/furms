/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_usage;

import io.imunity.furms.api.resource_usage.ResourceUsageService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_READ;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_LIMITED_READ;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_READ;
import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;
import static io.imunity.furms.domain.authz.roles.ResourceType.SITE;

@Service
class ResourceUsageServiceImpl implements ResourceUsageService {

	private final ResourceUsageRepository resourceUsageRepository;
	private final ProjectAllocationRepository projectAllocationRepository;

	ResourceUsageServiceImpl(ResourceUsageRepository resourceUsageRepository, ProjectAllocationRepository projectAllocationRepository) {
		this.resourceUsageRepository = resourceUsageRepository;
		this.projectAllocationRepository = projectAllocationRepository;
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Set<UserResourceUsage> findAllUserUsages(String siteId,
	                                                Set<UUID> projectAllocations,
	                                                LocalDateTime from,
	                                                LocalDateTime to) {
		return resourceUsageRepository.findUserResourceUsages(projectAllocations, from, to);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id = "projectId")
	public Set<ResourceUsage> findAllResourceUsageHistory(String projectId, String projectAllocationId) {
		projectAllocationRepository.findById(projectAllocationId)
			.filter(allocation -> allocation.projectId.equals(projectId))
			.orElseThrow(() -> new IllegalArgumentException(String.format(
				"Project id %s and project allocation id %s are not related", projectId, projectAllocationId)
			));
		return resourceUsageRepository.findResourceUsagesHistory(UUID.fromString(projectAllocationId));
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id = "projectId")
	public Set<UserResourceUsage> findAllUserUsagesHistory(String projectId, String projectAllocations) {
		return resourceUsageRepository.findUserResourceUsagesHistory(UUID.fromString(projectAllocations));
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Set<ResourceUsage> findAllResourceUsageHistoryByCommunity(String communityId, String communityAllocationId) {
		return resourceUsageRepository.findResourceUsagesHistoryByCommunityAllocationId(UUID.fromString(communityAllocationId));
	}

}
