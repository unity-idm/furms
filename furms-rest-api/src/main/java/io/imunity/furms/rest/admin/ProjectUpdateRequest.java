/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.util.Objects;

class ProjectUpdateRequest {
	public final String name;
	public final String description;
	public final Validity validity;
	public final String researchField;
	public final String projectLeaderId;
	
	ProjectUpdateRequest(String name, String description, Validity validity,
	                     String researchField, String projectLeaderId) {
		this.name = name;
		this.description = description;
		this.validity = validity;
		this.researchField = researchField;
		this.projectLeaderId = projectLeaderId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectUpdateRequest that = (ProjectUpdateRequest) o;
		return Objects.equals(name, that.name)
				&& Objects.equals(description, that.description)
				&& Objects.equals(validity, that.validity)
				&& Objects.equals(researchField, that.researchField)
				&& Objects.equals(projectLeaderId, that.projectLeaderId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, description, validity, researchField, projectLeaderId);
	}

	@Override
	public String toString() {
		return "ProjectUpdateRequest{" +
				"name='" + name + '\'' +
				", description='" + description + '\'' +
				", validity=" + validity +
				", researchField='" + researchField + '\'' +
				", projectLeaderId=" + projectLeaderId +
				'}';
	}
}
