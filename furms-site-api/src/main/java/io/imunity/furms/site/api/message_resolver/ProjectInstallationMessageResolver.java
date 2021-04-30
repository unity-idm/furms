/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api.message_resolver;

import io.imunity.furms.domain.project_installation.ProjectInstallationStatus;
import io.imunity.furms.domain.project_installation.ProjectRemovalStatus;
import io.imunity.furms.domain.project_installation.ProjectUpdateStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;

public interface ProjectInstallationMessageResolver {
	void update(CorrelationId correlationId, ProjectInstallationStatus status);
	void update(CorrelationId correlationId, ProjectUpdateStatus status);
	void update(CorrelationId correlationId, ProjectRemovalStatus status);
}
