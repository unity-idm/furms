/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

class ProjectAllocation extends ProjectAllocationDefinition {
	
	final ProjectAllocationId id;

	ProjectAllocation(ProjectAllocationId id, CommunityAllocationId communityAllocationId,
			String name, ResourceCredit credits) {
		super(communityAllocationId, name, credits);
		this.id = id;
	}
}
