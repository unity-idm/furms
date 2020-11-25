/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

class CommunityAllocation
{
	final CommunityAllocationId id;
	final SiteAllocationId siteAllocationId;
	final String name;
	final Credits credits;

	CommunityAllocation(CommunityAllocationId id,
			SiteAllocationId siteAllocationId,
			String name,
			Credits credits)
	{
		this.id = id;
		this.siteAllocationId = siteAllocationId;
		this.name = name;
		this.credits = credits;
	}
}
