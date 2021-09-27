/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.cidp;

import java.util.Objects;
import java.util.Set;

class GroupAccessJson {
	public final String communityId;
	public final Set<String> groups;

	GroupAccessJson(String communityId, Set<String> groups) {
		this.communityId = communityId;
		this.groups = groups;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GroupAccessJson that = (GroupAccessJson) o;
		return Objects.equals(communityId, that.communityId) && Objects.equals(groups, that.groups);
	}

	@Override
	public int hashCode() {
		return Objects.hash(communityId, groups);
	}

	@Override
	public String toString() {
		return "GroupAccessJson{" +
			"communityId='" + communityId + '\'' +
			", groups=" + groups +
			'}';
	}
}
