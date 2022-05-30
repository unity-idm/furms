/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.branding.logo.providers;

import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;
import io.imunity.furms.ui.user_context.ViewMode;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class FurmsLogoProviderSite implements FurmsLogoProvider{
	private final SiteService siteService;

	FurmsLogoProviderSite(SiteService siteService) {
		this.siteService = siteService;
	}

	@Override
	public ViewMode getViewMode() {
		return ViewMode.SITE;
	}

	@Override
	public Optional<FurmsImage> getLogoForCurrentViewMode() {
		final FurmsViewUserContext context = FurmsViewUserContext.getCurrent();
		return siteService.findById(new SiteId(context.id))
				.map(Site::getLogo);
	}
}
