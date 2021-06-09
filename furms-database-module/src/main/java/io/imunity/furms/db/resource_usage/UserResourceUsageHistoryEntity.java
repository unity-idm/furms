/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_usage;

import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import io.imunity.furms.domain.users.FenixUserId;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Table("user_resource_usage_history")
public class UserResourceUsageHistoryEntity {
	@Id
	public final long id;
	public final UUID projectId;
	public final UUID projectAllocationId;
	public final String fenixUserId;
	public final BigDecimal cumulativeConsumption;
	public final LocalDateTime consumedUntil;

	UserResourceUsageHistoryEntity(long id, UUID projectId, UUID projectAllocationId, String fenixUserId, BigDecimal cumulativeConsumption, LocalDateTime consumedUntil) {
		this.id = id;
		this.projectId = projectId;
		this.projectAllocationId = projectAllocationId;
		this.fenixUserId = fenixUserId;
		this.cumulativeConsumption = cumulativeConsumption;
		this.consumedUntil = consumedUntil;
	}

	public UserResourceUsage toUserResourceUsage() {
		return UserResourceUsage.builder()
			.projectId(projectId.toString())
			.projectAllocationId(projectAllocationId.toString())
			.fenixUserId(new FenixUserId(fenixUserId))
			.cumulativeConsumption(cumulativeConsumption)
			.consumedUntil(consumedUntil)
			.build();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserResourceUsageHistoryEntity that = (UserResourceUsageHistoryEntity) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(projectId, that.projectId) &&
			Objects.equals(projectAllocationId, that.projectAllocationId) &&
			Objects.equals(fenixUserId, that.fenixUserId) &&
			Objects.equals(cumulativeConsumption, that.cumulativeConsumption) &&
			Objects.equals(consumedUntil, that.consumedUntil);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, projectId, projectAllocationId, fenixUserId, cumulativeConsumption, consumedUntil);
	}

	@Override
	public String toString() {
		return "UserResourceUsageEntity{" +
			"id=" + id +
			", projectId=" + projectId +
			", projectAllocationId=" + projectAllocationId +
			", fenixUserId='" + fenixUserId + '\'' +
			", cumulativeConsumption=" + cumulativeConsumption +
			", consumedUntil=" + consumedUntil +
			'}';
	}

	public static UserResourceUsageHistoryEntityBuilder builder() {
		return new UserResourceUsageHistoryEntityBuilder();
	}

	public static final class UserResourceUsageHistoryEntityBuilder {
		private UUID projectId;
		private UUID projectAllocationId;
		private String fenixUserId;
		private BigDecimal cumulativeConsumption;
		private LocalDateTime consumedUntil;

		private UserResourceUsageHistoryEntityBuilder() {
		}

		public UserResourceUsageHistoryEntityBuilder projectId(UUID projectId) {
			this.projectId = projectId;
			return this;
		}

		public UserResourceUsageHistoryEntityBuilder projectAllocationId(UUID projectAllocationId) {
			this.projectAllocationId = projectAllocationId;
			return this;
		}

		public UserResourceUsageHistoryEntityBuilder fenixUserId(String fenixUserId) {
			this.fenixUserId = fenixUserId;
			return this;
		}

		public UserResourceUsageHistoryEntityBuilder cumulativeConsumption(BigDecimal cumulativeConsumption) {
			this.cumulativeConsumption = cumulativeConsumption;
			return this;
		}

		public UserResourceUsageHistoryEntityBuilder consumedUntil(LocalDateTime consumedUntil) {
			this.consumedUntil = consumedUntil;
			return this;
		}

		public UserResourceUsageHistoryEntity build() {
			return new UserResourceUsageHistoryEntity(0, projectId, projectAllocationId, fenixUserId, cumulativeConsumption, consumedUntil);
		}
	}
}
