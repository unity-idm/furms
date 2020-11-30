/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

class CommunityAllocationDefinition {

	final SiteAllocationId siteAllocationId;
	final String name;
	final ResourceType resourceType;
	final ResourceAmount credits;

	CommunityAllocationDefinition(SiteAllocationId siteAllocationId, String name,
			ResourceType resourceType,
			ResourceAmount credits) {
		this.siteAllocationId = siteAllocationId;
		this.name = name;
		this.resourceType = resourceType;
		this.credits = credits;
	}
}
