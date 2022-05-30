/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.resource_access;

import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FenixUserId;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

class ResourceAccessModel {
	private final String firstName;
	private final String lastName;
	private final String email;
	private final String allocation;
	private final String access;
	private final String status;
	private final SiteId siteId;
	private final ProjectAllocationId allocationId;
	private final FenixUserId fenixUserId;
	private final boolean accessible;
	private final String message;

	ResourceAccessModel(String firstName, String lastName, String email, String allocation, String access, String status,
	                    boolean accessible, SiteId siteId, ProjectAllocationId allocationId, FenixUserId fenixUserId, String message) {
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

	String getFullName() {
		return (firstName == null ? "" : firstName + " ") + (lastName == null ? "" : lastName); 
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

	ProjectAllocationId getAllocationId() {
		return allocationId;
	}

	FenixUserId getFenixUserId() {
		return fenixUserId;
	}

	String getMessage() {
		return message;
	}

	boolean matches(String value) {
		if (StringUtils.isBlank(value)) {
			return true;
		}
		final String lowerCaseValue = value.toLowerCase();
		return (firstName != null && firstName.toLowerCase().contains(lowerCaseValue)) ||
				(lastName != null && lastName.toLowerCase().contains(lowerCaseValue)) ||
				email.toLowerCase().contains(lowerCaseValue);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceAccessModel that = (ResourceAccessModel) o;
		return Objects.equals(allocationId, that.allocationId) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(fenixUserId, that.fenixUserId) &&
			Objects.equals(email, that.email);

	}

	@Override
	public int hashCode() {
		return Objects.hash(allocationId, siteId, fenixUserId, email);
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
		private ProjectAllocationId allocationId;
		private FenixUserId fenixUserId;
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

		public ResourceAccessModelBuilder allocationId(ProjectAllocationId allocationId) {
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

		public ResourceAccessModelBuilder fenixUserId(FenixUserId fenixUserId) {
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
