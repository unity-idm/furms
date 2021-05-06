/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation_installation;

import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.site.api.message_resolver.ProjectAllocationInstallationMessageResolver;
import io.imunity.furms.spi.project_allocation_installation.ProjectAllocationInstallationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;

@Service
class ProjectAllocationInstallationMessageResolverImpl implements ProjectAllocationInstallationMessageResolver {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ProjectAllocationInstallationRepository projectAllocationInstallationRepository;

	ProjectAllocationInstallationMessageResolverImpl(ProjectAllocationInstallationRepository projectAllocationInstallationRepository) {
		this.projectAllocationInstallationRepository = projectAllocationInstallationRepository;
	}

	@Override
	@Transactional
	public void updateStatus(CorrelationId correlationId, ProjectAllocationInstallationStatus status) {
		if(status.equals(ProjectAllocationInstallationStatus.FAILED)){
			LOG.info("ProjectInstallationAllocation with given correlation id {} failed. It's not supported", correlationId.id);
			return;
		}
		projectAllocationInstallationRepository.findByCorrelationId(correlationId);
		projectAllocationInstallationRepository.update(correlationId.id, status);
		LOG.info("ProjectAllocationInstallation status with given correlation id {} was updated to {}", correlationId.id, status);
	}

	@Override
	@Transactional
	public void updateStatus(CorrelationId correlationId, ProjectDeallocationStatus status) {
		if(status.equals(ProjectDeallocationStatus.FAILED)){
			LOG.info("ProjectDeallocation with given correlation id {} failed. It's not supported", correlationId.id);
			return;
		}
		projectAllocationInstallationRepository.update(correlationId.id, status);
		LOG.info("ProjectAllocationInstallation status with given correlation id {} was updated to {}", correlationId.id, status);
	}

	@Override
	@Transactional
	public void updateStatus(ProjectAllocationInstallation result) {
		if(result.status.equals(ProjectAllocationInstallationStatus.FAILED)){
			LOG.info("ProjectInstallationAllocation with given correlation id {} failed. It's not supported", result.correlationId.id);
			return;
		}
		projectAllocationInstallationRepository.findByCorrelationId(result.correlationId).ifPresentOrElse(job -> {
			projectAllocationInstallationRepository.update(result);
			LOG.info("ProjectAllocationInstallation status with given id {} was updated to {}", job.id, result.status);
		}, () -> {
			projectAllocationInstallationRepository.create(result);
			LOG.info("ProjectAllocationInstallation was updated: {}", result);
		});
	}
}
