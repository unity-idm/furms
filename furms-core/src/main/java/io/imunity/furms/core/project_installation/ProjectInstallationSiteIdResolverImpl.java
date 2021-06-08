/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.domain.project_installation.ProjectInstallationJob;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.site.api.message_resolver.ProjectInstallationSiteIdResolver;
import io.imunity.furms.spi.project_installation.ProjectOperationRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class ProjectInstallationSiteIdResolverImpl implements ProjectInstallationSiteIdResolver {
	private final ProjectOperationRepository projectOperationRepository;
	private final SiteRepository siteRepository;

	ProjectInstallationSiteIdResolverImpl(ProjectOperationRepository projectOperationRepository, SiteRepository siteRepository) {
		this.projectOperationRepository = projectOperationRepository;
		this.siteRepository = siteRepository;
	}

	@Override
	@Transactional
	public SiteExternalId getSiteId(CorrelationId id) {
		ProjectInstallationJob installationJobByCorrelationId = projectOperationRepository.findInstallationJobByCorrelationId(id);
		return siteRepository.findByIdExternalId(installationJobByCorrelationId.siteId);
	}
}
