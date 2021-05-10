/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.resource_access;

import java.util.Objects;

class ResourceAccessModel {
	private String firstName;
	private String lastName;
	private String email;
	private String allocation;
	private String access;
	private String status;
	private boolean accessible;

	ResourceAccessModel(String firstName, String lastName, String email, String allocation, String access, String status, boolean accessible) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.allocation = allocation;
		this.access = access;
		this.status = status;
		this.accessible = accessible;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceAccessModel that = (ResourceAccessModel) o;
		return accessible == that.accessible && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(email, that.email) && Objects.equals(allocation, that.allocation) && Objects.equals(access, that.access) && Objects.equals(status, that.status);
	}

	@Override
	public int hashCode() {
		return Objects.hash(firstName, lastName, email, allocation, access, status, accessible);
	}

	@Override
	public String toString() {
		return "ResourceAccessModel{" +
			"firstName='" + firstName + '\'' +
			", lastName='" + lastName + '\'' +
			", email='" + email + '\'' +
			", allocation='" + allocation + '\'' +
			", access='" + access + '\'' +
			", status='" + status + '\'' +
			", accessible=" + accessible +
			'}';
	}

	public static ResourceAccessModelBuilder builder() {
		return new ResourceAccessModelBuilder();
	}

	public static final class ResourceAccessModelBuilder {
		private String firstName;
		private String lastName;
		private String email;
		private String allocation;
		private String access;
		private String status;
		private boolean accessible;

		private ResourceAccessModelBuilder() {
		}

		public ResourceAccessModelBuilder firstName(String firstName) {
			this.firstName = firstName;
			return this;
		}

		public ResourceAccessModelBuilder lastName(String lastName) {
			this.lastName = lastName;
			return this;
		}

		public ResourceAccessModelBuilder email(String email) {
			this.email = email;
			return this;
		}

		public ResourceAccessModelBuilder allocation(String allocation) {
			this.allocation = allocation;
			return this;
		}

		public ResourceAccessModelBuilder access(String access) {
			this.access = access;
			return this;
		}

		public ResourceAccessModelBuilder status(String status) {
			this.status = status;
			return this;
		}

		public ResourceAccessModelBuilder accessible(boolean accessible) {
			this.accessible = accessible;
			return this;
		}

		public ResourceAccessModel build() {
			return new ResourceAccessModel(firstName, lastName, email, allocation, access, status, accessible);
		}
	}
}
