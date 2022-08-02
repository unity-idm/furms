/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

@JsonTypeName("CumulativeResourceUsageRecord")
public class CumulativeResourceUsageRecord implements Body {
	@NotEmpty
	public final String projectIdentifier;
	@NotEmpty
	public final String allocationIdentifier;
	@NotNull
	public final BigDecimal cumulativeConsumption;
	@NotNull
	public final OffsetDateTime probedAt;

	@JsonCreator
	public CumulativeResourceUsageRecord(String projectIdentifier, String allocationIdentifier,
	                                     @JsonProperty(required = true) BigDecimal cumulativeConsumption, OffsetDateTime probedAt) {
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
		return Objects.equals(projectIdentifier, that.projectIdentifier) &&
			Objects.equals(allocationIdentifier, that.allocationIdentifier) &&
			Objects.equals(cumulativeConsumption, that.cumulativeConsumption) &&
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
