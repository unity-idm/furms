/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.api.project_installation.ProjectInstallationStatusService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.project_installation.ProjectInstallationJobStatus;
import io.imunity.furms.domain.project_installation.ProjectUpdateJobStatus;
import io.imunity.furms.spi.project_installation.ProjectOperationRepository;
import org.springframework.stereotype.Service;

import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.*;
import static io.imunity.furms.domain.authz.roles.ResourceType.*;

@Service
class ProjectInstallationStatusServiceImpl implements ProjectInstallationStatusService {
	private final ProjectOperationRepository projectOperationRepository;

	ProjectInstallationStatusServiceImpl(ProjectOperationRepository projectOperationRepository) {
		this.projectOperationRepository = projectOperationRepository;
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Set<ProjectInstallationJobStatus> findAllBySiteId(String siteId) {
		return projectOperationRepository.findAllBySiteId(siteId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Set<ProjectInstallationJobStatus> findAllByCommunityId(String communityId) {
		return projectOperationRepository.findAllByCommunityId(communityId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Set<ProjectUpdateJobStatus> findAllUpdatesByCommunityId(String communityId) {
		return projectOperationRepository.findAllUpdatesByCommunityId(communityId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId")
	public Set<ProjectInstallationJobStatus> findAllByProjectId(String projectId) {
		return projectOperationRepository.findAllByProjectId(projectId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId")
	public Set<ProjectUpdateJobStatus> findAllUpdatesByProjectId(String projectId) {
		return projectOperationRepository.findAllUpdatesByProjectId(projectId);
	}
}
