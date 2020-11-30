package io.imunity.furms.admin;
/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

import java.util.List;

import com.google.common.collect.ImmutableList;

class GroupWithMembers extends Group {
	final List<String> memberFenixUserIds;

	GroupWithMembers(String id, String name, String description,
			List<String> memberFenixUserIds) {
		super(id, name, description);
		this.memberFenixUserIds = ImmutableList.copyOf(memberFenixUserIds);
	}
}
