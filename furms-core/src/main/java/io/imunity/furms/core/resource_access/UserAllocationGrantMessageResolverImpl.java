/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_access;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.site.api.message_resolver.UserAllocationGrantMessageResolver;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class UserAllocationGrantMessageResolverImpl implements UserAllocationGrantMessageResolver {
	private final ResourceAccessRepository repository;
	private final SiteRepository siteRepository;

	UserAllocationGrantMessageResolverImpl(ResourceAccessRepository repository, SiteRepository siteRepository) {
		this.repository = repository;
		this.siteRepository = siteRepository;
	}

	@Override
	@Transactional
	public boolean isMessageCorrelated(CorrelationId id, SiteExternalId siteExternalId) {
		String siteId = repository.findSiteIdByCorrelationId(id);
		SiteExternalId externalId = siteRepository.findByIdExternalId(siteId);
		return externalId.equals(siteExternalId);
	}
}
