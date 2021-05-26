/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_allocation_installation;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Table("project_allocation_chunk")
class ProjectAllocationChunkEntity extends UUIDIdentifiable {
	public final UUID projectAllocationId;
	public final String chunkId;
	public final BigDecimal amount;
	public final LocalDateTime validFrom;
	public final LocalDateTime validTo;
	public final LocalDateTime receivedTime;

	ProjectAllocationChunkEntity(UUID id, UUID projectAllocationId, String chunkId,
	                             BigDecimal amount, LocalDateTime validFrom, LocalDateTime validTo,
	                             LocalDateTime receivedTime) {
		this.id = id;
		this.projectAllocationId = projectAllocationId;
		this.chunkId = chunkId;
		this.amount = amount;
		this.validFrom = validFrom;
		this.validTo = validTo;
		this.receivedTime = receivedTime;
	}

	ProjectAllocationChunk toProjectAllocationChunk() {
		return ProjectAllocationChunk.builder()
			.id(id.toString())
			.projectAllocationId(projectAllocationId.toString())
			.chunkId(chunkId)
			.amount(amount)
			.validFrom(validFrom)
			.validTo(validTo)
			.receivedTime(receivedTime)
			.build();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectAllocationChunkEntity that = (ProjectAllocationChunkEntity) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(projectAllocationId, that.projectAllocationId) &&
			Objects.equals(chunkId, that.chunkId) &&
			Objects.equals(amount, that.amount) &&
			Objects.equals(validFrom, that.validFrom) &&
			Objects.equals(validTo, that.validTo) &&
			Objects.equals(receivedTime, that.receivedTime);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, projectAllocationId, chunkId, amount, validFrom, validTo, receivedTime);
	}

	@Override
	public String toString() {
		return "ProjectAllocationChunkEntity{" +
			"id=" + id +
			", projectAllocationId=" + projectAllocationId +
			", chunkId='" + chunkId + '\'' +
			", amount=" + amount +
			", validFrom=" + validFrom +
			", validTo=" + validTo +
			", receivedTime=" + receivedTime +
			'}';
	}

	public static ProjectAllocationChunkEntityBuilder builder() {
		return new ProjectAllocationChunkEntityBuilder();
	}

	public static final class ProjectAllocationChunkEntityBuilder {
		protected UUID id;
		public UUID projectAllocationId;
		public String chunkId;
		public BigDecimal amount;
		public LocalDateTime validFrom;
		public LocalDateTime validTo;
		public LocalDateTime receivedTime;

		private ProjectAllocationChunkEntityBuilder() {
		}

		public ProjectAllocationChunkEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public ProjectAllocationChunkEntityBuilder projectAllocationId(UUID projectAllocationId) {
			this.projectAllocationId = projectAllocationId;
			return this;
		}

		public ProjectAllocationChunkEntityBuilder chunkId(String chunkId) {
			this.chunkId = chunkId;
			return this;
		}

		public ProjectAllocationChunkEntityBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public ProjectAllocationChunkEntityBuilder validFrom(LocalDateTime validFrom) {
			this.validFrom = validFrom;
			return this;
		}

		public ProjectAllocationChunkEntityBuilder validTo(LocalDateTime validTo) {
			this.validTo = validTo;
			return this;
		}

		public ProjectAllocationChunkEntityBuilder receivedTime(LocalDateTime receivedTime) {
			this.receivedTime = receivedTime;
			return this;
		}

		public ProjectAllocationChunkEntity build() {
			return new ProjectAllocationChunkEntity(id, projectAllocationId, chunkId, amount, validFrom, validTo, receivedTime);
		}
	}
}
