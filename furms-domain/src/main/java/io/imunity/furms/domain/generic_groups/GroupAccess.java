/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.generic_groups;

import java.util.Objects;
import java.util.Set;

public class GroupAccess {
	public final String communityId;
	public final Set<String> groups;

	public GroupAccess(String communityId, Set<String> groups) {
		this.communityId = communityId;
		this.groups = groups;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GroupAccess that = (GroupAccess) o;
		return Objects.equals(communityId, that.communityId) && Objects.equals(groups, that.groups);
	}

	@Override
	public int hashCode() {
		return Objects.hash(communityId, groups);
	}

	@Override
	public String toString() {
		return "GroupAccess{" +
			"communityId='" + communityId + '\'' +
			", groups=" + groups +
			'}';
	}
}
