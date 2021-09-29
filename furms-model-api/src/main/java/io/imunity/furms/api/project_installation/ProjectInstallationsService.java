/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.project_installation;

import io.imunity.furms.domain.project_installation.ProjectInstallationJobStatus;
import io.imunity.furms.domain.project_installation.ProjectUpdateJobStatus;
import io.imunity.furms.domain.sites.SiteInstalledProject;
import io.imunity.furms.domain.sites.SiteInstalledProjectResolved;

import java.util.Set;

public interface ProjectInstallationsService {
	Set<SiteInstalledProjectResolved> findAllSiteInstalledProjectsBySiteId(String siteId);
	Set<SiteInstalledProject> findAllSiteInstalledProjectsByProjectId(String projectId);
	Set<ProjectInstallationJobStatus> findAllBySiteId(String siteId);
	Set<ProjectInstallationJobStatus> findAllByCommunityId(String communityId);
	Set<ProjectUpdateJobStatus> findAllUpdatesByCommunityId(String communityId);
	Set<ProjectInstallationJobStatus> findAllByProjectId(String projectId);
	Set<ProjectUpdateJobStatus> findAllUpdatesByProjectId(String projectId);
}
