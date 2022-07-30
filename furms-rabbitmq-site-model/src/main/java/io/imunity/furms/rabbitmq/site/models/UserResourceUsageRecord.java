/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

@JsonTypeName("UserResourceUsageRecord")
public class UserResourceUsageRecord implements Body {
	@NotEmpty
	public final String projectIdentifier;
	@NotEmpty
	public final String allocationIdentifier;
	@NotEmpty
	public final String fenixUserId;
	@NotNull
	public final BigDecimal cumulativeConsumption;
	@NotNull
	public final OffsetDateTime consumedUntil;

	@JsonCreator
	public UserResourceUsageRecord(String projectIdentifier, String allocationIdentifier, String fenixUserId, BigDecimal cumulativeConsumption, OffsetDateTime consumedUntil) {
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
		return Objects.equals(projectIdentifier, that.projectIdentifier) &&
			Objects.equals(allocationIdentifier, that.allocationIdentifier) &&
			Objects.equals(fenixUserId, that.fenixUserId) &&
			Objects.equals(cumulativeConsumption, that.cumulativeConsumption) &&
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
