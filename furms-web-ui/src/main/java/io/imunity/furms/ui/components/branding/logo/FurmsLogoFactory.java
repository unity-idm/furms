/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.branding.logo;

import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.ui.components.branding.logo.providers.FurmsLogoProvider;
import io.imunity.furms.ui.components.branding.logo.providers.UnsupportedViewModeException;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;
import io.imunity.furms.ui.user_context.ViewMode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class FurmsLogoFactory {

	private List<FurmsLogoProvider> providers;

	FurmsLogoFactory(List<FurmsLogoProvider> providers) {
		this.providers = providers;
	}

	public FurmsLogo create() {
		return new FurmsLogo(findLogo());
	}

	private Optional<FurmsImage> findLogo() {
		final ViewMode currentViewMode = currentViewMode();
		return providers.stream()
				.filter(provider -> provider.getViewMode().equals(currentViewMode))
				.findFirst()
				.map(FurmsLogoProvider::getLogoForCurrentViewMode)
				.orElseThrow(() -> new UnsupportedViewModeException(currentViewMode));
	}

	private ViewMode currentViewMode() {
		return FurmsViewUserContext.getCurrent().viewMode;
	}
}
