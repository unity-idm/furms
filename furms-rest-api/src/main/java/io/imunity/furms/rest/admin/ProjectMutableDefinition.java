/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.util.Objects;

class ProjectMutableDefinition {
	public final String name;
	public final String description;
	public final Validity validity;
	public final String researchField;
	public final User projectLeader;
	
	ProjectMutableDefinition(String name, String description, Validity validity, 
			String researchField, User projectLeader) {
		this.name = name;
		this.description = description;
		this.validity = validity;
		this.researchField = researchField;
		this.projectLeader = projectLeader;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectMutableDefinition that = (ProjectMutableDefinition) o;
		return Objects.equals(name, that.name)
				&& Objects.equals(description, that.description)
				&& Objects.equals(validity, that.validity)
				&& Objects.equals(researchField, that.researchField)
				&& Objects.equals(projectLeader, that.projectLeader);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, description, validity, researchField, projectLeader);
	}

	@Override
	public String toString() {
		return "ProjectMutableDefinition{" +
				"name='" + name + '\'' +
				", description='" + description + '\'' +
				", validity=" + validity +
				", researchField='" + researchField + '\'' +
				", projectLeader=" + projectLeader +
				'}';
	}
}
