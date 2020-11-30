/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

class CommunityAllocation extends CommunityAllocationDefinition {
	
	final CommunityAllocationId id;

	CommunityAllocation(SiteCreditId siteAllocationId, String name, ResourceType resourceType,
			ResourceAmount credits, CommunityAllocationId id) {
		super(siteAllocationId, name, resourceType, credits);
		this.id = id;
	}
}
