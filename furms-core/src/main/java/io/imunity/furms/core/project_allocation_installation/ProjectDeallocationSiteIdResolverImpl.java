/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation_installation;

import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.site.api.message_resolver.ProjectDeallocationSiteIdResolver;
import io.imunity.furms.spi.project_allocation_installation.ProjectAllocationInstallationRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class ProjectDeallocationSiteIdResolverImpl implements ProjectDeallocationSiteIdResolver {
	private final ProjectAllocationInstallationRepository projectAllocationInstallationRepository;
	private final SiteRepository siteRepository;

	ProjectDeallocationSiteIdResolverImpl(ProjectAllocationInstallationRepository projectAllocationInstallationRepository, SiteRepository siteRepository) {
		this.projectAllocationInstallationRepository = projectAllocationInstallationRepository;
		this.siteRepository = siteRepository;
	}

	@Override
	@Transactional
	public SiteExternalId getSiteId(CorrelationId id) {
		ProjectDeallocation installation = projectAllocationInstallationRepository.findDeallocationByCorrelationId(id.id);
		return siteRepository.findByIdExternalId(installation.siteId);
	}
}
