/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_allocation_installation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class ProjectAllocationChunk {
	public final String id;
	public final String projectAllocationId;
	public final String chunkId;
	public final BigDecimal amount;
	public final LocalDateTime validFrom;
	public final LocalDateTime validTo;
	public final LocalDateTime receivedTime;

	ProjectAllocationChunk(String id, String projectAllocationId, String chunkId, BigDecimal amount,
	                       LocalDateTime validFrom, LocalDateTime validTo, LocalDateTime receivedTime) {
		this.id = id;
		this.projectAllocationId = projectAllocationId;
		this.chunkId = chunkId;
		this.amount = amount;
		this.validFrom = validFrom;
		this.validTo = validTo;
		this.receivedTime = receivedTime;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectAllocationChunk that = (ProjectAllocationChunk) o;
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
		return "ProjectAllocationChunk{" +
			"id='" + id + '\'' +
			", projectAllocationId='" + projectAllocationId + '\'' +
			", chunkId='" + chunkId + '\'' +
			", amount=" + amount +
			", validFrom=" + validFrom +
			", validTo=" + validTo +
			", receivedTime=" + receivedTime +
			'}';
	}

	public static ProjectAllocationChunkBuilder builder() {
		return new ProjectAllocationChunkBuilder();
	}

	public static final class ProjectAllocationChunkBuilder {
		public String id;
		public String projectAllocationId;
		public String chunkId;
		public BigDecimal amount;
		public LocalDateTime validFrom;
		public LocalDateTime validTo;
		public LocalDateTime receivedTime;

		private ProjectAllocationChunkBuilder() {
		}

		public ProjectAllocationChunkBuilder id(String id) {
			this.id = id;
			return this;
		}

		public ProjectAllocationChunkBuilder projectAllocationId(String projectAllocationId) {
			this.projectAllocationId = projectAllocationId;
			return this;
		}

		public ProjectAllocationChunkBuilder chunkId(String chunkId) {
			this.chunkId = chunkId;
			return this;
		}

		public ProjectAllocationChunkBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public ProjectAllocationChunkBuilder validFrom(LocalDateTime validFrom) {
			this.validFrom = validFrom;
			return this;
		}

		public ProjectAllocationChunkBuilder validTo(LocalDateTime validTo) {
			this.validTo = validTo;
			return this;
		}

		public ProjectAllocationChunkBuilder receivedTime(LocalDateTime receivedTime) {
			this.receivedTime = receivedTime;
			return this;
		}

		public ProjectAllocationChunk build() {
			return new ProjectAllocationChunk(id, projectAllocationId, chunkId, amount, validFrom, validTo, receivedTime);
		}
	}
}
