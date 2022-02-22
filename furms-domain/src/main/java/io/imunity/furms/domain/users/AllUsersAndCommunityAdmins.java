/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.users;

import java.util.List;
import java.util.Objects;

public class AllUsersAndCommunityAdmins {
	public final List<FURMSUser> allUsers;
	public final List<FURMSUser> communityAdmins;

	public AllUsersAndCommunityAdmins(List<FURMSUser> allUsers, List<FURMSUser> communityAdmins) {
		this.allUsers = allUsers;
		this.communityAdmins = communityAdmins;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AllUsersAndCommunityAdmins that = (AllUsersAndCommunityAdmins) o;
		return Objects.equals(allUsers, that.allUsers) && Objects.equals(communityAdmins, that.communityAdmins);
	}

	@Override
	public int hashCode() {
		return Objects.hash(allUsers, communityAdmins);
	}

	@Override
	public String toString() {
		return "UsersAndCommunityAdmins{" +
			"allUsers=" + allUsers +
			", communityAdmins=" + communityAdmins +
			'}';
	}
}
