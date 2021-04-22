/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.resource_credits;


import io.imunity.furms.domain.resource_credits.ResourceCredit;

import java.time.ZoneId;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;
import static io.imunity.furms.utils.UTCTimeUtils.convertToZoneTime;

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
			.createTime(convertToZoneTime(resourceCredit.utcCreateTime, zoneId))
			.startTime(convertToZoneTime(resourceCredit.utcStartTime, zoneId))
			.endTime(convertToZoneTime(resourceCredit.utcEndTime, zoneId))
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
			.utcCreateTime(convertToUTCTime(resourceCreditViewModel.createTime))
			.utcStartTime(convertToUTCTime(resourceCreditViewModel.startTime))
			.utcEndTime(convertToUTCTime(resourceCreditViewModel.endTime))
			.build();
	}
}
