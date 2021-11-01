/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.branding.logo.providers;

import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.ui.user_context.ViewMode;

import java.util.Optional;

public interface FurmsLogoProvider {
	ViewMode getViewMode();
	Optional<FurmsImage> getLogoForCurrentViewMode();
}
