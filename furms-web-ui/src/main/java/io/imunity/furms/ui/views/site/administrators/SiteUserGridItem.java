/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.administrators;

import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.ui.components.administrators.UserGridItem;

import java.util.Optional;

public class SiteUserGridItem extends UserGridItem {
	private final Optional<SiteRole> siteRole;

	SiteUserGridItem(FURMSUser user, SiteRole role){
		super(user);
		this.siteRole = Optional.of(role);
	}

	SiteUserGridItem(String email, SiteRole role, InvitationId invitationId){
		super(email, invitationId);
		this.siteRole = Optional.of(role);
	}

	public Optional<SiteRole> getSiteRole() {
		return siteRole;
	}

}
