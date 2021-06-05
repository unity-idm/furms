/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_usage;

import io.imunity.furms.domain.users.FenixUserId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class UserResourceUsage {
	public final String projectId;
	public final String projectAllocationId;
	public final FenixUserId fenixUserId;
	public final BigDecimal cumulativeConsumption;
	public final LocalDateTime utcConsumedUntil;

	UserResourceUsage(String projectId, String projectAllocationId, FenixUserId fenixUserId, BigDecimal cumulativeConsumption, LocalDateTime utcConsumedUntil) {
		this.projectId = projectId;
		this.projectAllocationId = projectAllocationId;
		this.fenixUserId = fenixUserId;
		this.cumulativeConsumption = cumulativeConsumption;
		this.utcConsumedUntil = utcConsumedUntil;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserResourceUsage that = (UserResourceUsage) o;
		return Objects.equals(projectId, that.projectId) &&
			Objects.equals(projectAllocationId, that.projectAllocationId) &&
			Objects.equals(fenixUserId, that.fenixUserId) &&
			Objects.equals(cumulativeConsumption, that.cumulativeConsumption) &&
			Objects.equals(utcConsumedUntil, that.utcConsumedUntil);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectId, projectAllocationId, fenixUserId, cumulativeConsumption, utcConsumedUntil);
	}

	@Override
	public String toString() {
		return "UserProjectAllocationUsage{" +
			"projectId='" + projectId + '\'' +
			", projectAllocationId='" + projectAllocationId + '\'' +
			", fenixUserId=" + fenixUserId +
			", cumulativeConsumption=" + cumulativeConsumption +
			", consumedUntil=" + utcConsumedUntil +
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
		private LocalDateTime consumedUntil;

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

		public UserProjectAllocationUsageBuilder consumedUntil(LocalDateTime consumedUntil) {
			this.consumedUntil = consumedUntil;
			return this;
		}

		public UserResourceUsage build() {
			return new UserResourceUsage(projectId, projectAllocationId, fenixUserId, cumulativeConsumption, consumedUntil);
		}
	}
}
