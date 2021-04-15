/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_installation;

import java.util.Map;
import java.util.Objects;

public class ProjectInstallationResult {
	public final String identifier;
	public final Map<String, String> attributes;

	public ProjectInstallationResult(String identifier, Map<String, String> attributes) {
		this.identifier = identifier;
		this.attributes = attributes;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectInstallationResult that = (ProjectInstallationResult) o;
		return Objects.equals(identifier, that.identifier) && Objects.equals(attributes, that.attributes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(identifier, attributes);
	}

	@Override
	public String toString() {
		return "ProjectInstallationResult{" +
			"identifier='" + identifier + '\'' +
			", attributes=" + attributes +
			'}';
	}
}
