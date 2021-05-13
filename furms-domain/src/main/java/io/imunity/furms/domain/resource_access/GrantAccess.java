/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_access;

import io.imunity.furms.domain.sites.SiteId;

import java.util.Objects;

public class GrantAccess {
	public final SiteId siteId;
	public final String projectId;
	public final String allocationId;
	public final String fenixUserId;
	public final AccessStatus status;

	GrantAccess(SiteId siteId, String projectId, String allocationId, String fenixUserId, AccessStatus status) {
		this.siteId = siteId;
		this.projectId = projectId;
		this.allocationId = allocationId;
		this.fenixUserId = fenixUserId;
		this.status = status;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GrantAccess that = (GrantAccess) o;
		return Objects.equals(siteId, that.siteId) &&
			Objects.equals(projectId, that.projectId) &&
			Objects.equals(allocationId, that.allocationId) &&
			Objects.equals(fenixUserId, that.fenixUserId) &&
			status == that.status;
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteId, projectId, allocationId, fenixUserId, status);
	}

	@Override
	public String toString() {
		return "RevokeAccess{" +
			"siteId=" + siteId +
			", projectId='" + projectId + '\'' +
			", allocationId='" + allocationId + '\'' +
			", fenixUserId='" + fenixUserId + '\'' +
			", status=" + status +
			'}';
	}

	public static RevokeAccessBuilder builder() {
		return new RevokeAccessBuilder();
	}

	public static final class RevokeAccessBuilder {
		private SiteId siteId;
		private String projectId;
		private String allocationId;
		private String fenixUserId;
		private AccessStatus status;

		private RevokeAccessBuilder() {
		}

		public RevokeAccessBuilder siteId(SiteId siteId) {
			this.siteId = siteId;
			return this;
		}

		public RevokeAccessBuilder projectId(String projectId) {
			this.projectId = projectId;
			return this;
		}

		public RevokeAccessBuilder allocationId(String allocationId) {
			this.allocationId = allocationId;
			return this;
		}

		public RevokeAccessBuilder fenixUserId(String fenixUserId) {
			this.fenixUserId = fenixUserId;
			return this;
		}

		public RevokeAccessBuilder status(AccessStatus status) {
			this.status = status;
			return this;
		}

		public GrantAccess build() {
			return new GrantAccess(siteId, projectId, allocationId, fenixUserId, status);
		}
	}
}
