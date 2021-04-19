/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation_installation;

import io.imunity.furms.api.project_allocation_installation.ProjectAllocationInstallationService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.site.api.message_resolver.ProjectAllocationMessageResolver;
import io.imunity.furms.spi.project_allocation_installation.ProjectAllocationInstallationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;

@Service
class ProjectAllocationInstallationServiceImpl implements ProjectAllocationInstallationService, ProjectAllocationMessageResolver {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ProjectAllocationInstallationRepository projectAllocationInstallationRepository;

	ProjectAllocationInstallationServiceImpl(ProjectAllocationInstallationRepository projectAllocationInstallationRepository) {
		this.projectAllocationInstallationRepository = projectAllocationInstallationRepository;
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
	public Set<ProjectAllocationInstallation> findAll(String communityId, String projectId) {
		return projectAllocationInstallationRepository.findAll(projectId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
	public void create(String communityId, ProjectAllocationInstallation projectAllocationInstallation) {
		projectAllocationInstallationRepository.create(projectAllocationInstallation);
		LOG.info("ProjectInstallation was updated: {}", projectAllocationInstallation);
	}

	@Override
	//FIXME To auth this method special user for queue message resolving is needed
	public void updateStatus(CorrelationId correlationId, ProjectAllocationInstallationStatus status) {
		projectAllocationInstallationRepository.findByCorrelationId(correlationId).ifPresent(job -> {
			projectAllocationInstallationRepository.update(job.id, status);
			LOG.info("ProjectInstallation status with given id {} was updated to {}", job.id, status);
		});
	}

	@Override
	//FIXME To auth this method special user for queue message resolving is needed
	public void updateStatus(ProjectAllocationInstallation result) {
		projectAllocationInstallationRepository.findByCorrelationId(new CorrelationId(result.correlationId)).ifPresentOrElse(job -> {
			projectAllocationInstallationRepository.update(job.id, result.status);
			LOG.info("ProjectInstallation status with given id {} was updated to {}", job.id, result.status);
		}, () -> {
			projectAllocationInstallationRepository.create(result);
			LOG.info("ProjectInstallation was updated: {}", result);
		});
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
	public void delete(String communityId, String id) {
		projectAllocationInstallationRepository.delete(id);
		LOG.info("ProjectInstallation with given ID {} was deleted", id);
	}
}
