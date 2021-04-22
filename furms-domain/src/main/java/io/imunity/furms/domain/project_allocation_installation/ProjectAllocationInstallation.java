/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_allocation_installation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class ProjectAllocationInstallation {
	public final String id;
	public final String correlationId;
	public final String siteId;
	public final String projectAllocationId;
	public final String chunkId;
	public final BigDecimal amount;
	public final LocalDateTime validFrom;
	public final LocalDateTime validTo;
	public final LocalDateTime receivedTime;
	public final ProjectAllocationInstallationStatus status;

	ProjectAllocationInstallation(String id, String correlationId, String siteId, String projectAllocationId,
	                              String chunkId, BigDecimal amount, LocalDateTime validFrom, LocalDateTime validTo,
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectAllocationInstallation that = (ProjectAllocationInstallation) o;
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
		return "ProjectAllocationInstallation{" +
			"id='" + id + '\'' +
			", correlationId='" + correlationId + '\'' +
			", siteId='" + siteId + '\'' +
			", projectAllocationId='" + projectAllocationId + '\'' +
			", chunkId='" + chunkId + '\'' +
			", amount=" + amount +
			", validFrom=" + validFrom +
			", validTo=" + validTo +
			", receivedTime=" + receivedTime +
			", status=" + status +
			'}';
	}

	public static ProjectAllocationInstallationBuilder builder() {
		return new ProjectAllocationInstallationBuilder();
	}

	public static final class ProjectAllocationInstallationBuilder {
		public String id;
		public String correlationId;
		public String siteId;
		public String projectAllocationId;
		public String chunkId;
		public BigDecimal amount;
		public LocalDateTime validFrom;
		public LocalDateTime validTo;
		public LocalDateTime receivedTime;
		public ProjectAllocationInstallationStatus status;

		private ProjectAllocationInstallationBuilder() {
		}

		public ProjectAllocationInstallationBuilder id(String id) {
			this.id = id;
			return this;
		}

		public ProjectAllocationInstallationBuilder correlationId(String correlationId) {
			this.correlationId = correlationId;
			return this;
		}

		public ProjectAllocationInstallationBuilder siteId(String siteId) {
			this.siteId = siteId;
			return this;
		}

		public ProjectAllocationInstallationBuilder projectAllocationId(String projectAllocationId) {
			this.projectAllocationId = projectAllocationId;
			return this;
		}

		public ProjectAllocationInstallationBuilder chunkId(String chunkId) {
			this.chunkId = chunkId;
			return this;
		}

		public ProjectAllocationInstallationBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public ProjectAllocationInstallationBuilder validFrom(LocalDateTime validFrom) {
			this.validFrom = validFrom;
			return this;
		}

		public ProjectAllocationInstallationBuilder validTo(LocalDateTime validTo) {
			this.validTo = validTo;
			return this;
		}

		public ProjectAllocationInstallationBuilder receivedTime(LocalDateTime receivedTime) {
			this.receivedTime = receivedTime;
			return this;
		}

		public ProjectAllocationInstallationBuilder status(ProjectAllocationInstallationStatus status) {
			this.status = status;
			return this;
		}

		public ProjectAllocationInstallation build() {
			return new ProjectAllocationInstallation(id, correlationId, siteId, projectAllocationId, chunkId, amount, validFrom, validTo, receivedTime, status);
		}
	}
}
