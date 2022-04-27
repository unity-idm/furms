/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.validation.exceptions;

import io.imunity.furms.domain.sites.SiteId;

public class UninstalledUserError extends IllegalArgumentException {

	public final SiteId siteId;

	public UninstalledUserError(String message, SiteId siteId) {
		super(message);
		this.siteId = siteId;
	}
}
