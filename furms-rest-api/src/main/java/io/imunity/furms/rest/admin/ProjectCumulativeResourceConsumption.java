/*
 * Copyright (c) 2020 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.rest.admin;

import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCZoned;

class ProjectCumulativeResourceConsumption {
	public final String projectAllocationId;
	public final String siteId;
	public final String resourceTypeId;
	public final BigDecimal consumedAmount;
	public final ZonedDateTime consumedUntil;

	public ProjectCumulativeResourceConsumption(String projectAllocationId, String siteId, String resourceTypeId,
	                                            BigDecimal consumedAmount, ZonedDateTime consumedUntil) {
		this.projectAllocationId = projectAllocationId;
		this.siteId = siteId;
		this.resourceTypeId = resourceTypeId;
		this.consumedAmount = consumedAmount;
		this.consumedUntil = consumedUntil;
	}

	public ProjectCumulativeResourceConsumption(ProjectAllocationResolved allocation) {
		this(allocation.id.id.toString(), allocation.site.getId().id.toString(), allocation.resourceType.id.id.toString(),
				allocation.consumed, convertToUTCZoned(allocation.resourceCredit.utcEndTime));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectCumulativeResourceConsumption that = (ProjectCumulativeResourceConsumption) o;
		return Objects.equals(projectAllocationId, that.projectAllocationId)
				&& Objects.equals(siteId, that.siteId)
				&& Objects.equals(resourceTypeId, that.resourceTypeId)
				&& Objects.equals(consumedAmount, that.consumedAmount)
				&& Objects.equals(consumedUntil, that.consumedUntil);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectAllocationId, siteId, resourceTypeId, consumedAmount, consumedUntil);
	}

	@Override
	public String toString() {
		return "ProjectCumulativeResourceConsumption{" +
				"projectAllocationId='" + projectAllocationId + '\'' +
				", siteId='" + siteId + '\'' +
				", resourceTypeId='" + resourceTypeId + '\'' +
				", consumedAmount=" + consumedAmount +
				", consumedUntil=" + consumedUntil +
				'}';
	}
}
