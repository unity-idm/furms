/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.groups;

import io.imunity.furms.domain.generic_groups.GenericGroupId;

import java.util.Objects;

class GroupGridModel {
	public final GenericGroupId id;
	public final String communityId;
	public final String name;
	public final String description;
	public final int membersAmount;

	GroupGridModel(GenericGroupId id, String communityId, String name, String description, int membersAmount) {
		this.id = id;
		this.communityId = communityId;
		this.name = name;
		this.description = description;
		this.membersAmount = membersAmount;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GroupGridModel that = (GroupGridModel) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "GroupGridModel{" +
			"id=" + id +
			", name='" + name + '\'' +
			", description='" + description + '\'' +
			", membersAmount=" + membersAmount +
			'}';
	}
}
