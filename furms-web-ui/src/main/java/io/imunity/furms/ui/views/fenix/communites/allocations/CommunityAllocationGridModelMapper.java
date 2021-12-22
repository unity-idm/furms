/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.communites.allocations;

import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;

class CommunityAllocationGridModelMapper {

	static CommunityAllocationGridModel gridMap(CommunityAllocationResolved communityAllocationResolved) {
		return CommunityAllocationGridModel.builder()
				.id(communityAllocationResolved.id)
				.siteName(communityAllocationResolved.site.getName())
				.resourceTypeName(communityAllocationResolved.resourceType.name)
				.resourceTypeUnit(communityAllocationResolved.resourceType.unit)
				.resourceCreditName(communityAllocationResolved.resourceCredit.name)
				.name(communityAllocationResolved.name)
				.amount(communityAllocationResolved.amount)
				.remaining(communityAllocationResolved.remaining)
				.consumed(communityAllocationResolved.consumed)
				.creationTime(communityAllocationResolved.creationTime)
				.validFrom(communityAllocationResolved.resourceCredit.utcStartTime)
				.validTo(communityAllocationResolved.resourceCredit.utcEndTime)
				.build();
	}

}
