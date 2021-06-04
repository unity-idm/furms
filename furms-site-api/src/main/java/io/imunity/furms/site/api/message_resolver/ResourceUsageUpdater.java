/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api.message_resolver;

import io.imunity.furms.domain.resource_usage.ProjectAllocationUsage;
import io.imunity.furms.domain.resource_usage.UserProjectAllocationUsage;

public interface ResourceUsageUpdater {
	void updateUsage(ProjectAllocationUsage usage);
	void updateUsage(UserProjectAllocationUsage usage);
}
