/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.project_installation;

import io.imunity.furms.domain.project_installation.*;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public interface ProjectOperationRepository {
	ProjectInstallationJob findInstallationJobByCorrelationId(CorrelationId id);

	ProjectUpdateJob findUpdateJobByCorrelationId(CorrelationId id);

	ProjectInstallation findProjectInstallation(String projectAllocationId, Function<PersistentId, Optional<FURMSUser>> userGetter);

	String create(ProjectInstallationJob projectInstallationJob);

	String create(ProjectUpdateJob projectUpdateJob);

	String update(String id, ProjectInstallationStatus status, String gid);

	String update(String id, ProjectUpdateStatus status);

	boolean installedProjectExistsBySiteIdAndProjectId(String siteId, String projectId);

	boolean areAllProjectOperationInTerminateState(String projectId);

	Set<ProjectInstallationJob> findProjectInstallation(String projectId);

	Set<ProjectUpdateStatus> findProjectUpdateStatues(String projectId);

	void deleteById(String id);

	void deleteAll();
}
