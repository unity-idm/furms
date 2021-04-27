/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_allocation_installation;

import java.time.LocalDateTime;
import java.util.Objects;

public class ProjectAllocationInstallationResult {
	public final String projectIdentifier;
	public final String allocationIdentifier;
	public final String allocationChunkIdentifier;
	public final String resourceType;
	public final double amount;
	public final LocalDateTime validFrom;
	public final LocalDateTime validTo;

	ProjectAllocationInstallationResult(String projectIdentifier, String allocationIdentifier, String allocationChunkIdentifier, String resourceType, double amount, LocalDateTime validFrom, LocalDateTime validTo) {
		this.projectIdentifier = projectIdentifier;
		this.allocationIdentifier = allocationIdentifier;
		this.allocationChunkIdentifier = allocationChunkIdentifier;
		this.resourceType = resourceType;
		this.amount = amount;
		this.validFrom = validFrom;
		this.validTo = validTo;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectAllocationInstallationResult that = (ProjectAllocationInstallationResult) o;
		return Double.compare(that.amount, amount) == 0 &&
			Objects.equals(projectIdentifier, that.projectIdentifier) &&
			Objects.equals(allocationIdentifier, that.allocationIdentifier) &&
			Objects.equals(allocationChunkIdentifier, that.allocationChunkIdentifier) &&
			Objects.equals(resourceType, that.resourceType) &&
			Objects.equals(validFrom, that.validFrom) &&
			Objects.equals(validTo, that.validTo);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectIdentifier, allocationIdentifier, allocationChunkIdentifier, resourceType, amount, validFrom, validTo);
	}

	@Override
	public String toString() {
		return "ProjectAllocationInstallationResult{" +
			"projectIdentifier='" + projectIdentifier + '\'' +
			", allocationIdentifier='" + allocationIdentifier + '\'' +
			", allocationChunkIdentifier='" + allocationChunkIdentifier + '\'' +
			", resourceType='" + resourceType + '\'' +
			", amount=" + amount +
			", validFrom=" + validFrom +
			", validTo=" + validTo +
			'}';
	}

	public static ProjectAllocationInstallationResultBuilder builder() {
		return new ProjectAllocationInstallationResultBuilder();
	}

	public static final class ProjectAllocationInstallationResultBuilder {
		public String projectIdentifier;
		public String allocationIdentifier;
		public String allocationChunkIdentifier;
		public String resourceType;
		public double amount;
		public LocalDateTime validFrom;
		public LocalDateTime validTo;

		private ProjectAllocationInstallationResultBuilder() {
		}

		public ProjectAllocationInstallationResultBuilder projectIdentifier(String projectIdentifier) {
			this.projectIdentifier = projectIdentifier;
			return this;
		}

		public ProjectAllocationInstallationResultBuilder allocationIdentifier(String allocationIdentifier) {
			this.allocationIdentifier = allocationIdentifier;
			return this;
		}

		public ProjectAllocationInstallationResultBuilder allocationChunkIdentifier(String allocationChunkIdentifier) {
			this.allocationChunkIdentifier = allocationChunkIdentifier;
			return this;
		}

		public ProjectAllocationInstallationResultBuilder resourceType(String resourceType) {
			this.resourceType = resourceType;
			return this;
		}

		public ProjectAllocationInstallationResultBuilder amount(double amount) {
			this.amount = amount;
			return this;
		}

		public ProjectAllocationInstallationResultBuilder validFrom(LocalDateTime validFrom) {
			this.validFrom = validFrom;
			return this;
		}

		public ProjectAllocationInstallationResultBuilder validTo(LocalDateTime validTo) {
			this.validTo = validTo;
			return this;
		}

		public ProjectAllocationInstallationResult build() {
			return new ProjectAllocationInstallationResult(projectIdentifier, allocationIdentifier, allocationChunkIdentifier, resourceType, amount, validFrom, validTo);
		}
	}
}
