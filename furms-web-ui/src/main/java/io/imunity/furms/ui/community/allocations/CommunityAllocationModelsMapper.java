/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.community.allocations;

import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
import io.imunity.furms.ui.components.support.models.ComboBoxModel;
import io.imunity.furms.ui.components.support.models.allocation.ResourceCreditComboBoxModel;
import io.imunity.furms.ui.components.support.models.allocation.ResourceTypeComboBoxModel;

public class CommunityAllocationModelsMapper {

	public static CommunityAllocationViewModel map(CommunityAllocationResolved communityAllocationResolved) {
		return CommunityAllocationViewModel.builder()
			.id(communityAllocationResolved.id)
			.communityId(communityAllocationResolved.communityId)
			.communityName(communityAllocationResolved.communityName)
			.site(new ComboBoxModel(communityAllocationResolved.site.getId(), communityAllocationResolved.site.getName()))
			.resourceType(new ResourceTypeComboBoxModel(communityAllocationResolved.resourceType.id, communityAllocationResolved.resourceType.name, communityAllocationResolved.resourceType.unit))
			.resourceCredit(new ResourceCreditComboBoxModel(communityAllocationResolved.resourceCredit.id, communityAllocationResolved.resourceCredit.name, communityAllocationResolved.resourceCredit.amount, communityAllocationResolved.resourceCredit.splittable))
			.name(communityAllocationResolved.name)
			.amount(communityAllocationResolved.amount)
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
