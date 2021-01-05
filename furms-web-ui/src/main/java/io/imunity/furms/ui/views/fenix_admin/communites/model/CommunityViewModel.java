/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix_admin.communites.model;

import io.imunity.furms.domain.images.FurmsImage;

import java.util.Objects;

public class CommunityViewModel {
	private String id;
	private String name;
	private String description;
	private FurmsImage logo;

	public CommunityViewModel(){}

	public CommunityViewModel(String id, String name, String description, FurmsImage logo) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.logo = logo;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public FurmsImage getLogoImage() {
		return logo;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setLogoImage(FurmsImage logo) {
		this.logo = logo;
	}

	public static CommunityViewModelBuilder builder() {
		return new CommunityViewModelBuilder();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommunityViewModel that = (CommunityViewModel) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(name, that.name) &&
			Objects.equals(description, that.description) &&
			Objects.equals(logo, that.logo);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, description, logo);
	}

	@Override
	public String toString() {
		return "CommunityViewModel{" +
			"id='" + id + '\'' +
			", name='" + name + '\'' +
			", description='" + description + '\'' +
			", logo=" + logo +
			'}';
	}

	public static class CommunityViewModelBuilder {
		private String id;
		private String name;
		private String description;
		private FurmsImage logo;

		private CommunityViewModelBuilder() {
		}

		public CommunityViewModelBuilder id(String id) {
			this.id = id;
			return this;
		}

		public CommunityViewModelBuilder name(String name) {
			this.name = name;
			return this;
		}

		public CommunityViewModelBuilder description(String description) {
			this.description = description;
			return this;
		}

		public CommunityViewModelBuilder logo(FurmsImage logo) {
			this.logo = logo;
			return this;
		}

		public CommunityViewModel build() {
			return new CommunityViewModel(id, name, description, logo);
		}
	}
}
