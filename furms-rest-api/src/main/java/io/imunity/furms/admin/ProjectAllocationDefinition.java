/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

class ProjectAllocationDefinition {

	final CommunityAllocationId siteAllocationId;
	final String name;
	final Credits credits;

	ProjectAllocationDefinition(CommunityAllocationId siteAllocationId, String name,
			Credits credits) {
		this.siteAllocationId = siteAllocationId;
		this.name = name;
		this.credits = credits;
	}
}
