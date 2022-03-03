/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.users;

import java.util.List;
import java.util.Objects;

public class CommunityUsersAndAdmins {
	public final List<FURMSUser> communityUsers;
	public final List<FURMSUser> communityAdmins;

	public CommunityUsersAndAdmins(List<FURMSUser> communityUsers, List<FURMSUser> communityAdmins) {
		this.communityUsers = List.copyOf(communityUsers);
		this.communityAdmins = List.copyOf(communityAdmins);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommunityUsersAndAdmins that = (CommunityUsersAndAdmins) o;
		return Objects.equals(communityUsers, that.communityUsers) && Objects.equals(communityAdmins, that.communityAdmins);
	}

	@Override
	public int hashCode() {
		return Objects.hash(communityUsers, communityAdmins);
	}

	@Override
	public String toString() {
		return "CommunityUsersAndCommunityAdmins{" +
			"communityUsers=" + communityUsers +
			", communityAdmins=" + communityAdmins +
			'}';
	}
}
