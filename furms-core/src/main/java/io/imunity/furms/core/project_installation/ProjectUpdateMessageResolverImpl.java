/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.domain.project_installation.ProjectUpdateJob;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.site.api.message_resolver.ProjectUpdateMessageResolver;
import io.imunity.furms.spi.project_installation.ProjectOperationRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class ProjectUpdateMessageResolverImpl implements ProjectUpdateMessageResolver {
	private final ProjectOperationRepository projectOperationRepository;
	private final SiteRepository siteRepository;

	ProjectUpdateMessageResolverImpl(ProjectOperationRepository projectOperationRepository, SiteRepository siteRepository) {
		this.projectOperationRepository = projectOperationRepository;
		this.siteRepository = siteRepository;
	}

	@Override
	@Transactional
	public boolean isMessageCorrelated(CorrelationId id, SiteExternalId siteExternalId) {
		ProjectUpdateJob update = projectOperationRepository.findUpdateJobByCorrelationId(id);
		SiteExternalId externalId = siteRepository.findByIdExternalId(update.siteId);
		return externalId.equals(siteExternalId);
	}
}
