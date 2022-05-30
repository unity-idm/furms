/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.project_installation;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.project_installation.ProjectInstallationJobStatus;
import io.imunity.furms.domain.project_installation.ProjectUpdateJobStatus;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.sites.SiteInstalledProject;
import io.imunity.furms.domain.sites.SiteInstalledProjectResolved;

import java.util.Set;

public interface ProjectInstallationsService {
	Set<SiteInstalledProjectResolved> findAllSiteInstalledProjectsBySiteId(SiteId siteId);
	Set<SiteInstalledProjectResolved> findAllSiteInstalledProjectsOfCurrentUser();
	Set<SiteInstalledProject> findAllSiteInstalledProjectsByProjectId(ProjectId projectId);
	Set<ProjectInstallationJobStatus> findAllBySiteId(SiteId siteId);
	Set<ProjectInstallationJobStatus> findAllByCommunityId(CommunityId communityId);
	Set<ProjectUpdateJobStatus> findAllUpdatesByCommunityId(CommunityId communityId);
	Set<ProjectInstallationJobStatus> findAllByProjectId(ProjectId projectId);
	Set<ProjectUpdateJobStatus> findAllUpdatesByProjectId(ProjectId projectId);
}
