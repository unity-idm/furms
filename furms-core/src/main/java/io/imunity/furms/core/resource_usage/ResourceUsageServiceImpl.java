/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_usage;

import io.imunity.furms.api.resource_usage.ResourceUsageService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.domain.authz.roles.Capability.RESOURCE_USAGE_READ;
import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;
import static io.imunity.furms.domain.authz.roles.ResourceType.SITE;

@Service
class ResourceUsageServiceImpl implements ResourceUsageService {

	private final ResourceUsageRepository resourceUsageRepository;
	private final CommunityAllocationRepository communityAllocationRepository;
	private final ProjectAllocationRepository projectAllocationRepository;

	ResourceUsageServiceImpl(ResourceUsageRepository resourceUsageRepository, ProjectAllocationRepository projectAllocationRepository,
	                         CommunityAllocationRepository communityAllocationRepository) {
		this.resourceUsageRepository = resourceUsageRepository;
		this.communityAllocationRepository = communityAllocationRepository;
		this.projectAllocationRepository = projectAllocationRepository;
	}

	@Override
	@FurmsAuthorize(capability = RESOURCE_USAGE_READ, resourceType = SITE, id = "siteId")
	public Set<UserResourceUsage> findAllUserUsages(String siteId,
	                                                Set<UUID> projectAllocations,
	                                                LocalDateTime from,
	                                                LocalDateTime to) {
		return resourceUsageRepository.findUserResourceUsages(projectAllocations, from, to);
	}

	@Override
	@FurmsAuthorize(capability = RESOURCE_USAGE_READ, resourceType = PROJECT, id = "projectId")
	public Set<ResourceUsage> findAllResourceUsageHistory(String projectId, String projectAllocationId) {
		validProjectAndAllocationAreRelated(projectId, projectAllocationId);
		return resourceUsageRepository.findResourceUsagesHistory(UUID.fromString(projectAllocationId));
	}

	@Override
	@FurmsAuthorize(capability = RESOURCE_USAGE_READ, resourceType = PROJECT, id = "projectId")
	public Set<UserResourceUsage> findAllUserUsagesHistory(String projectId, String projectAllocationId) {
		validProjectAndAllocationAreRelated(projectId, projectAllocationId);
		return resourceUsageRepository.findUserResourceUsagesHistory(UUID.fromString(projectAllocationId));
	}

	@Override
	@FurmsAuthorize(capability = RESOURCE_USAGE_READ, resourceType = COMMUNITY, id = "communityId")
	public Set<ResourceUsage> findAllResourceUsageHistoryByCommunity(String communityId, String communityAllocationId) {
		communityAllocationRepository.findById(communityAllocationId)
			.filter(allocation -> allocation.communityId.equals(communityId))
			.orElseThrow(() -> new IllegalArgumentException(String.format(
				"Community id %s and community allocation id %s are not related", communityId, communityAllocationId)
			));
		return resourceUsageRepository.findResourceUsagesHistoryByCommunityAllocationId(UUID.fromString(communityAllocationId));
	}

	private void validProjectAndAllocationAreRelated(String projectId, String projectAllocationId) {
		projectAllocationRepository.findById(projectAllocationId)
			.filter(allocation -> allocation.projectId.equals(projectId))
			.orElseThrow(() -> new IllegalArgumentException(String.format(
				"Project id %s and project allocation id %s are not related", projectId, projectAllocationId)
			));
	}
}
