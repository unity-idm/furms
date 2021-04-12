/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_installation;

import io.imunity.furms.domain.site_agent.CorrelationId;

import java.util.Objects;

public class ProjectInstallationJob {
	public final String id;
	public final CorrelationId correlationId;
	public final ProjectInstallationStatus status;

	public ProjectInstallationJob(String id, CorrelationId correlationId, ProjectInstallationStatus status) {
		this.id = id;
		this.correlationId = correlationId;
		this.status = status;
	}

	public ProjectInstallationJob(CorrelationId correlationId, ProjectInstallationStatus status) {
		this(null, correlationId, status);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectInstallationJob that = (ProjectInstallationJob) o;
		return Objects.equals(id, that.id) && Objects.equals(correlationId, that.correlationId) && status == that.status;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, correlationId, status);
	}

	@Override
	public String toString() {
		return "ProjectInstallationJob{" +
			"id='" + id + '\'' +
			", correlationId='" + correlationId + '\'' +
			", status=" + status +
			'}';
	}
}
