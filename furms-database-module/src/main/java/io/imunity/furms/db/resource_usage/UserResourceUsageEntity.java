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

@Table("user_resource_usage")
public class UserResourceUsageEntity {

	@Id
	public final long id;
	public final UUID projectId;
	public final UUID projectAllocationId;
	public final String fenixUserId;
	public final BigDecimal cumulativeConsumption;
	public final LocalDateTime consumedUntil;

	UserResourceUsageEntity(long id, UUID projectId, UUID projectAllocationId, String fenixUserId, BigDecimal cumulativeConsumption, LocalDateTime consumedUntil) {
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
		UserResourceUsageEntity that = (UserResourceUsageEntity) o;
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

	public static UserResourceUsageEntityBuilder builder() {
		return new UserResourceUsageEntityBuilder();
	}

	public static final class UserResourceUsageEntityBuilder {
		private long id;
		private UUID projectId;
		private UUID projectAllocationId;
		private String fenixUserId;
		private BigDecimal cumulativeConsumption;
		private LocalDateTime consumedUntil;

		private UserResourceUsageEntityBuilder() {
		}

		public UserResourceUsageEntityBuilder id(long id) {
			this.id = id;
			return this;
		}

		public UserResourceUsageEntityBuilder projectId(UUID projectId) {
			this.projectId = projectId;
			return this;
		}

		public UserResourceUsageEntityBuilder projectAllocationId(UUID projectAllocationId) {
			this.projectAllocationId = projectAllocationId;
			return this;
		}

		public UserResourceUsageEntityBuilder fenixUserId(String fenixUserId) {
			this.fenixUserId = fenixUserId;
			return this;
		}

		public UserResourceUsageEntityBuilder cumulativeConsumption(BigDecimal cumulativeConsumption) {
			this.cumulativeConsumption = cumulativeConsumption;
			return this;
		}

		public UserResourceUsageEntityBuilder consumedUntil(LocalDateTime consumedUntil) {
			this.consumedUntil = consumedUntil;
			return this;
		}

		public UserResourceUsageEntity build() {
			return new UserResourceUsageEntity(id, projectId, projectAllocationId, fenixUserId, cumulativeConsumption, consumedUntil);
		}
	}
}
