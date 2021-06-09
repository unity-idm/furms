/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_usage;

import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import io.imunity.furms.site.api.message_resolver.ResourceUsageUpdater;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ResourceUsageUpdaterImpl implements ResourceUsageUpdater {
	private final ResourceUsageRepository resourceUsageRepository;

	ResourceUsageUpdaterImpl(ResourceUsageRepository resourceUsageRepository) {
		this.resourceUsageRepository = resourceUsageRepository;
	}

	@Override
	@Transactional
	public void updateUsage(ResourceUsage usage) {
		resourceUsageRepository.create(usage);
	}

	@Override
	@Transactional
	public void updateUsage(UserResourceUsage usage) {
		resourceUsageRepository.create(usage);
	}
}
