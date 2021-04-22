/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_allocation_installation;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Table("project_allocation_installation")
class ProjectAllocationInstallationEntity extends UUIDIdentifiable {
	public final UUID correlationId;
	public final UUID siteId;
	public final UUID projectAllocationId;
	public final String chunkId;
	public final BigDecimal amount;
	public final LocalDateTime validFrom;
	public final LocalDateTime validTo;
	public final LocalDateTime receivedTime;
	public final ProjectAllocationInstallationStatus status;

	ProjectAllocationInstallationEntity(UUID id, UUID correlationId, UUID siteId, UUID projectAllocationId, String chunkId,
	                                    BigDecimal amount, LocalDateTime validFrom, LocalDateTime validTo,
	                                    LocalDateTime receivedTime, ProjectAllocationInstallationStatus status) {
		this.id = id;
		this.correlationId = correlationId;
		this.siteId = siteId;
		this.projectAllocationId = projectAllocationId;
		this.chunkId = chunkId;
		this.amount = amount;
		this.validFrom = validFrom;
		this.validTo = validTo;
		this.receivedTime = receivedTime;
		this.status = status;
	}

	ProjectAllocationInstallation toProjectAllocationInstallation() {
		return ProjectAllocationInstallation.builder()
			.id(id.toString())
			.correlationId(correlationId.toString())
			.siteId(siteId.toString())
			.projectAllocationId(projectAllocationId.toString())
			.chunkId(chunkId)
			.amount(amount)
			.validFrom(validFrom)
			.validTo(validTo)
			.receivedTime(receivedTime)
			.status(status)
			.build();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectAllocationInstallationEntity that = (ProjectAllocationInstallationEntity) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(correlationId, that.correlationId) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(projectAllocationId, that.projectAllocationId) &&
			Objects.equals(chunkId, that.chunkId) &&
			Objects.equals(amount, that.amount) &&
			Objects.equals(validFrom, that.validFrom) &&
			Objects.equals(validTo, that.validTo) &&
			Objects.equals(receivedTime, that.receivedTime) &&
			status == that.status;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, correlationId, siteId, projectAllocationId, chunkId, amount, validFrom, validTo, receivedTime, status);
	}

	@Override
	public String toString() {
		return "ProjectAllocationInstallationEntity{" +
			"id=" + id +
			", correlationId=" + correlationId +
			", siteId=" + siteId +
			", projectAllocationId=" + projectAllocationId +
			", chunkId='" + chunkId + '\'' +
			", amount=" + amount +
			", validFrom=" + validFrom +
			", validTo=" + validTo +
			", receivedTime=" + receivedTime +
			", status=" + status +
			'}';
	}

	public static ProjectAllocationInstallationEntityBuilder builder() {
		return new ProjectAllocationInstallationEntityBuilder();
	}

	public static final class ProjectAllocationInstallationEntityBuilder {
		public UUID correlationId;
		public UUID siteId;
		public UUID projectAllocationId;
		public String chunkId;
		public BigDecimal amount;
		public LocalDateTime validFrom;
		public LocalDateTime validTo;
		public LocalDateTime receivedTime;
		public ProjectAllocationInstallationStatus status;
		protected UUID id;

		private ProjectAllocationInstallationEntityBuilder() {
		}

		public ProjectAllocationInstallationEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public ProjectAllocationInstallationEntityBuilder correlationId(UUID correlationId) {
			this.correlationId = correlationId;
			return this;
		}

		public ProjectAllocationInstallationEntityBuilder siteId(UUID siteId) {
			this.siteId = siteId;
			return this;
		}

		public ProjectAllocationInstallationEntityBuilder projectAllocationId(UUID projectAllocationId) {
			this.projectAllocationId = projectAllocationId;
			return this;
		}

		public ProjectAllocationInstallationEntityBuilder chunkId(String chunkId) {
			this.chunkId = chunkId;
			return this;
		}

		public ProjectAllocationInstallationEntityBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public ProjectAllocationInstallationEntityBuilder validFrom(LocalDateTime validFrom) {
			this.validFrom = validFrom;
			return this;
		}

		public ProjectAllocationInstallationEntityBuilder validTo(LocalDateTime validTo) {
			this.validTo = validTo;
			return this;
		}

		public ProjectAllocationInstallationEntityBuilder receivedTime(LocalDateTime receivedTime) {
			this.receivedTime = receivedTime;
			return this;
		}

		public ProjectAllocationInstallationEntityBuilder status(ProjectAllocationInstallationStatus status) {
			this.status = status;
			return this;
		}

		public ProjectAllocationInstallationEntity build() {
			return new ProjectAllocationInstallationEntity(id, correlationId, siteId, projectAllocationId, chunkId, amount, validFrom, validTo, receivedTime, status);
		}
	}
}
