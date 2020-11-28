/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

import java.util.List;

import com.google.common.collect.ImmutableList;

class Community
{
	final String id;
	final String name;
	final List<String> allocations;

	Community(String id, String name, List<String> allocations)
	{
		this.id = id;
		this.name = name;
		this.allocations = ImmutableList.copyOf(allocations);
	}

}
