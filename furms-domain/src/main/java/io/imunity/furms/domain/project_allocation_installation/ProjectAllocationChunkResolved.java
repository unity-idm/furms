/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_allocation_installation;

import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class ProjectAllocationChunkResolved {
	public final ProjectAllocationInstallationId id;
	public final ChunkId chunkId;
	public final ProjectAllocationResolved projectAllocation;
	public final BigDecimal amount;
	public final LocalDateTime validFrom;
	public final LocalDateTime validTo;
	public final LocalDateTime receivedTime;

	public ProjectAllocationChunkResolved(ProjectAllocationInstallationId id,
	                                      ChunkId chunkId,
	                                      ProjectAllocationResolved projectAllocation,
	                                      BigDecimal amount,
	                                      LocalDateTime validFrom,
	                                      LocalDateTime validTo,
	                                      LocalDateTime receivedTime) {
		this.id = id;
		this.chunkId = chunkId;
		this.projectAllocation = projectAllocation;
		this.amount = amount;
		this.validFrom = validFrom;
		this.validTo = validTo;
		this.receivedTime = receivedTime;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectAllocationChunkResolved that = (ProjectAllocationChunkResolved) o;
		return Objects.equals(id, that.id)
				&& Objects.equals(chunkId, that.chunkId)
				&& Objects.equals(projectAllocation, that.projectAllocation)
				&& Objects.equals(amount, that.amount)
				&& Objects.equals(validFrom, that.validFrom)
				&& Objects.equals(validTo, that.validTo)
				&& Objects.equals(receivedTime, that.receivedTime);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, chunkId, projectAllocation, amount, validFrom, validTo, receivedTime);
	}

	@Override
	public String toString() {
		return "ProjectAllocationChunkResolved{" +
				"id='" + id + '\'' +
				", chunkId='" + chunkId + '\'' +
				", projectAllocation='" + projectAllocation + '\'' +
				", amount=" + amount +
				", validFrom=" + validFrom +
				", validTo=" + validTo +
				", receivedTime=" + receivedTime +
				'}';
	}

	public static ProjectAllocationChunkResolvedBuilder builder() {
		return new ProjectAllocationChunkResolvedBuilder();
	}

	public static final class ProjectAllocationChunkResolvedBuilder {
		public ProjectAllocationInstallationId id;
		public ChunkId chunkId;
		public ProjectAllocationResolved projectAllocation;
		public BigDecimal amount;
		public LocalDateTime validFrom;
		public LocalDateTime validTo;
		public LocalDateTime receivedTime;

		private ProjectAllocationChunkResolvedBuilder() {
		}

		public ProjectAllocationChunkResolvedBuilder id(ProjectAllocationInstallationId id) {
			this.id = id;
			return this;
		}

		public ProjectAllocationChunkResolvedBuilder chunkId(ChunkId chunkId) {
			this.chunkId = chunkId;
			return this;
		}

		public ProjectAllocationChunkResolvedBuilder projectAllocation(ProjectAllocationResolved projectAllocation) {
			this.projectAllocation = projectAllocation;
			return this;
		}

		public ProjectAllocationChunkResolvedBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public ProjectAllocationChunkResolvedBuilder validFrom(LocalDateTime validFrom) {
			this.validFrom = validFrom;
			return this;
		}

		public ProjectAllocationChunkResolvedBuilder validTo(LocalDateTime validTo) {
			this.validTo = validTo;
			return this;
		}

		public ProjectAllocationChunkResolvedBuilder receivedTime(LocalDateTime receivedTime) {
			this.receivedTime = receivedTime;
			return this;
		}

		public ProjectAllocationChunkResolved build() {
			return new ProjectAllocationChunkResolved(id, chunkId, projectAllocation, amount, validFrom, validTo, receivedTime);
		}
	}
}
