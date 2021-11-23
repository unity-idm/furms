/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.site.api.message_remover.ProjectInstallationRemover;
import io.imunity.furms.spi.project_installation.ProjectOperationRepository;
import org.springframework.stereotype.Component;

@Component
class PendingProjectInstallationRemover implements ProjectInstallationRemover {
	private final ProjectOperationRepository projectOperationRepository;

	PendingProjectInstallationRemover(ProjectOperationRepository projectOperationRepository) {
		this.projectOperationRepository = projectOperationRepository;
	}

	@Override
	public void remove(CorrelationId correlationId) {
		projectOperationRepository.delete(correlationId);
	}
}
