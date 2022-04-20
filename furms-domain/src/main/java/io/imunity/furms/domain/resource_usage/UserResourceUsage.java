/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_usage;

import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.users.FenixUserId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class UserResourceUsage {
	public final ProjectId projectId;
	public final ProjectAllocationId projectAllocationId;
	public final FenixUserId fenixUserId;
	public final BigDecimal cumulativeConsumption;
	public final LocalDateTime utcConsumedUntil;

	UserResourceUsage(ProjectId projectId, ProjectAllocationId projectAllocationId, FenixUserId fenixUserId, BigDecimal cumulativeConsumption, LocalDateTime utcConsumedUntil) {
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

	public static UserResourceUsageBuilder builder() {
		return new UserResourceUsageBuilder();
	}

	public static final class UserResourceUsageBuilder {
		private ProjectId projectId;
		private ProjectAllocationId projectAllocationId;
		private FenixUserId fenixUserId;
		private BigDecimal cumulativeConsumption;
		private LocalDateTime consumedUntil;

		private UserResourceUsageBuilder() {
		}

		public UserResourceUsageBuilder projectId(String projectId) {
			this.projectId = new ProjectId(projectId);
			return this;
		}

		public UserResourceUsageBuilder projectAllocationId(String projectAllocationId) {
			this.projectAllocationId = new ProjectAllocationId(projectAllocationId);
			return this;
		}

		public UserResourceUsageBuilder fenixUserId(FenixUserId fenixUserId) {
			this.fenixUserId = fenixUserId;
			return this;
		}

		public UserResourceUsageBuilder cumulativeConsumption(BigDecimal cumulativeConsumption) {
			this.cumulativeConsumption = cumulativeConsumption;
			return this;
		}

		public UserResourceUsageBuilder consumedUntil(LocalDateTime consumedUntil) {
			this.consumedUntil = consumedUntil;
			return this;
		}

		public UserResourceUsage build() {
			return new UserResourceUsage(projectId, projectAllocationId, fenixUserId, cumulativeConsumption, consumedUntil);
		}
	}
}
