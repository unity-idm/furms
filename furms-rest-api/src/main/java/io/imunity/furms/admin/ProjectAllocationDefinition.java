/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

class ProjectAllocationDefinition {

	final CommunityAllocationId communityAllocationId;
	final String name;
	final ResourceType resourceType;
	final ResourceAmount amount;

	ProjectAllocationDefinition(CommunityAllocationId communityAllocationId, String name,
			ResourceType resourceType, ResourceAmount amount) {
		this.communityAllocationId = communityAllocationId;
		this.name = name;
		this.resourceType = resourceType;
		this.amount = amount;
	}
}
