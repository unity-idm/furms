/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.branding.logo.providers;

import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;
import io.imunity.furms.ui.user_context.ViewMode;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class FurmsLogoProviderCommunity implements FurmsLogoProvider{
	private final CommunityService communityService;

	FurmsLogoProviderCommunity(CommunityService communityService) {
		this.communityService = communityService;
	}

	@Override
	public ViewMode getViewMode() {
		return ViewMode.COMMUNITY;
	}

	@Override
	public Optional<FurmsImage> getLogoForCurrentViewMode() {
		final FurmsViewUserContext context = FurmsViewUserContext.getCurrent();
		return communityService.findById(context.id)
				.map(Community::getLogo);
	}
}
