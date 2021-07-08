/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation_installation;

import io.imunity.furms.domain.project_allocation_installation.ErrorMessage;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.site.api.status_updater.ProjectAllocationInstallationStatusUpdater;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.project_allocation_installation.ProjectAllocationInstallationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

import static io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus.ACKNOWLEDGED;
import static io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus.FAILED;
import static io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus.UPDATING;
import static io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus.UPDATING_FAILED;

@Service
class ProjectAllocationInstallationStatusUpdaterImpl implements ProjectAllocationInstallationStatusUpdater {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ProjectAllocationInstallationRepository projectAllocationInstallationRepository;
	private final ProjectAllocationRepository projectAllocationRepository;

	ProjectAllocationInstallationStatusUpdaterImpl(ProjectAllocationInstallationRepository projectAllocationInstallationRepository,
	                                               ProjectAllocationRepository projectAllocationRepository) {
		this.projectAllocationInstallationRepository = projectAllocationInstallationRepository;
		this.projectAllocationRepository = projectAllocationRepository;
	}

	@Override
	@Transactional
	public void updateStatus(CorrelationId correlationId, ProjectAllocationInstallationStatus status, Optional<ErrorMessage> errorMessage) {
		ProjectAllocationInstallation job = projectAllocationInstallationRepository.findByCorrelationId(correlationId)
			.orElseThrow(() -> new IllegalArgumentException("Correlation Id not found: " + correlationId));
		if(job.status.isTerminal()) {
			LOG.info("ProjectAllocationInstallation with given correlation id {} cannot be modified", correlationId.id);
			return;
		}

		if(isTransitionFromUpdatingToFailedStatus(job.status, status))
			projectAllocationInstallationRepository.update(correlationId.id, UPDATING_FAILED, errorMessage);
		else
			projectAllocationInstallationRepository.update(correlationId.id, status, errorMessage);

		LOG.info("ProjectAllocationInstallation status with given correlation id {} was updated to {}", correlationId.id, status);
	}

	private boolean isTransitionFromUpdatingToFailedStatus(ProjectAllocationInstallationStatus oldStatus, ProjectAllocationInstallationStatus newStatus) {
		return oldStatus == UPDATING && newStatus == FAILED;
	}

	@Override
	@Transactional
	public void updateStatus(CorrelationId correlationId, ProjectDeallocationStatus status, Optional<ErrorMessage> errorMessage) {
		ProjectDeallocation projectDeallocation = projectAllocationInstallationRepository.findDeallocationByCorrelationId(correlationId.id);
		if(status.equals(ProjectDeallocationStatus.ACKNOWLEDGED)){
			projectAllocationRepository.deleteById(projectDeallocation.projectAllocationId);
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
	public void createChunk(ProjectAllocationChunk result) {
		ProjectAllocationInstallation allocationInstallation = projectAllocationInstallationRepository.findByProjectAllocationId(result.projectAllocationId);
		if(!allocationInstallation.status.equals(ACKNOWLEDGED))
			throw new IllegalArgumentException(String.format(
				"Protocol error, only acknowledged allocation get add chunk - project allocation %s status is %s",
				allocationInstallation.projectAllocationId,
				allocationInstallation.status)
			);
		projectAllocationInstallationRepository.create(result);
		LOG.info("ProjectAllocationChunk was created: {}", result);
	}

	@Override
	@Transactional
	public void updateChunk(ProjectAllocationChunk result) {
		projectAllocationInstallationRepository.update(result);
		LOG.info("ProjectAllocationChunk was updated: {}", result);
	}
}
