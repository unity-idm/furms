/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation_installation;

import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.site.api.message_resolver.ProjectAllocationChunkSiteIdResolver;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import org.springframework.stereotype.Component;

@Component
class ProjectAllocationChunkSiteIdResolverImpl implements ProjectAllocationChunkSiteIdResolver {
	private final ProjectAllocationRepository projectAllocationRepository;

	ProjectAllocationChunkSiteIdResolverImpl(ProjectAllocationRepository projectAllocationRepository) {
		this.projectAllocationRepository = projectAllocationRepository;
	}

	@Override
	public SiteExternalId getSiteId(String projectAllocationId) {
		ProjectAllocationResolved projectAllocationResolved = projectAllocationRepository.findByIdWithRelatedObjects(projectAllocationId)
			.orElseThrow(() -> new IllegalArgumentException("Project Allocation doesn't exist: " + projectAllocationId));
		return projectAllocationResolved.site.getExternalId();
	}
}
