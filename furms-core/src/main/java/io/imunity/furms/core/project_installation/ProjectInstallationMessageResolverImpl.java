/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.domain.project_installation.*;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.site.api.message_resolver.ProjectInstallationMessageResolver;
import io.imunity.furms.spi.project_installation.ProjectOperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

@Service
class ProjectInstallationMessageResolverImpl implements ProjectInstallationMessageResolver  {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ProjectOperationRepository projectOperationRepository;

	ProjectInstallationMessageResolverImpl(ProjectOperationRepository projectOperationRepository) {
		this.projectOperationRepository = projectOperationRepository;
	}

	@Override
	public void update(CorrelationId correlationId, ProjectInstallationResult result) {
		ProjectInstallationJob job = projectOperationRepository.findInstallationJobByCorrelationId(correlationId);
		projectOperationRepository.update(job.id, result.status);
		LOG.info("ProjectInstallation status with given id {} was updated to {}", job.id, result.status);
	}

	@Override
	public void update(CorrelationId correlationId, ProjectUpdateResult result) {
		ProjectUpdateJob job = projectOperationRepository.findUpdateJobByCorrelationId(correlationId);
		projectOperationRepository.update(job.id, result.status);
		LOG.info("ProjectUpdate status with given id {} was updated to {}", job.id, result.status);
	}
}
