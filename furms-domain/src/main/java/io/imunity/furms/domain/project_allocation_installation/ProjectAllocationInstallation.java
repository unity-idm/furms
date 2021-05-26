/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_allocation_installation;

import io.imunity.furms.domain.site_agent.CorrelationId;

import java.util.Objects;
import java.util.Optional;

public class ProjectAllocationInstallation {
	public final String id;
	public final CorrelationId correlationId;
	public final String siteId;
	public final String projectAllocationId;
	public final ProjectAllocationInstallationStatus status;
	public final Optional<ErrorMessage> errorMessage;

	ProjectAllocationInstallation(String id, CorrelationId correlationId, String siteId, String projectAllocationId,
	                              ProjectAllocationInstallationStatus status, Optional<ErrorMessage> errorMessage) {
		this.id = id;
		this.correlationId = correlationId;
		this.siteId = siteId;
		this.projectAllocationId = projectAllocationId;
		this.status = status;
		this.errorMessage = errorMessage;
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
			Objects.equals(errorMessage, that.errorMessage) &&
			status == that.status;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, correlationId, siteId, projectAllocationId, status, errorMessage);
	}

	@Override
	public String toString() {
		return "ProjectAllocationInstallation{" +
			"id='" + id + '\'' +
			", correlationId='" + correlationId + '\'' +
			", siteId='" + siteId + '\'' +
			", projectAllocationId='" + projectAllocationId + '\'' +
			", status=" + status +
			", errorMessage=" + errorMessage +
			'}';
	}

	public static ProjectAllocationInstallationBuilder builder() {
		return new ProjectAllocationInstallationBuilder();
	}

	public static final class ProjectAllocationInstallationBuilder {
		public String id;
		public CorrelationId correlationId;
		public String siteId;
		public String projectAllocationId;
		public ProjectAllocationInstallationStatus status;
		public Optional<ErrorMessage> errorMessage = Optional.empty();

		private ProjectAllocationInstallationBuilder() {
		}

		public ProjectAllocationInstallationBuilder id(String id) {
			this.id = id;
			return this;
		}

		public ProjectAllocationInstallationBuilder correlationId(CorrelationId correlationId) {
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

		public ProjectAllocationInstallationBuilder status(ProjectAllocationInstallationStatus status) {
			this.status = status;
			return this;
		}

		public ProjectAllocationInstallationBuilder errorMessage(String code, String message) {
			if(code == null && message == null)
				return this;
			this.errorMessage = Optional.of(new ErrorMessage(code, message));
			return this;
		}

		public ProjectAllocationInstallationBuilder errorMessage(Optional<ErrorMessage> errorMessage) {
			this.errorMessage = errorMessage;
			return this;
		}

		public ProjectAllocationInstallation build() {
			return new ProjectAllocationInstallation(id, correlationId, siteId, projectAllocationId, status, errorMessage);
		}
	}
}
