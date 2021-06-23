/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.project_installation;

import io.imunity.furms.domain.project_installation.ProjectInstallationJobStatus;

import java.util.Set;

public interface ProjectInstallationStatusService {
	Set<ProjectInstallationJobStatus> findAllByCommunityId(String communityId);
}
