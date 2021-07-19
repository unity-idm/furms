/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.util.Objects;

class ProjectAllocationId {
	public final String projectId;
	public final String allocationId;

	ProjectAllocationId(String projectId, String allocationId) {
		this.projectId = projectId;
		this.allocationId = allocationId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectAllocationId that = (ProjectAllocationId) o;
		return Objects.equals(projectId, that.projectId)
				&& Objects.equals(allocationId, that.allocationId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectId, allocationId);
	}

	@Override
	public String toString() {
		return "ProjectAllocationId{" +
				"projectId='" + projectId + '\'' +
				", allocationId='" + allocationId + '\'' +
				'}';
	}
}
