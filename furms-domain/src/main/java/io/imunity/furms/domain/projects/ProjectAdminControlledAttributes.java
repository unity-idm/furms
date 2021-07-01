/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.projects;

import java.util.Objects;

import io.imunity.furms.domain.images.FurmsImage;

public class ProjectAdminControlledAttributes {
	private final String id;
	private final String description;
	private final String researchField;
	private final FurmsImage logo;

	public ProjectAdminControlledAttributes(String id, String description, String researchField, FurmsImage logo) {
		this.id = id;
		this.description = description;
		this.researchField = researchField;
		this.logo = logo;
	}

	public String getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public String getResearchField() {
		return researchField;
	}

	public FurmsImage getLogo() {
		return logo;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectAdminControlledAttributes that = (ProjectAdminControlledAttributes) o;
		return Objects.equals(id, that.id)
				&& Objects.equals(description, that.description)
				&& Objects.equals(researchField, that.researchField)
				&& Objects.equals(logo, that.logo);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, description, researchField, logo);
	}

	@Override
	public String toString() {
		return "ProjectAdminControlledAttributes{" +
				"id='" + id + '\'' +
				", description='" + description + '\'' +
				", researchField='" + researchField + '\'' +
				", logo=" + logo +
				'}';
	}
}
