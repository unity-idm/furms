/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api.status_updater;

import io.imunity.furms.domain.resource_access.AccessStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;

public interface UserAllocationStatusUpdater {
	void update(CorrelationId correlationId, AccessStatus status, String msg);
}
