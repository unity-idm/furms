/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

class ProjectAllocation extends ProjectAllocationDefinition {
	
	final ProjectAllocationId id;

	ProjectAllocation(CommunityAllocationId communityAllocationId, String name, ResourceType resourceType,
			ResourceAmount credits, ProjectAllocationId id) {
		super(communityAllocationId, name, resourceType, credits);
		this.id = id;
	}
}
