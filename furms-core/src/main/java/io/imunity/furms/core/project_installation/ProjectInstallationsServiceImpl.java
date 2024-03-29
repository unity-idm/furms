/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.api.project_installation.ProjectInstallationsService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.project_installation.ProjectInstallationJobStatus;
import io.imunity.furms.domain.project_installation.ProjectUpdateJobStatus;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.sites.SiteInstalledProject;
import io.imunity.furms.domain.sites.SiteInstalledProjectResolved;
import io.imunity.furms.spi.project_installation.ProjectOperationRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.*;
import static io.imunity.furms.domain.authz.roles.ResourceType.*;
import static java.util.stream.Collectors.toSet;

@Service
class ProjectInstallationsServiceImpl implements ProjectInstallationsService {
	private final ProjectOperationRepository projectOperationRepository;
	private final ProjectRepository projectRepository;
	private final SiteService siteService;

	ProjectInstallationsServiceImpl(ProjectOperationRepository projectOperationRepository,
	                                ProjectRepository projectRepository,
	                                SiteService siteService) {
		this.projectOperationRepository = projectOperationRepository;
		this.projectRepository = projectRepository;
		this.siteService = siteService;
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Set<SiteInstalledProjectResolved> findAllSiteInstalledProjectsBySiteId(SiteId siteId) {
		return projectOperationRepository.findAllSiteInstalledProjectsBySiteId(siteId).stream()
				.map(this::toSiteInstalledProjectResolved)
				.collect(toSet());
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED)
	public Set<SiteInstalledProjectResolved> findAllSiteInstalledProjectsOfCurrentUser() {
		return siteService.findAllOfCurrentUserId().stream()
				.map(site -> projectOperationRepository.findAllSiteInstalledProjectsBySiteId(site.getId()))
				.flatMap(Collection::stream)
				.map(this::toSiteInstalledProjectResolved)
				.collect(toSet());
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id = "projectId")
	public Set<SiteInstalledProject> findAllSiteInstalledProjectsByProjectId(ProjectId projectId) {
		return projectOperationRepository.findAllSiteInstalledProjectsByProjectId(projectId);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Set<ProjectInstallationJobStatus> findAllBySiteId(SiteId siteId) {
		return projectOperationRepository.findAllBySiteId(siteId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Set<ProjectInstallationJobStatus> findAllByCommunityId(CommunityId communityId) {
		return projectOperationRepository.findAllByCommunityId(communityId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Set<ProjectUpdateJobStatus> findAllUpdatesByCommunityId(CommunityId communityId) {
		return projectOperationRepository.findAllUpdatesByCommunityId(communityId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId")
	public Set<ProjectInstallationJobStatus> findAllByProjectId(ProjectId projectId) {
		return projectOperationRepository.findAllByProjectId(projectId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId")
	public Set<ProjectUpdateJobStatus> findAllUpdatesByProjectId(ProjectId projectId) {
		return projectOperationRepository.findAllUpdatesByProjectId(projectId);
	}

	private SiteInstalledProjectResolved toSiteInstalledProjectResolved(SiteInstalledProject installation) {
		return SiteInstalledProjectResolved.builder()
				.siteId(installation.siteId)
				.siteName(installation.siteName)
				.project(projectRepository.findById(installation.projectId).get())
				.gid(installation.gid)
				.build();
	}
}
