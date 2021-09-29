/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.generic_groups;

import io.imunity.furms.domain.users.FURMSUser;

import java.util.Objects;

public class GenericGroupAssignmentWithUser {
	public final FURMSUser furmsUser;
	public final GenericGroupAssignment assignment;

	public GenericGroupAssignmentWithUser(FURMSUser furmsUser, GenericGroupAssignment assignment) {
		this.furmsUser = furmsUser;
		this.assignment = assignment;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GenericGroupAssignmentWithUser that = (GenericGroupAssignmentWithUser) o;
		return Objects.equals(furmsUser, that.furmsUser) && Objects.equals(assignment, that.assignment);
	}

	@Override
	public int hashCode() {
		return Objects.hash(furmsUser, assignment);
	}

	@Override
	public String toString() {
		return "GenericGroupAssignmentWithUser{" +
			"furmsUser=" + furmsUser +
			", assignment=" + assignment +
			'}';
	}
}
