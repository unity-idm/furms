/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_access;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.site.api.message_resolver.UserAllocationGrantSiteIdResolver;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class UserAllocationGrantSiteIdResolverImpl implements UserAllocationGrantSiteIdResolver {
	private final ResourceAccessRepository repository;
	private final SiteRepository siteRepository;

	UserAllocationGrantSiteIdResolverImpl(ResourceAccessRepository repository, SiteRepository siteRepository) {
		this.repository = repository;
		this.siteRepository = siteRepository;
	}

	@Override
	@Transactional
	public SiteExternalId getSiteId(CorrelationId id) {
		String siteId = repository.findSiteIdByCorrelationId(id);
		return siteRepository.findByIdExternalId(siteId);
	}
}
