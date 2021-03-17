/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.resource_credits;


import io.imunity.furms.domain.resource_credits.ResourceCredit;

import java.time.ZoneId;

import static io.imunity.furms.ui.utils.UTCTimeUtils.convertToUTCTime;
import static io.imunity.furms.ui.utils.UTCTimeUtils.convertToZoneTime;

class ResourceCreditViewModelMapper {
	static ResourceCreditViewModel map(ResourceCredit resourceCredit, ZoneId zoneId) {
		return ResourceCreditViewModel.builder()
			.id(resourceCredit.id)
			.siteId(resourceCredit.siteId)
			.resourceTypeId(resourceCredit.resourceTypeId)
			.name(resourceCredit.name)
			.split(resourceCredit.split)
			.access(resourceCredit.access)
			.amount(resourceCredit.amount)
			.createTime(convertToZoneTime(resourceCredit.createTime, zoneId))
			.startTime(convertToZoneTime(resourceCredit.startTime, zoneId))
			.endTime(convertToZoneTime(resourceCredit.endTime, zoneId))
			.build();
	}

	static ResourceCredit map(ResourceCreditViewModel resourceCreditViewModel, ZoneId zoneId){
		return ResourceCredit.builder()
			.id(resourceCreditViewModel.id)
			.siteId(resourceCreditViewModel.siteId)
			.resourceTypeId(resourceCreditViewModel.resourceTypeId)
			.name(resourceCreditViewModel.name)
			.split(resourceCreditViewModel.split)
			.access(resourceCreditViewModel.access)
			.amount(resourceCreditViewModel.amount)
			.createTime(convertToUTCTime(resourceCreditViewModel.createTime))
			.startTime(convertToUTCTime(resourceCreditViewModel.startTime))
			.endTime(convertToUTCTime(resourceCreditViewModel.endTime))
			.build();
	}
}
