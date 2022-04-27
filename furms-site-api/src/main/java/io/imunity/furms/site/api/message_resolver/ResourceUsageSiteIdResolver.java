/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api.message_resolver;

import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.sites.SiteExternalId;

public interface ResourceUsageSiteIdResolver {
	SiteExternalId getSiteId(ProjectId projectId, ProjectAllocationId projectAllocationId);
}
