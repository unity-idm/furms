/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation_installation;

import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.site.api.message_resolver.ProjectAllocationInstallationSiteIdResolver;
import io.imunity.furms.spi.project_allocation_installation.ProjectAllocationInstallationRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class ProjectAllocationInstallationSiteIdResolverImpl implements ProjectAllocationInstallationSiteIdResolver {
	private final ProjectAllocationInstallationRepository projectAllocationInstallationRepository;
	private final SiteRepository siteRepository;

	ProjectAllocationInstallationSiteIdResolverImpl(ProjectAllocationInstallationRepository projectAllocationInstallationRepository, SiteRepository siteRepository) {
		this.projectAllocationInstallationRepository = projectAllocationInstallationRepository;
		this.siteRepository = siteRepository;
	}

	@Override
	@Transactional
	public SiteExternalId getSiteId(CorrelationId id) {
		ProjectAllocationInstallation installation = projectAllocationInstallationRepository.findByCorrelationId(id)
			.orElseThrow(() -> new IllegalArgumentException("CorrelationId doesn't exist: " + id));
		return siteRepository.findByIdExternalId(installation.siteId);
	}
}
