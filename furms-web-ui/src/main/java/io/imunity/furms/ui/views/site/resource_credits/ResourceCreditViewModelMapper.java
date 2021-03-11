/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.resource_credits;


import io.imunity.furms.domain.resource_credits.ResourceCredit;

class ResourceCreditViewModelMapper {
	static ResourceCreditViewModel map(ResourceCredit resourceCredit) {
		return ResourceCreditViewModel.builder()
			.id(resourceCredit.id)
			.siteId(resourceCredit.siteId)
			.resourceTypeId(resourceCredit.resourceTypeId)
			.name(resourceCredit.name)
			.split(resourceCredit.split)
			.access(resourceCredit.access)
			.amount(resourceCredit.amount)
			.createTime(resourceCredit.createTime)
			.startTime(resourceCredit.startTime)
			.endTime(resourceCredit.endTime)
			.build();
	}

	static ResourceCredit map(ResourceCreditViewModel resourceCreditViewModel){
		return ResourceCredit.builder()
			.id(resourceCreditViewModel.id)
			.siteId(resourceCreditViewModel.siteId)
			.resourceTypeId(resourceCreditViewModel.resourceTypeId)
			.name(resourceCreditViewModel.name)
			.split(resourceCreditViewModel.split)
			.access(resourceCreditViewModel.access)
			.amount(resourceCreditViewModel.amount)
			.createTime(resourceCreditViewModel.createTime)
			.startTime(resourceCreditViewModel.startTime)
			.endTime(resourceCreditViewModel.endTime)
			.build();
	}
}
