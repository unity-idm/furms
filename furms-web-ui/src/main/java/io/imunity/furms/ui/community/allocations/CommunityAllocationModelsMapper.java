/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.community.allocations;

import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;

public class CommunityAllocationModelsMapper {

	public static CommunityAllocationViewModel map(CommunityAllocationResolved CommunityAllocation) {
		return CommunityAllocationViewModel.builder()
			.id(CommunityAllocation.id)
			.communityId(CommunityAllocation.communityId)
			.site(new SiteComboBoxModel(CommunityAllocation.site.getId(), CommunityAllocation.site.getName()))
			.resourceType(new ResourceTypeComboBoxModel(CommunityAllocation.resourceType.id, CommunityAllocation.resourceType.name, CommunityAllocation.resourceType.unit))
			.resourceCredit(new ResourceCreditComboBoxModel(CommunityAllocation.resourceCredit.id, CommunityAllocation.resourceCredit.name, CommunityAllocation.resourceCredit.amount, CommunityAllocation.resourceCredit.split))
			.name(CommunityAllocation.name)
			.amount(CommunityAllocation.amount)
			.build();
	}

	public static CommunityAllocation map(CommunityAllocationViewModel resourceCreditViewModel){
		return CommunityAllocation.builder()
			.id(resourceCreditViewModel.getId())
			.communityId(resourceCreditViewModel.getCommunityId())
			.resourceCreditId(resourceCreditViewModel.getResourceCredit().id)
			.name(resourceCreditViewModel.getName())
			.amount(resourceCreditViewModel.getAmount())
			.build();
	}
}
