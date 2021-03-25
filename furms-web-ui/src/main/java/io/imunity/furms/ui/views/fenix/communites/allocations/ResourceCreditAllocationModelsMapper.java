/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.communites.allocations;


import io.imunity.furms.domain.resource_credit_allocation.ResourceCreditAllocation;
import io.imunity.furms.domain.resource_credit_allocation.ResourceCreditAllocationExtend;

class ResourceCreditAllocationModelsMapper {
	static ResourceCreditAllocationViewModel map(ResourceCreditAllocationExtend resourceCreditAllocation) {
		return ResourceCreditAllocationViewModel.builder()
			.id(resourceCreditAllocation.id)
			.communityId(resourceCreditAllocation.communityId)
			.site(new SiteComboBoxModel(resourceCreditAllocation.site.getId(), resourceCreditAllocation.site.getName()))
			.resourceType(new ResourceTypeComboBoxModel(resourceCreditAllocation.resourceType.id, resourceCreditAllocation.resourceType.name, resourceCreditAllocation.resourceType.unit))
			.resourceCredit(new ResourceCreditComboBoxModel(resourceCreditAllocation.resourceCredit.id, resourceCreditAllocation.resourceCredit.name, resourceCreditAllocation.resourceCredit.amount, resourceCreditAllocation.resourceCredit.split))
			.name(resourceCreditAllocation.name)
			.amount(resourceCreditAllocation.amount)
			.build();
	}

	static ResourceCreditAllocation map(ResourceCreditAllocationViewModel resourceCreditViewModel){
		return ResourceCreditAllocation.builder()
			.id(resourceCreditViewModel.id)
			.communityId(resourceCreditViewModel.communityId)
			.siteId(resourceCreditViewModel.site.id)
			.resourceTypeId(resourceCreditViewModel.resourceType.id)
			.resourceCreditId(resourceCreditViewModel.resourceCredit.id)
			.name(resourceCreditViewModel.name)
			.amount(resourceCreditViewModel.amount)
			.build();
	}

	static ResourceCreditAllocationGridModel gridMap(ResourceCreditAllocationExtend resourceCreditAllocation) {
		return ResourceCreditAllocationGridModel.builder()
			.id(resourceCreditAllocation.id)
			.siteName(resourceCreditAllocation.site.getName())
			.resourceTypeName(resourceCreditAllocation.resourceType.name)
			.resourceTypeUnit(resourceCreditAllocation.resourceType.unit.name())
			.resourceCreditName(resourceCreditAllocation.resourceCredit.name)
			.name(resourceCreditAllocation.name)
			.amount(resourceCreditAllocation.amount)
			.build();
	}
}
