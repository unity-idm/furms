/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.users;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GroupedUsers {
	private final Map<String, List<FURMSUser>> usersByGroups;

	public GroupedUsers(Map<String, List<FURMSUser>> usersByGroups) {
		this.usersByGroups = usersByGroups;
	}

	public List<FURMSUser> getUsers(String group){
		return usersByGroups.getOrDefault(group, Collections.emptyList());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GroupedUsers that = (GroupedUsers) o;
		return Objects.equals(usersByGroups, that.usersByGroups);
	}

	@Override
	public int hashCode() {
		return Objects.hash(usersByGroups);
	}

	@Override
	public String toString() {
		return "GroupedUsers{" +
			"usersByGroups=" + usersByGroups +
			'}';
	}
}
