/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.services;

import java.util.Objects;

class InfraServiceViewModel {
	public final String id;
	public final String siteId;
	public String name;
	public String description;

	private InfraServiceViewModel(String id, String siteId, String name, String description) {
		this.id = id;
		this.siteId = siteId;
		this.name = name;
		this.description = description;
	}

	InfraServiceViewModel(String siteId) {
		this.id = null;
		this.siteId = siteId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		InfraServiceViewModel that = (InfraServiceViewModel) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(name, that.name) &&
			Objects.equals(description, that.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, siteId, name, description);
	}

	@Override
	public String toString() {
		return "ServiceViewModel{" +
			"id='" + id + '\'' +
			", siteId='" + siteId + '\'' +
			", name='" + name + '\'' +
			", description='" + description + '\'' +
			'}';
	}

	public static ServiceViewModelBuilder builder() {
		return new ServiceViewModelBuilder();
	}

	public static final class ServiceViewModelBuilder {
		public String id;
		public String siteId;
		public String name;
		public String description;

		private ServiceViewModelBuilder() {
		}

		public ServiceViewModelBuilder id(String id) {
			this.id = id;
			return this;
		}

		public ServiceViewModelBuilder siteId(String siteId) {
			this.siteId = siteId;
			return this;
		}

		public ServiceViewModelBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ServiceViewModelBuilder description(String description) {
			this.description = description;
			return this;
		}

		public InfraServiceViewModel build() {
			return new InfraServiceViewModel(id, siteId, name, description);
		}
	}
}
