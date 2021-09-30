/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.groups;

import io.imunity.furms.domain.generic_groups.GenericGroupAssignmentId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.ui.components.administrators.UserGridItem;

import java.time.ZonedDateTime;

public class UserGroupGridItem extends UserGridItem {
	private final GenericGroupAssignmentId genericGroupAssignmentId;
	private final ZonedDateTime memberSince;

	UserGroupGridItem(FURMSUser user, GenericGroupAssignmentId genericGroupAssignmentId, ZonedDateTime memberSince){
		super(user);
		this.memberSince = memberSince;
		this.genericGroupAssignmentId = genericGroupAssignmentId;
	}

	ZonedDateTime getMemberSince() {
		return memberSince;
	}

	GenericGroupAssignmentId getGenericGroupAssignmentId() {
		return genericGroupAssignmentId;
	}
}
