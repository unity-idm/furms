/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_usage;

import io.imunity.furms.domain.users.FenixUserId;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;

public class UserProjectAllocationUsage {
	public final String projectId;
	public final String projectAllocationId;
	public final FenixUserId fenixUserId;
	public final BigDecimal cumulativeConsumption;
	public final ZonedDateTime consumedUntil;

	UserProjectAllocationUsage(String projectId, String projectAllocationId, FenixUserId fenixUserId, BigDecimal cumulativeConsumption, ZonedDateTime consumedUntil) {
		this.projectId = projectId;
		this.projectAllocationId = projectAllocationId;
		this.fenixUserId = fenixUserId;
		this.cumulativeConsumption = cumulativeConsumption;
		this.consumedUntil = consumedUntil;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserProjectAllocationUsage that = (UserProjectAllocationUsage) o;
		return Objects.equals(projectId, that.projectId) &&
			Objects.equals(projectAllocationId, that.projectAllocationId) &&
			Objects.equals(fenixUserId, that.fenixUserId) &&
			Objects.equals(cumulativeConsumption, that.cumulativeConsumption) &&
			Objects.equals(consumedUntil, that.consumedUntil);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectId, projectAllocationId, fenixUserId, cumulativeConsumption, consumedUntil);
	}

	@Override
	public String toString() {
		return "UserProjectAllocationUsage{" +
			"projectId='" + projectId + '\'' +
			", projectAllocationId='" + projectAllocationId + '\'' +
			", fenixUserId=" + fenixUserId +
			", cumulativeConsumption=" + cumulativeConsumption +
			", consumedUntil=" + consumedUntil +
			'}';
	}

	public static UserProjectAllocationUsageBuilder builder() {
		return new UserProjectAllocationUsageBuilder();
	}

	public static final class UserProjectAllocationUsageBuilder {
		private String projectId;
		private String projectAllocationId;
		private FenixUserId fenixUserId;
		private BigDecimal cumulativeConsumption;
		private ZonedDateTime consumedUntil;

		private UserProjectAllocationUsageBuilder() {
		}

		public UserProjectAllocationUsageBuilder projectId(String projectId) {
			this.projectId = projectId;
			return this;
		}

		public UserProjectAllocationUsageBuilder projectAllocationId(String projectAllocationId) {
			this.projectAllocationId = projectAllocationId;
			return this;
		}

		public UserProjectAllocationUsageBuilder fenixUserId(FenixUserId fenixUserId) {
			this.fenixUserId = fenixUserId;
			return this;
		}

		public UserProjectAllocationUsageBuilder cumulativeConsumption(BigDecimal cumulativeConsumption) {
			this.cumulativeConsumption = cumulativeConsumption;
			return this;
		}

		public UserProjectAllocationUsageBuilder consumedUntil(ZonedDateTime consumedUntil) {
			this.consumedUntil = consumedUntil;
			return this;
		}

		public UserProjectAllocationUsage build() {
			return new UserProjectAllocationUsage(projectId, projectAllocationId, fenixUserId, cumulativeConsumption, consumedUntil);
		}
	}
}
