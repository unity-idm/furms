/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.generic_groups;

import io.imunity.furms.domain.users.FURMSUser;

import java.util.Objects;

public class GenericGroupAssignmentWithUser {
	public final FURMSUser furmsUser;
	public final GenericGroupMembership membership;

	public GenericGroupAssignmentWithUser(FURMSUser furmsUser, GenericGroupMembership membership) {
		this.furmsUser = furmsUser;
		this.membership = membership;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GenericGroupAssignmentWithUser that = (GenericGroupAssignmentWithUser) o;
		return Objects.equals(furmsUser, that.furmsUser) && Objects.equals(membership, that.membership);
	}

	@Override
	public int hashCode() {
		return Objects.hash(furmsUser, membership);
	}

	@Override
	public String toString() {
		return "GenericGroupAssignmentWithUser{" +
			"furmsUser=" + furmsUser +
			", assignment=" + membership +
			'}';
	}
}
