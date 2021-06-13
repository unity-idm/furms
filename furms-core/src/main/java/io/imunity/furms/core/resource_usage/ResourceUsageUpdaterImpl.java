/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_usage;

import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import io.imunity.furms.site.api.message_resolver.ResourceUsageUpdater;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ResourceUsageUpdaterImpl implements ResourceUsageUpdater {
	private final ResourceUsageRepository resourceUsageRepository;
	private final ProjectAllocationRepository projectAllocationRepository;

	ResourceUsageUpdaterImpl(ResourceUsageRepository resourceUsageRepository, ProjectAllocationRepository projectAllocationRepository) {
		this.resourceUsageRepository = resourceUsageRepository;
		this.projectAllocationRepository = projectAllocationRepository;
	}

	@Override
	@Transactional
	public void updateUsage(ResourceUsage usage) {
		ProjectAllocationResolved projectAllocationResolved = projectAllocationRepository.findByIdWithRelatedObjects(usage.projectAllocationId)
			.orElseThrow(() -> new IllegalArgumentException("Project Allocation doesn't exist: " + usage.projectAllocationId));
		resourceUsageRepository.create(usage, projectAllocationResolved);
	}

	@Override
	@Transactional
	public void updateUsage(UserResourceUsage usage) {
		resourceUsageRepository.create(usage);
	}
}
