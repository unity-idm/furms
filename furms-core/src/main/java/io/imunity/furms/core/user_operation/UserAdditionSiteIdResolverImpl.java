/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.user_operation;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.site.api.message_resolver.UserAdditionSiteIdResolver;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class UserAdditionSiteIdResolverImpl implements UserAdditionSiteIdResolver {
	private final UserOperationRepository repository;
	private final SiteRepository siteRepository;

	UserAdditionSiteIdResolverImpl(UserOperationRepository repository, SiteRepository siteRepository) {
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
