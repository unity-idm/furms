/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.resource_credits;


import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_credits.ResourceCreditWithAllocations;

import java.time.ZoneId;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;
import static io.imunity.furms.utils.UTCTimeUtils.convertToZoneTime;

class ResourceCreditViewModelMapper {
	static ResourceCreditViewModel map(ResourceCreditWithAllocations resourceCredit, ZoneId zoneId) {
		return ResourceCreditViewModel.builder()
			.id(resourceCredit.getId())
			.siteId(resourceCredit.getSiteId())
			.resourceTypeId(resourceCredit.getResourceType().id)
			.resourceTypeName(resourceCredit.getResourceType().name)
			.name(resourceCredit.getName())
			.split(resourceCredit.getSplit())
			.amount(resourceCredit.getAmount())
			.remaining(resourceCredit.getRemaining())
			.consumed(resourceCredit.getConsumed())
			.unit(resourceCredit.getResourceType().unit)
			.createTime(convertToZoneTime(resourceCredit.getUtcCreateTime(), zoneId))
			.startTime(convertToZoneTime(resourceCredit.getUtcStartTime(), zoneId))
			.endTime(convertToZoneTime(resourceCredit.getUtcEndTime(), zoneId))
			.build();
	}

	static ResourceCredit map(ResourceCreditViewModel resourceCreditViewModel){
		return ResourceCredit.builder()
			.id(resourceCreditViewModel.getId())
			.siteId(resourceCreditViewModel.getSiteId())
			.resourceTypeId(resourceCreditViewModel.getResourceTypeId())
			.name(resourceCreditViewModel.getName())
			.splittable(resourceCreditViewModel.getSplit())
			.amount(resourceCreditViewModel.getAmount().amount)
			.utcCreateTime(convertToUTCTime(resourceCreditViewModel.getCreateTime()))
			.utcStartTime(convertToUTCTime(resourceCreditViewModel.getStartTime()))
			.utcEndTime(convertToUTCTime(resourceCreditViewModel.getEndTime()))
			.build();
	}
}
