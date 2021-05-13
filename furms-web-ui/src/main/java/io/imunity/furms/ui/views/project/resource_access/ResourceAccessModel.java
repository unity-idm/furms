/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.resource_access;

import io.imunity.furms.domain.sites.SiteId;

import java.util.Objects;

class ResourceAccessModel {
	private String firstName;
	private String lastName;
	private String email;
	private String allocation;
	private String access;
	private String status;
	private SiteId siteId;
	private String allocationId;
	private String fenixUserId;
	private boolean accessible;
	private String message;

	ResourceAccessModel(String firstName, String lastName, String email, String allocation, String access, String status,
	                    boolean accessible, SiteId siteId, String allocationId, String fenixUserId, String message) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.allocation = allocation;
		this.access = access;
		this.status = status;
		this.accessible = accessible;
		this.siteId = siteId;
		this.allocationId = allocationId;
		this.fenixUserId = fenixUserId;
		this.message = message;
	}

	String getFirstName() {
		return firstName;
	}

	String getLastName() {
		return lastName;
	}

	String getEmail() {
		return email;
	}

	String getAllocation() {
		return allocation;
	}

	String getAccess() {
		return access;
	}

	String getStatus() {
		return status;
	}

	boolean isAccessible() {
		return accessible;
	}

	SiteId getSiteId() {
		return siteId;
	}

	String getAllocationId() {
		return allocationId;
	}

	String getFenixUserId() {
		return fenixUserId;
	}

	String getMessage() {
		return message;
	}

	boolean matches(String value) {
		return firstName.toLowerCase().contains(value) ||
			lastName.toLowerCase().contains(value) ||
			email.toLowerCase().contains(value);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceAccessModel that = (ResourceAccessModel) o;
		return accessible == that.accessible &&
			Objects.equals(firstName, that.firstName) &&
			Objects.equals(lastName, that.lastName) &&
			Objects.equals(email, that.email) &&
			Objects.equals(allocation, that.allocation) &&
			Objects.equals(access, that.access) &&
			Objects.equals(message, that.message) &&
			Objects.equals(status, that.status);
	}

	@Override
	public int hashCode() {
		return Objects.hash(firstName, lastName, email, allocation, access, status, accessible, message);
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
			", message=" + message +
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
		private SiteId siteId;
		private String allocationId;
		private String fenixUserId;
		private boolean accessible;
		private String message;

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

		public ResourceAccessModelBuilder allocationId(String allocationId) {
			this.allocationId = allocationId;
			return this;
		}

		public ResourceAccessModelBuilder siteId(SiteId siteId) {
			this.siteId = siteId;
			return this;
		}

		public ResourceAccessModelBuilder accessible(boolean accessible) {
			this.accessible = accessible;
			return this;
		}

		public ResourceAccessModelBuilder fenixUserId(String fenixUserId) {
			this.fenixUserId = fenixUserId;
			return this;
		}

		public ResourceAccessModelBuilder message(String message) {
			this.message = message;
			return this;
		}

		public ResourceAccessModel build() {
			return new ResourceAccessModel(firstName, lastName, email, allocation, access, status, accessible, siteId, allocationId, fenixUserId, message);
		}
	}
}
