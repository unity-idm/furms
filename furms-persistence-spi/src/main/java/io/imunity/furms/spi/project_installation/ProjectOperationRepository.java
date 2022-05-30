/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.project_installation;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.project_installation.*;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.sites.SiteInstalledProject;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public interface ProjectOperationRepository {
	Optional<ProjectInstallationJob> findInstallationJobByCorrelationId(CorrelationId id);

	Optional<ProjectUpdateJob> findUpdateJobByCorrelationId(CorrelationId id);

	ProjectInstallation findProjectInstallation(ProjectAllocationId projectAllocationId,
	                                            Function<PersistentId, Optional<FURMSUser>> userGetter);

	void createOrUpdate(ProjectInstallationJob projectInstallationJob);

	void createOrUpdate(ProjectUpdateJob projectUpdateJob);

	void update(ProjectInstallationId id, ProjectInstallationResult result);

	void update(ProjectUpdateId id, ProjectUpdateResult result);

	boolean installedProjectExistsBySiteIdAndProjectId(SiteId siteId, ProjectId projectId);

	boolean areAllProjectOperationInTerminateState(ProjectId projectId);

	Set<ProjectInstallationJobStatus> findAllBySiteId(SiteId siteId);

	Set<ProjectInstallationJobStatus> findAllByCommunityId(CommunityId communityId);

	Set<ProjectUpdateJobStatus> findAllUpdatesByCommunityId(CommunityId communityId);

	Set<ProjectInstallationJobStatus> findAllByProjectId(ProjectId projectId);

	Set<ProjectUpdateJobStatus> findAllUpdatesByProjectId(ProjectId projectId);

	Set<ProjectInstallationJob> findProjectInstallation(ProjectId projectId);

	Set<ProjectUpdateStatus> findProjectUpdateStatues(ProjectId projectId);

	Set<SiteInstalledProject> findAllSiteInstalledProjectsBySiteId(SiteId siteId);

	Set<SiteInstalledProject> findAllSiteInstalledProjectsByProjectId(ProjectId projectId);

	void deleteById(ProjectInstallationId id);

	void deleteAll();

	void delete(CorrelationId id);
}
