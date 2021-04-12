/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.project_installation.ProjectInstallationJob;
import io.imunity.furms.site.api.ProjectInstallationService;
import io.imunity.furms.spi.project_installation.ProjectInstallationRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

@Service
class ProjectInstallationServiceImpl implements ProjectInstallationService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ProjectInstallationRepository projectInstallationRepository;
	private final UsersDAO usersDAO;

	ProjectInstallationServiceImpl(ProjectInstallationRepository projectInstallationRepository, UsersDAO usersDAO) {
		this.projectInstallationRepository = projectInstallationRepository;
		this.usersDAO = usersDAO;
	}

	@Override
	public ProjectInstallation findProjectInstallation(String projectAllocationId) {
		return projectInstallationRepository.findProjectInstallation(projectAllocationId, usersDAO::findById);
	}

	@Override
	public void create(ProjectInstallationJob projectInstallationJob) {
		projectInstallationRepository.create(projectInstallationJob);
		LOG.info("ProjectInstallation was updated: {}", projectInstallationJob);
	}

	@Override
	public void update(ProjectInstallationJob updateJob) {
		ProjectInstallationJob job = projectInstallationRepository.findByCorrelationId(updateJob.correlationId);
		ProjectInstallationJob projectInstallationJob = new ProjectInstallationJob(job.id, job.correlationId, updateJob.status);
		projectInstallationRepository.update(projectInstallationJob);
		LOG.info("ProjectInstallation was updated: {}", projectInstallationJob);
	}

	@Override
	public void delete(String id) {
		projectInstallationRepository.delete(id);
		LOG.info("ProjectInstallation with given ID {} was deleted", id);
	}
}
