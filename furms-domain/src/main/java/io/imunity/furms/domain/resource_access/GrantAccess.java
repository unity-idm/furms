/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_access;

import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Objects;

public class GrantAccess {
	public final SiteId siteId;
	public final ProjectId projectId;
	public final ProjectAllocationId allocationId;
	public final FenixUserId fenixUserId;
	public final AccessStatus status;

	GrantAccess(SiteId siteId, ProjectId projectId, ProjectAllocationId allocationId, FenixUserId fenixUserId, AccessStatus status) {
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
		return "GrantAccess{" +
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
		private ProjectId projectId;
		private ProjectAllocationId allocationId;
		private FenixUserId fenixUserId;
		private AccessStatus status;

		private RevokeAccessBuilder() {
		}

		public RevokeAccessBuilder siteId(SiteId siteId) {
			this.siteId = siteId;
			return this;
		}

		public RevokeAccessBuilder projectId(String projectId) {
			this.projectId = new ProjectId(projectId);
			return this;
		}

		public RevokeAccessBuilder projectId(ProjectId projectId) {
			this.projectId = projectId;
			return this;
		}

		public RevokeAccessBuilder allocationId(String allocationId) {
			this.allocationId = new ProjectAllocationId(allocationId);
			return this;
		}

		public RevokeAccessBuilder allocationId(ProjectAllocationId allocationId) {
			this.allocationId = allocationId;
			return this;
		}

		public RevokeAccessBuilder fenixUserId(FenixUserId fenixUserId) {
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
