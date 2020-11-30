/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

class CommunityAllocationDefinition {

	final SiteAllocationId siteAllocationId;
	final String name;
	final ResourceCredit credits;

	CommunityAllocationDefinition(SiteAllocationId siteAllocationId, String name,
			ResourceCredit credits) {
		this.siteAllocationId = siteAllocationId;
		this.name = name;
		this.credits = credits;
	}
}
