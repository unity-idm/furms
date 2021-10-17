/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.users.account;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.site.api.message_resolver.UserAccountStatusUpdateSiteIdResolver;
import org.springframework.stereotype.Component;

@Component
public class UserAccountStatusUpdateSiteIdResolverImpl implements UserAccountStatusUpdateSiteIdResolver {

	private final UserAccountStatusService userAccountStatusService;

	public UserAccountStatusUpdateSiteIdResolverImpl(UserAccountStatusService userAccountStatusService) {
		this.userAccountStatusService = userAccountStatusService;
	}

	@Override
	public SiteExternalId getSiteId(CorrelationId id) {
		final UserAddition userAddition = userAccountStatusService.findByCorrelationId(id);

		return userAddition != null
				? userAddition.siteId.externalId
				: null;
	}

}
