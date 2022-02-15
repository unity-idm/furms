/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.users;

import java.util.List;
import java.util.Objects;

public class GroupedUsers {
	public final List<FURMSUser> firstUsersGroup;
	public final List<FURMSUser> secondUsersGroup;

	public GroupedUsers(List<FURMSUser> firstUsersGroup, List<FURMSUser> secondUsersGroup) {
		this.firstUsersGroup = firstUsersGroup;
		this.secondUsersGroup = secondUsersGroup;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GroupedUsers that = (GroupedUsers) o;
		return Objects.equals(firstUsersGroup, that.firstUsersGroup) && Objects.equals(secondUsersGroup, that.secondUsersGroup);
	}

	@Override
	public int hashCode() {
		return Objects.hash(firstUsersGroup, secondUsersGroup);
	}

	@Override
	public String toString() {
		return "GroupedUsers{" +
			"firstUsersGroup=" + firstUsersGroup +
			", secondUsersGroup=" + secondUsersGroup +
			'}';
	}
}
