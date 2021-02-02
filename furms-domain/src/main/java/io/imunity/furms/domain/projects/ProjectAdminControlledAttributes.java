/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.projects;

import io.imunity.furms.domain.images.FurmsImage;

import java.util.Objects;

public class ProjectAdminControlledAttributes {
	private final String id;
	private final String description;
	private final FurmsImage logo;

	public ProjectAdminControlledAttributes(String id, String description, FurmsImage logo) {
		this.id = id;
		this.description = description;
		this.logo = logo;
	}

	public String getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public FurmsImage getLogo() {
		return logo;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectAdminControlledAttributes that = (ProjectAdminControlledAttributes) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(description, that.description) &&
			Objects.equals(logo, that.logo);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, description, logo);
	}

	@Override
	public String toString() {
		return "AdminControlledProjectAttributes{" +
			"id='" + id + '\'' +
			", description='" + description + '\'' +
			", logo=" + logo +
			'}';
	}
}
