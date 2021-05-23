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
			.split(resourceCredit.splittable)
			.access(resourceCredit.accessibleForAllProjectMembers)
			.amount(resourceCredit.amount)
			.createTime(convertToZoneTime(resourceCredit.utcCreateTime, zoneId))
			.startTime(convertToZoneTime(resourceCredit.utcStartTime, zoneId))
			.endTime(convertToZoneTime(resourceCredit.utcEndTime, zoneId))
			.build();
	}

	static ResourceCredit map(ResourceCreditViewModel resourceCreditViewModel){
		return ResourceCredit.builder()
			.id(resourceCreditViewModel.getId())
			.siteId(resourceCreditViewModel.getSiteId())
			.resourceTypeId(resourceCreditViewModel.getResourceTypeId())
			.name(resourceCreditViewModel.getName())
			.splittable(resourceCreditViewModel.getSplit())
			.accessibleForAllProjectMembers(resourceCreditViewModel.getAccess())
			.amount(resourceCreditViewModel.getAmount())
			.utcCreateTime(convertToUTCTime(resourceCreditViewModel.getCreateTime()))
			.utcStartTime(convertToUTCTime(resourceCreditViewModel.getStartTime()))
			.utcEndTime(convertToUTCTime(resourceCreditViewModel.getEndTime()))
			.build();
	}
}
