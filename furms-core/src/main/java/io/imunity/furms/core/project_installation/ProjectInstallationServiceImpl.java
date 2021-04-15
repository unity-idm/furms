/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.api.project_installation.ProjectInstallationService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.project_installation.ProjectInstallationJob;
import io.imunity.furms.domain.project_installation.ProjectInstallationStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.site.api.message_resolver.ProjectInstallationMessageResolver;
import io.imunity.furms.spi.project_installation.ProjectInstallationRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_READ;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;

@Service
class ProjectInstallationServiceImpl implements ProjectInstallationService, ProjectInstallationMessageResolver {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ProjectInstallationRepository projectInstallationRepository;
	private final UsersDAO usersDAO;

	ProjectInstallationServiceImpl(ProjectInstallationRepository projectInstallationRepository, UsersDAO usersDAO) {
		this.projectInstallationRepository = projectInstallationRepository;
		this.usersDAO = usersDAO;
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT)
	public ProjectInstallation findProjectInstallation(String projectAllocationId) {
		return projectInstallationRepository.findProjectInstallation(projectAllocationId, usersDAO::findById);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_WRITE, resourceType = PROJECT, id = "projectInstallationJob.projectId")
	public void create(ProjectInstallationJob projectInstallationJob) {
		projectInstallationRepository.create(projectInstallationJob);
		LOG.info("ProjectInstallation was updated: {}", projectInstallationJob);
	}

	@Override
	//FIXME To auth this method special user for queue message resolving is needed
	public void updateStatus(CorrelationId correlationId, ProjectInstallationStatus status) {
		ProjectInstallationJob job = projectInstallationRepository.findByCorrelationId(correlationId);
		projectInstallationRepository.update(job.id, status);
		LOG.info("ProjectInstallation status with given id {} was updated to {}", job.id, status);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_WRITE, resourceType = PROJECT, id = "projectId")
	public void delete(String projectId, String id) {
		projectInstallationRepository.delete(id);
		LOG.info("ProjectInstallation with given ID {} was deleted", id);
	}
}
