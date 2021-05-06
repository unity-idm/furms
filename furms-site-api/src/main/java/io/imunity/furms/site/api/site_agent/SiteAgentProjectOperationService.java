/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api.site_agent;

import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.users.FURMSUser;

public interface SiteAgentProjectOperationService {
	void installProject(CorrelationId correlationId, ProjectInstallation installation);
	void updateProject(CorrelationId correlationId, SiteExternalId siteExternalId, Project project, FURMSUser user);
	void removeProject(CorrelationId correlationId, SiteExternalId siteId, String projectId);
}
