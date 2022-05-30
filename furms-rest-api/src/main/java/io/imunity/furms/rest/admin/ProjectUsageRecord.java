/*
 * Copyright (c) 2020 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.rest.admin;

import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCZoned;

class ProjectUsageRecord {
	public final String projectAllocationId;
	public final String resourceTypeId;
	public final String siteId;
	public final BigDecimal consumedAmount;
	public final String userFenixId;
	public final ZonedDateTime from;
	public final ZonedDateTime until;

	public ProjectUsageRecord(String projectAllocationId, String resourceTypeId, String siteId, BigDecimal consumedAmount,
	                          String userFenixId, ZonedDateTime from, ZonedDateTime until) {
		this.projectAllocationId = projectAllocationId;
		this.resourceTypeId = resourceTypeId;
		this.siteId = siteId;
		this.consumedAmount = consumedAmount;
		this.userFenixId = userFenixId;
		this.from = from;
		this.until = until;
	}

	public ProjectUsageRecord(UserResourceUsage userUsage, ProjectAllocationResolved allocation) {
		this(allocation.id.id.toString(), allocation.resourceType.id.id.toString(), allocation.site.getId().id.toString(),
			userUsage.cumulativeConsumption,
				userUsage.fenixUserId.id, convertToUTCZoned(allocation.resourceCredit.utcStartTime),
				convertToUTCZoned(allocation.resourceCredit.utcEndTime));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectUsageRecord that = (ProjectUsageRecord) o;
		return Objects.equals(projectAllocationId, that.projectAllocationId) && Objects.equals(resourceTypeId, that.resourceTypeId) && Objects.equals(siteId, that.siteId) && Objects.equals(consumedAmount, that.consumedAmount) && Objects.equals(userFenixId, that.userFenixId) && Objects.equals(from, that.from) && Objects.equals(until, that.until);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectAllocationId, resourceTypeId, siteId, consumedAmount, userFenixId, from, until);
	}

	@Override
	public String toString() {
		return "ProjectUsageRecord{" +
				"projectAllocationId='" + projectAllocationId + '\'' +
				", resourceTypeId='" + resourceTypeId + '\'' +
				", siteId='" + siteId + '\'' +
				", consumedAmount=" + consumedAmount +
				", userFenixId='" + userFenixId + '\'' +
				", from=" + from +
				", until=" + until +
				'}';
	}
}
