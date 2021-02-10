/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

class CommunityAllocationDefinition {

	final SiteCreditId siteAllocationId;
	final String name;
	final ResourceType resourceType;
	final ResourceAmount amount;

	CommunityAllocationDefinition(SiteCreditId siteAllocationId, String name,
			ResourceType resourceType,
			ResourceAmount amount) {
		this.siteAllocationId = siteAllocationId;
		this.name = name;
		this.resourceType = resourceType;
		this.amount = amount;
	}
}
