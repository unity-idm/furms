/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_allocation_installation;

import io.imunity.furms.domain.site_agent.CorrelationId;

import java.util.Objects;
import java.util.Optional;

public class ProjectDeallocation {
	public final String id;
	public final CorrelationId correlationId;
	public final String siteId;
	public final String projectAllocationId;
	public final ProjectDeallocationStatus status;
	public final Optional<ErrorMessage> errorMessage;

	ProjectDeallocation(String id, CorrelationId correlationId, String siteId, String projectAllocationId, ProjectDeallocationStatus status, Optional<ErrorMessage> errorMessage) {
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
		ProjectDeallocation that = (ProjectDeallocation) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(correlationId, that.correlationId) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(projectAllocationId, that.projectAllocationId) &&
			Objects.equals(errorMessage, that.errorMessage) &&
			status == that.status;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, correlationId, siteId, projectAllocationId, status);
	}

	@Override
	public String toString() {
		return "ProjectDeallocationStatus{" +
			"id='" + id + '\'' +
			", correlationId='" + correlationId + '\'' +
			", siteId='" + siteId + '\'' +
			", projectAllocationId='" + projectAllocationId + '\'' +
			", status=" + status +
			", errorStatus=" + errorMessage +
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
		public ProjectDeallocationStatus status;
		public Optional<ErrorMessage> errorStatus = Optional.empty();

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

		public ProjectAllocationInstallationBuilder status(ProjectDeallocationStatus status) {
			this.status = status;
			return this;
		}

		public ProjectAllocationInstallationBuilder message(String code, String message) {
			if (code == null && message == null)
				return this;
			this.errorStatus = Optional.of(new ErrorMessage(code, message));
			return this;
		}

		public ProjectDeallocation build() {
			return new ProjectDeallocation(id, correlationId, siteId, projectAllocationId, status, errorStatus);
		}
	}
}
