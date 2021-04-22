/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api.site_agent;

import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.site_agent.CorrelationId;

public interface SiteAgentProjectAllocationInstallationService {
	void allocateProject(CorrelationId correlationId, ProjectAllocationResolved projectAllocation);
}
