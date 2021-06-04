/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_usage;

import io.imunity.furms.domain.resource_usage.ProjectAllocationUsage;
import io.imunity.furms.domain.resource_usage.UserProjectAllocationUsage;
import io.imunity.furms.site.api.message_resolver.ResourceUsageUpdater;
import org.springframework.stereotype.Service;

@Service
class ResourceUsageUpdaterImpl implements ResourceUsageUpdater {
	@Override
	public void updateUsage(ProjectAllocationUsage usage) {

	}

	@Override
	public void updateUsage(UserProjectAllocationUsage usage) {

	}
}
