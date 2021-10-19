/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.project_installation;

import io.imunity.furms.domain.project_installation.*;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteInstalledProject;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public interface ProjectOperationRepository {
	ProjectInstallationJob findInstallationJobByCorrelationId(CorrelationId id);

	ProjectUpdateJob findUpdateJobByCorrelationId(CorrelationId id);

	ProjectInstallation findProjectInstallation(String projectAllocationId, Function<PersistentId, Optional<FURMSUser>> userGetter);

	String createOrUpdate(ProjectInstallationJob projectInstallationJob);

	String createOrUpdate(ProjectUpdateJob projectUpdateJob);

	String update(String id, ProjectInstallationResult result);

	String update(String id, ProjectUpdateResult result);

	boolean installedProjectExistsBySiteIdAndProjectId(String siteId, String projectId);

	boolean areAllProjectOperationInTerminateState(String projectId);

	Set<ProjectInstallationJobStatus> findAllBySiteId(String siteId);

	Set<ProjectInstallationJobStatus> findAllByCommunityId(String communityId);

	Set<ProjectUpdateJobStatus> findAllUpdatesByCommunityId(String communityId);

	Set<ProjectInstallationJobStatus> findAllByProjectId(String projectId);

	Set<ProjectUpdateJobStatus> findAllUpdatesByProjectId(String projectId);

	Set<ProjectInstallationJob> findProjectInstallation(String projectId);

	Set<ProjectUpdateStatus> findProjectUpdateStatues(String projectId);

	Set<SiteInstalledProject> findAllSiteInstalledProjectsBySiteId(String siteId);

	Set<SiteInstalledProject> findAllSiteInstalledProjectsByProjectId(String projectId);

	void deleteById(String id);

	void deleteAll();
}
