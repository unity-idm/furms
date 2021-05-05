/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_installation;

import java.util.Objects;

public class ProjectUpdateResult {
	public final ProjectUpdateStatus status;
	public final Error error;

	public ProjectUpdateResult(ProjectUpdateStatus status, Error error) {
		this.status = status;
		this.error = error;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectUpdateResult that = (ProjectUpdateResult) o;
		return status == that.status &&
			Objects.equals(error, that.error);
	}

	@Override
	public int hashCode() {
		return Objects.hash(status, error);
	}

	@Override
	public String toString() {
		return "ProjectUpdateResult{" +
			", status=" + status +
			", error=" + error +
			'}';
	}
}
