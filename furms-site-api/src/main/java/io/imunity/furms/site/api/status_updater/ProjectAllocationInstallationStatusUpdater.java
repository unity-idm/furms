/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api.status_updater;

import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.project_allocation_installation.*;
import io.imunity.furms.domain.site_agent.CorrelationId;

import java.util.Optional;

public interface ProjectAllocationInstallationStatusUpdater {
	boolean isWaitingForInstallationConfirmation(ProjectAllocationId projectAllocationId);

	CorrelationId getCorrelationId(ProjectAllocationId projectAllocationId);

	void updateStatus(ProjectAllocationId projectAllocationId, ProjectAllocationInstallationStatus status,
	                  Optional<ErrorMessage> errorMessage);
	void updateStatus(CorrelationId correlationId, ProjectAllocationInstallationStatus status, Optional<ErrorMessage> errorMessage);

	void updateStatusToAcknowledged(CorrelationId correlationId);
	void updateStatus(CorrelationId correlationId, ProjectDeallocationStatus status, Optional<ErrorMessage> errorMessage);
	void createChunk(ProjectAllocationChunk chunk);
	void updateChunk(ProjectAllocationChunk chunk);
}
