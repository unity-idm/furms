/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.users;

import java.util.List;
import java.util.Objects;

public class CommunityUsersAndCommunityAdmins {
	public final List<FURMSUser> communityUsers;
	public final List<FURMSUser> communityAdmins;

	public CommunityUsersAndCommunityAdmins(List<FURMSUser> communityUsers, List<FURMSUser> communityAdmins) {
		this.communityUsers = communityUsers;
		this.communityAdmins = communityAdmins;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommunityUsersAndCommunityAdmins that = (CommunityUsersAndCommunityAdmins) o;
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
