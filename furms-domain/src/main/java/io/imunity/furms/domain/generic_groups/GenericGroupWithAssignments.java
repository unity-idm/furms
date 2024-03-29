/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.generic_groups;

import java.util.Objects;
import java.util.Set;

public class GenericGroupWithAssignments {
	public final GenericGroup group;
	public final Set<GenericGroupMembership> memberships;

	public GenericGroupWithAssignments(GenericGroup group, Set<GenericGroupMembership> memberships) {
		this.group = group;
		this.memberships = memberships;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GenericGroupWithAssignments that = (GenericGroupWithAssignments) o;
		return Objects.equals(group, that.group) && Objects.equals(memberships, that.memberships);
	}

	@Override
	public int hashCode() {
		return Objects.hash(group, memberships);
	}

	@Override
	public String toString() {
		return "GenericGroupWithAssignments{" +
			"group=" + group +
			", assignments=" + memberships +
			'}';
	}
}
