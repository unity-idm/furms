/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.authz.roles;

import static io.imunity.furms.domain.constant.LoginFlowConst.*;

public enum RoleLevel {
	FENIX(FENIX_START_URL, 1), SITE(SITE_START_URL, 2), COMMUNITY(COMMUNITY_START_URL, 3),
	PROJECT(PROJECT_START_URL, 4), USER(USER_START_URL, 5);

	public final String startUrl;
	public final int order;

	RoleLevel(String url, int order) {
		this.startUrl = url;
		this.order = order;
	}
}
