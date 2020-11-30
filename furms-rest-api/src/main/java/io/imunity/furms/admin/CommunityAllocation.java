/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

class CommunityAllocation extends CommunityAllocationDefinition {
	
	final CommunityAllocationId id;

	CommunityAllocation(CommunityAllocationId id, SiteAllocationId siteAllocationId,
			String name, Credits credits) {
		super(siteAllocationId, name, credits);
		this.id = id;
	}
}
