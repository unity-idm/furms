/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_usage;

import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.resource_usage.ResourceUsageUpdatedEvent;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import io.imunity.furms.site.api.message_resolver.ResourceUsageUpdater;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ResourceUsageUpdaterImpl implements ResourceUsageUpdater {
	private final ResourceUsageRepository resourceUsageRepository;
	private final ProjectAllocationRepository projectAllocationRepository;
	private final ApplicationEventPublisher publisher;

	ResourceUsageUpdaterImpl(ResourceUsageRepository resourceUsageRepository, ProjectAllocationRepository projectAllocationRepository, ApplicationEventPublisher publisher) {
		this.resourceUsageRepository = resourceUsageRepository;
		this.projectAllocationRepository = projectAllocationRepository;
		this.publisher = publisher;
	}

	@Override
	@Transactional
	public void updateUsage(ResourceUsage usage) {
		ProjectAllocationResolved projectAllocationResolved = projectAllocationRepository.findByIdWithRelatedObjects(usage.projectAllocationId)
			.orElseThrow(() -> new IllegalArgumentException("Project Allocation doesn't exist: " + usage.projectAllocationId));
		resourceUsageRepository.create(usage, projectAllocationResolved);
		publisher.publishEvent(new ResourceUsageUpdatedEvent(projectAllocationResolved.amount, usage.cumulativeConsumption, usage.projectAllocationId));
	}

	@Override
	@Transactional
	public void updateUsage(UserResourceUsage usage) {
		resourceUsageRepository.create(usage);
	}
}
