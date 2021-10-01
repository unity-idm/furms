/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.groups;

import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.ui.components.administrators.UserGridItem;

import java.time.ZonedDateTime;

class UserGroupGridItem extends UserGridItem {
	private final ZonedDateTime memberSince;

	UserGroupGridItem(FURMSUser user, ZonedDateTime memberSince){
		super(user);
		this.memberSince = memberSince;
	}

	ZonedDateTime getMemberSince() {
		return memberSince;
	}
}
