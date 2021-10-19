/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_usage;

import io.imunity.furms.api.resource_usage.ResourceUsageService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.domain.authz.roles.Capability.SITE_READ;
import static io.imunity.furms.domain.authz.roles.ResourceType.SITE;

@Service
public class ResourceUsageServiceImpl implements ResourceUsageService {

	private final ResourceUsageRepository resourceUsageRepository;

	public ResourceUsageServiceImpl(ResourceUsageRepository resourceUsageRepository) {
		this.resourceUsageRepository = resourceUsageRepository;
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Set<UserResourceUsage> findAllUserUsages(String siteId,
	                                                Set<UUID> projectAllocations,
	                                                LocalDateTime from,
	                                                LocalDateTime to) {
		return resourceUsageRepository.findUserResourceUsages(projectAllocations, from, to);
	}

}