/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.communites.allocations;

import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;

class CommunityAllocationGridModelMapper {

	static CommunityAllocationGridModel gridMap(CommunityAllocationResolved CommunityAllocation) {
		return CommunityAllocationGridModel.builder()
				.id(CommunityAllocation.id)
				.siteName(CommunityAllocation.site.getName())
				.resourceTypeName(CommunityAllocation.resourceType.name)
				.resourceTypeUnit(CommunityAllocation.resourceType.unit.name())
				.resourceCreditName(CommunityAllocation.resourceCredit.name)
				.name(CommunityAllocation.name)
				.amount(CommunityAllocation.amount)
				.build();
	}

}
