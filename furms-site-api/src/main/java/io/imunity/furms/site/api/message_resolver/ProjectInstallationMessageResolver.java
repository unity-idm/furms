/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api.message_resolver;

import io.imunity.furms.domain.project_installation.ProjectInstallationResult;
import io.imunity.furms.domain.project_installation.ProjectUpdateResult;
import io.imunity.furms.domain.site_agent.CorrelationId;

public interface ProjectInstallationMessageResolver {
	void update(CorrelationId correlationId, ProjectInstallationResult result);
	void update(CorrelationId correlationId, ProjectUpdateResult result);
}
