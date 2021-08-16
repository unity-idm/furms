/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.administrators;

import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.ui.views.site.administrators.SiteRole;

import java.util.Optional;

public class SiteUserGridItem extends UserGridItem {
	private final Optional<SiteRole> siteRole;

	public SiteUserGridItem(FURMSUser user, SiteRole role){
		super(user);
		this.siteRole = Optional.of(role);
	}

	public Optional<SiteRole> getSiteRole() {
		return siteRole;
	}

}
