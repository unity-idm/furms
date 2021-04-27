/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.domain.project_installation.ProjectInstallationJob;
import io.imunity.furms.domain.project_installation.ProjectInstallationStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.site.api.message_resolver.ProjectInstallationMessageResolver;
import io.imunity.furms.spi.project_installation.ProjectInstallationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

@Service
class ProjectInstallationMessageResolverImpl implements ProjectInstallationMessageResolver  {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ProjectInstallationRepository projectInstallationRepository;

	ProjectInstallationMessageResolverImpl(ProjectInstallationRepository projectInstallationRepository) {
		this.projectInstallationRepository = projectInstallationRepository;
	}

	@Override
	public void updateStatus(CorrelationId correlationId, ProjectInstallationStatus status) {
		ProjectInstallationJob job = projectInstallationRepository.findByCorrelationId(correlationId);
		projectInstallationRepository.update(job.id, status);
		LOG.info("ProjectInstallation status with given id {} was updated to {}", job.id, status);
	}
}
