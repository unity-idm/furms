/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.communites.allocations;


import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;

class CommunityAllocationModelsMapper {
	static CommunityAllocationViewModel map(CommunityAllocationResolved CommunityAllocation) {
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

	static CommunityAllocation map(CommunityAllocationViewModel resourceCreditViewModel){
		return CommunityAllocation.builder()
			.id(resourceCreditViewModel.id)
			.communityId(resourceCreditViewModel.communityId)
			.resourceCreditId(resourceCreditViewModel.resourceCredit.id)
			.name(resourceCreditViewModel.name)
			.amount(resourceCreditViewModel.amount)
			.build();
	}

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
