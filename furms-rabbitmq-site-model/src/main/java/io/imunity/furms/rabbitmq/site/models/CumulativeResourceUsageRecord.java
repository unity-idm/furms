/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.time.OffsetDateTime;
import java.util.Objects;

@JsonTypeName("CumulativeResourceUsageRecord")
public class CumulativeResourceUsageRecord implements Body {
	public final String projectIdentifier;
	public final String allocationIdentifier;
	public final double cumulativeConsumption;
	public final OffsetDateTime probedAt;

	@JsonCreator
	CumulativeResourceUsageRecord(String projectIdentifier, String allocationIdentifier, double cumulativeConsumption, OffsetDateTime probedAt) {
		this.projectIdentifier = projectIdentifier;
		this.allocationIdentifier = allocationIdentifier;
		this.cumulativeConsumption = cumulativeConsumption;
		this.probedAt = probedAt;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CumulativeResourceUsageRecord that = (CumulativeResourceUsageRecord) o;
		return Double.compare(that.cumulativeConsumption, cumulativeConsumption) == 0 &&
			Objects.equals(projectIdentifier, that.projectIdentifier) &&
			Objects.equals(allocationIdentifier, that.allocationIdentifier) &&
			Objects.equals(probedAt, that.probedAt);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectIdentifier, allocationIdentifier, cumulativeConsumption, probedAt);
	}

	@Override
	public String toString() {
		return "CumulativeResourceUsageRecord{" +
			"projectIdentifier='" + projectIdentifier + '\'' +
			", allocationIdentifier='" + allocationIdentifier + '\'' +
			", cumulativeConsumption=" + cumulativeConsumption +
			", probedAt=" + probedAt +
			'}';
	}
}
