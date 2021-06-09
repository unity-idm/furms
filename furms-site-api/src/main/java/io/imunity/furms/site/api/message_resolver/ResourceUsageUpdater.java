/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api.message_resolver;

import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;

public interface ResourceUsageUpdater {
	void updateUsage(ResourceUsage usage);
	void updateUsage(UserResourceUsage usage);
}
