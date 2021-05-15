/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api.message_resolver;

import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;

public interface ProjectAllocationInstallationMessageResolver {
	void updateStatus(CorrelationId correlationId, ProjectAllocationInstallationStatus status, String message);
	void updateStatus(CorrelationId correlationId, ProjectDeallocationStatus status, String message);
	void updateStatus(ProjectAllocationInstallation result);
}
