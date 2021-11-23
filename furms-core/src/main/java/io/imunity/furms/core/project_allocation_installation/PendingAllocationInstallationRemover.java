/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation_installation;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.site.api.message_remover.ProjectAllocationInstallationRemover;
import io.imunity.furms.spi.project_allocation_installation.ProjectAllocationInstallationRepository;
import org.springframework.stereotype.Component;

@Component
class PendingAllocationInstallationRemover implements ProjectAllocationInstallationRemover {
	private final ProjectAllocationInstallationRepository projectAllocationInstallationRepository;

	PendingAllocationInstallationRemover(ProjectAllocationInstallationRepository projectAllocationInstallationRepository) {
		this.projectAllocationInstallationRepository = projectAllocationInstallationRepository;
	}

	@Override
	public void remove(CorrelationId correlationId) {
		projectAllocationInstallationRepository.delete(correlationId);
	}
}
