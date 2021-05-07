/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_installation;

import java.util.Map;
import java.util.Objects;

public class ProjectInstallationResult {
	public final Map<String, String> attributes;
	public final ProjectInstallationStatus status;
	public final Error error;

	public ProjectInstallationResult(Map<String, String> attributes, ProjectInstallationStatus status, Error error) {
		this.attributes = attributes;
		this.status = status;
		this.error = error;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectInstallationResult that = (ProjectInstallationResult) o;
		return Objects.equals(attributes, that.attributes) &&
			status == that.status &&
			Objects.equals(error, that.error);
	}

	@Override
	public int hashCode() {
		return Objects.hash(attributes, status, error);
	}

	@Override
	public String toString() {
		return "ProjectInstallationResult{" +
			"attributes=" + attributes +
			", status=" + status +
			", error=" + error +
			'}';
	}
}
