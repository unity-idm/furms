/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.user_context;

import static io.imunity.furms.domain.constant.RoutesConst.*;

public enum ViewMode {
	FENIX(FENIX_ADMIN_LANDING_PAGE, 1), SITE(SITE_BASE_LANDING_PAGE, 2), COMMUNITY(COMMUNITY_BASE_LANDING_PAGE, 3),
	PROJECT(PROJECT_BASE_LANDING_PAGE, 4), USER(USER_BASE_LANDING_PAGE, 5);

	public final String route;
	public final int order;

	ViewMode(String route, int order) {
		this.route = route;
		this.order = order;
	}

	public boolean hasHeader() {
		return this != FENIX && this != USER;
	}
}
