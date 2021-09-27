/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.generic_groups;

import java.util.Objects;

public class GenericGroupWithAssignmentAmount {
	public final GenericGroup group;
	public final int amount;

	public GenericGroupWithAssignmentAmount(GenericGroup group, int amount) {
		this.group = group;
		this.amount = amount;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GenericGroupWithAssignmentAmount that = (GenericGroupWithAssignmentAmount) o;
		return Objects.equals(group, that.group) && amount == that.amount;
	}

	@Override
	public int hashCode() {
		return Objects.hash(group, amount);
	}

	@Override
	public String toString() {
		return "GenericGroupWithAssignments{" +
			"group=" + group +
			", assignments=" + amount +
			'}';
	}
}
