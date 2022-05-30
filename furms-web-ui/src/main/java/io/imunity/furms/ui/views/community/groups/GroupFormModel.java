/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.groups;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.generic_groups.GenericGroupId;

import java.util.Objects;

class GroupFormModel {
	public final GenericGroupId id;
	public final CommunityId communityId;
	public String name;
	public String description;

	GroupFormModel(GenericGroupId id, CommunityId communityId, String name, String description) {
		this.id = id;
		this.communityId = communityId;
		this.name = name;
		this.description = description;
	}

	GroupFormModel(CommunityId communityId) {
		this.id = GenericGroupId.empty();
		this.communityId = communityId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GroupFormModel that = (GroupFormModel) o;
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
			'}';
	}
}
