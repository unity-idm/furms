/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

class ProjectAllocation
{
	final ProjectAllocationId id;
	final CommunityAllocationId communityAllocationId;
	final String name;
	final Credits credits;

	ProjectAllocation(ProjectAllocationId id,
			CommunityAllocationId communityAllocationId,
			String name,
			Credits credits)
	{
		this.id = id;
		this.communityAllocationId = communityAllocationId;
		this.name = name;
		this.credits = credits;
	}

}
