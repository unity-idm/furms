/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

import java.util.List;

import com.google.common.collect.ImmutableList;

class Project
{
	final String id;
	final String name;
	final String description;
	final List<String> allocations;
	final String communityId;

	Project(String id,
			String name,
			String description,
			List<String> allocations,
			String communityId)
	{
		this.id = id;
		this.name = name;
		this.description = description;
		this.allocations = ImmutableList.copyOf(allocations);
		this.communityId = communityId;
	}

}
