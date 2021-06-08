/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_usage;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Table("resource_usage_history")
public class ResourceUsageHistoryEntity extends UUIDIdentifiable {

	public final UUID projectId;
	public final UUID projectAllocationId;
	public final BigDecimal cumulativeConsumption;
	public final LocalDateTime probedAt;

	ResourceUsageHistoryEntity(UUID projectId, UUID projectAllocationId, BigDecimal cumulativeConsumption, LocalDateTime probedAt) {
		this.projectId = projectId;
		this.projectAllocationId = projectAllocationId;
		this.cumulativeConsumption = cumulativeConsumption;
		this.probedAt = probedAt;
	}

	public ResourceUsage toResourceUsage() {
		return ResourceUsage.builder()
			.projectId(projectId.toString())
			.projectAllocationId(projectAllocationId.toString())
			.cumulativeConsumption(cumulativeConsumption)
			.probedAt(probedAt)
			.build();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceUsageHistoryEntity that = (ResourceUsageHistoryEntity) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(projectId, that.projectId) &&
			Objects.equals(projectAllocationId, that.projectAllocationId) &&
			Objects.equals(cumulativeConsumption, that.cumulativeConsumption) &&
			Objects.equals(probedAt, that.probedAt);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, projectId, projectAllocationId, cumulativeConsumption, probedAt);
	}

	@Override
	public String toString() {
		return "ResourceUsageEntity{" +
			"id=" + id +
			", projectId=" + projectId +
			", projectAllocationId=" + projectAllocationId +
			", cumulativeConsumption=" + cumulativeConsumption +
			", probedAt=" + probedAt +
			'}';
	}

	public static ResourceUsageHistoryEntityBuilder builder() {
		return new ResourceUsageHistoryEntityBuilder();
	}

	public static final class ResourceUsageHistoryEntityBuilder {
		private UUID projectId;
		private UUID projectAllocationId;
		private BigDecimal cumulativeConsumption;
		private LocalDateTime probedAt;

		private ResourceUsageHistoryEntityBuilder() {
		}

		public ResourceUsageHistoryEntityBuilder projectId(UUID projectId) {
			this.projectId = projectId;
			return this;
		}

		public ResourceUsageHistoryEntityBuilder projectAllocationId(UUID projectAllocationId) {
			this.projectAllocationId = projectAllocationId;
			return this;
		}

		public ResourceUsageHistoryEntityBuilder cumulativeConsumption(BigDecimal cumulativeConsumption) {
			this.cumulativeConsumption = cumulativeConsumption;
			return this;
		}

		public ResourceUsageHistoryEntityBuilder probedAt(LocalDateTime probedAt) {
			this.probedAt = probedAt;
			return this;
		}

		public ResourceUsageHistoryEntity build() {
			return new ResourceUsageHistoryEntity(projectId, projectAllocationId, cumulativeConsumption, probedAt);
		}
	}
}