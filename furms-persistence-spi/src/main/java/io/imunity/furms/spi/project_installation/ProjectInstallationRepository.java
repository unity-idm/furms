/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.project_installation;

import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.project_installation.ProjectInstallationJob;
import io.imunity.furms.domain.project_installation.ProjectInstallationStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;

import java.util.Optional;
import java.util.function.Function;

public interface ProjectInstallationRepository {
	ProjectInstallationJob findByCorrelationId(CorrelationId id);

	ProjectInstallation findProjectInstallation(String projectAllocationId, Function<PersistentId, Optional<FURMSUser>> userGetter);

	String create(ProjectInstallationJob projectInstallationJob);

	String update(String id, ProjectInstallationStatus status);

	boolean existsByProjectId(String projectId);

	void delete(String id);

	void deleteAll();
}
