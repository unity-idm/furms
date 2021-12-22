/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.core.project_allocation_installation.ProjectAllocationInstallationService;
import io.imunity.furms.domain.project_allocation_installation.ErrorMessage;
import io.imunity.furms.domain.project_installation.ProjectInstallationJob;
import io.imunity.furms.domain.project_installation.ProjectInstallationResult;
import io.imunity.furms.domain.project_installation.ProjectInstallationStatus;
import io.imunity.furms.domain.project_installation.ProjectUpdateJob;
import io.imunity.furms.domain.project_installation.ProjectUpdateResult;
import io.imunity.furms.domain.project_installation.ProjectUpdateStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent.IllegalCorrelationIdException;
import io.imunity.furms.domain.site_agent.IllegalTransitStateException;
import io.imunity.furms.site.api.status_updater.ProjectInstallationStatusUpdater;
import io.imunity.furms.spi.project_installation.ProjectOperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;

@Service
class ProjectInstallationStatusUpdaterImpl implements ProjectInstallationStatusUpdater {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ProjectOperationRepository projectOperationRepository;
	private final ProjectAllocationInstallationService projectAllocationInstallationService;

	ProjectInstallationStatusUpdaterImpl(
		ProjectOperationRepository projectOperationRepository,
		ProjectAllocationInstallationService projectAllocationInstallationService) {
		this.projectOperationRepository = projectOperationRepository;
		this.projectAllocationInstallationService = projectAllocationInstallationService;
	}

	@Override
	@Transactional
	public void update(CorrelationId correlationId, ProjectInstallationResult result) {
		ProjectInstallationJob job = projectOperationRepository.findInstallationJobByCorrelationId(correlationId)
			.orElseThrow(() -> new IllegalCorrelationIdException("Correlation Id not found: " + correlationId));
		if(job.status.equals(ProjectInstallationStatus.INSTALLED) || job.status.equals(ProjectInstallationStatus.FAILED)){
			throw new IllegalTransitStateException(String.format("ProjectInstallation status is %s, it cannot be modified", job.status));
		}

		projectOperationRepository.update(job.id, result);
		if(result.status.equals(ProjectInstallationStatus.INSTALLED)){
			projectAllocationInstallationService.startWaitingAllocations(job.projectId, job.siteId);
		}
		if(result.status.equals(ProjectInstallationStatus.FAILED)){
			projectAllocationInstallationService.cancelWaitingAllocations(job.projectId, new ErrorMessage(result.error.code, result.error.message));
		}
		LOG.info("ProjectInstallation status with given id {} was updated to {}", job.id, result.status);
	}

	@Override
	@Transactional
	public void update(CorrelationId correlationId, ProjectUpdateResult result) {
		ProjectUpdateJob job = projectOperationRepository.findUpdateJobByCorrelationId(correlationId)
			.orElseThrow(() -> new IllegalCorrelationIdException("Correlation Id not found: " + correlationId));
		if(job.status.equals(ProjectUpdateStatus.UPDATED) || job.status.equals(ProjectUpdateStatus.FAILED)){
			throw new IllegalTransitStateException(String.format("ProjectUpdate status is %s, it cannot be modified", job.status));
		}

		projectOperationRepository.update(job.id, result);
		LOG.info("ProjectUpdate status with given id {} was updated to {}", job.id, result.status);
	}
}
