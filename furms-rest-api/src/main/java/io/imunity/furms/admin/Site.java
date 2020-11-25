/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

import java.util.List;

import com.google.common.collect.ImmutableList;

class Site
{
	final String id;
	final String name;
	final List<String> allocationIds;
	final List<String> resourceTypeIds;
	final List<String> serviceIds;
	final List<String> policieIds;

	Site(String id,
			String name,
			List<String> allocationIds,
			List<String> resourceTypeIds,
			List<String> serviceIds,
			List<String> policieIds)
	{
		this.id = id;
		this.name = name;
		this.allocationIds = ImmutableList.copyOf(allocationIds);
		this.resourceTypeIds = ImmutableList.copyOf(resourceTypeIds);
		this.serviceIds = ImmutableList.copyOf(serviceIds);
		this.policieIds = ImmutableList.copyOf(policieIds);
	}
}
