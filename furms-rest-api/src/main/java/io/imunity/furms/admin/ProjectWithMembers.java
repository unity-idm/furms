/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

import java.util.List;

import com.google.common.collect.ImmutableList;

class ProjectWithMembers extends Project
{
	final List<String> memberFenixUserIds;

	ProjectWithMembers(String id,
			String name,
			String description,
			List<String> allocations,
			String communityId,
			List<String> memberFenixUserIds)
	{
		super(id, name, description, allocations, communityId);
		this.memberFenixUserIds = ImmutableList.copyOf(memberFenixUserIds);
	}

}
