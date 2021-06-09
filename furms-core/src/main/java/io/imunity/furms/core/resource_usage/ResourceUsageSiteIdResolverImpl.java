/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_usage;

import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.site.api.message_resolver.ResourceUsageSiteIdResolver;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import org.springframework.stereotype.Service;

@Service
class ResourceUsageSiteIdResolverImpl implements ResourceUsageSiteIdResolver {
	private final ProjectAllocationRepository projectAllocationRepository;

	ResourceUsageSiteIdResolverImpl(ProjectAllocationRepository projectAllocationRepository) {
		this.projectAllocationRepository = projectAllocationRepository;
	}

	@Override
	public SiteExternalId getSiteId(String projectId, String projectAllocationId) {
		ProjectAllocationResolved projectAllocationResolved = projectAllocationRepository.findByIdWithRelatedObjects(projectAllocationId)
			.orElseThrow(() -> new IllegalArgumentException("Project Allocation doesn't exist: " + projectAllocationId));
		if(!projectAllocationResolved.projectId.equals(projectId))
			return null;
		return projectAllocationResolved.site.getExternalId();
	}
}
