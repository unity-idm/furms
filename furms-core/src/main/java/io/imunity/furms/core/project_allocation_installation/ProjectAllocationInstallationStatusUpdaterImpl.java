/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation_installation;

import io.imunity.furms.domain.project_allocation_installation.*;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.site.api.message_resolver.ProjectAllocationInstallationStatusUpdater;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.project_allocation_installation.ProjectAllocationInstallationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

@Service
class ProjectAllocationInstallationStatusUpdaterImpl implements ProjectAllocationInstallationStatusUpdater {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ProjectAllocationInstallationRepository projectAllocationInstallationRepository;
	private final ProjectAllocationRepository projectAllocationRepository;

	ProjectAllocationInstallationStatusUpdaterImpl(
		ProjectAllocationInstallationRepository projectAllocationInstallationRepository,
		ProjectAllocationRepository projectAllocationRepository
	) {
		this.projectAllocationInstallationRepository = projectAllocationInstallationRepository;
		this.projectAllocationRepository = projectAllocationRepository;
	}

	@Override
	@Transactional
	public void updateStatus(CorrelationId correlationId, ProjectAllocationInstallationStatus status, Optional<ErrorMessage> errorMessage) {
		ProjectAllocationInstallation job = projectAllocationInstallationRepository.findByCorrelationId(correlationId)
			.orElseThrow(() -> new IllegalArgumentException("Correlation Id not found: " + correlationId));
		if(job.status.equals(ProjectAllocationInstallationStatus.INSTALLED) || job.status.equals(ProjectAllocationInstallationStatus.FAILED)) {
			LOG.info("ProjectAllocationInstallation with given correlation id {} cannot be modified", correlationId.id);
			return;
		}
		projectAllocationInstallationRepository.update(correlationId.id, status, errorMessage);
		LOG.info("ProjectAllocationInstallation status with given correlation id {} was updated to {}", correlationId.id, status);
	}

	@Override
	@Transactional
	public void updateStatus(CorrelationId correlationId, ProjectDeallocationStatus status, Optional<ErrorMessage> errorMessage) {
		ProjectDeallocation projectDeallocation = projectAllocationInstallationRepository.findDeallocationByCorrelationId(correlationId.id);
		if(status.equals(ProjectDeallocationStatus.ACKNOWLEDGED)){
			projectAllocationRepository.delete(projectDeallocation.projectAllocationId);
			return;
		}
		if(projectDeallocation.status.equals(ProjectDeallocationStatus.FAILED)) {
			LOG.info("ProjectDeallocationInstallation with given correlation id {} cannot be modified", correlationId.id);
			return;
		}
		projectAllocationInstallationRepository.update(correlationId.id, status, errorMessage);
		LOG.info("ProjectDeallocationInstallation status with given correlation id {} was updated to {}", correlationId.id, status);
	}

	@Override
	@Transactional
	public void updateStatus(ProjectAllocationInstallation result) {
		if(result.status.equals(ProjectAllocationInstallationStatus.FAILED)){
			LOG.info("ProjectInstallationAllocation with given correlation id {} failed. It's not supported", result.correlationId.id);
			return;
		}
		Optional<ProjectAllocationInstallation> projectInstallation = projectAllocationInstallationRepository.findByCorrelationId(result.correlationId);
		if(projectInstallation.isPresent() && projectInstallation.get().status.isInstalling()){
			projectAllocationInstallationRepository.update(result);
			LOG.info("ProjectAllocationInstallation status with given id {} was updated to {}", projectInstallation.get().id, result.status);
			return;
		}
		projectAllocationInstallationRepository.create(result);
		LOG.info("ProjectAllocationInstallation was updated: {}", result);
	}
}
