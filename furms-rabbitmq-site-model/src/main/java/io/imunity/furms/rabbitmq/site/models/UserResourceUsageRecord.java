/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.time.OffsetDateTime;
import java.util.Objects;

@JsonTypeName("UserResourceUsageRecord")
public class UserResourceUsageRecord implements Body {
	public final String projectIdentifier;
	public final String allocationIdentifier;
	public final String fenixUserId;
	public final double cumulativeConsumption;
	public final OffsetDateTime consumedUntil;

	@JsonCreator
	UserResourceUsageRecord(String projectIdentifier, String allocationIdentifier, String fenixUserId, double cumulativeConsumption, OffsetDateTime consumedUntil) {
		this.projectIdentifier = projectIdentifier;
		this.allocationIdentifier = allocationIdentifier;
		this.fenixUserId = fenixUserId;
		this.cumulativeConsumption = cumulativeConsumption;
		this.consumedUntil = consumedUntil;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserResourceUsageRecord that = (UserResourceUsageRecord) o;
		return Double.compare(that.cumulativeConsumption, cumulativeConsumption) == 0 &&
			Objects.equals(projectIdentifier, that.projectIdentifier) &&
			Objects.equals(allocationIdentifier, that.allocationIdentifier) &&
			Objects.equals(fenixUserId, that.fenixUserId) &&
			Objects.equals(consumedUntil, that.consumedUntil);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectIdentifier, allocationIdentifier, fenixUserId, cumulativeConsumption, consumedUntil);
	}

	@Override
	public String toString() {
		return "UserResourceUsageRecord{" +
			"projectIdentifier='" + projectIdentifier + '\'' +
			", allocationIdentifier='" + allocationIdentifier + '\'' +
			", fenixUserId='" + fenixUserId + '\'' +
			", cumulativeConsumption=" + cumulativeConsumption +
			", consumedUntil=" + consumedUntil +
			'}';
	}
}
