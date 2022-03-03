/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.users;

import java.util.List;
import java.util.Objects;

public class AllUsersAndSiteAdmins {
	public final List<FURMSUser> allUsers;
	public final List<FURMSUser> siteAdmins;

	public AllUsersAndSiteAdmins(List<FURMSUser> allUsers, List<FURMSUser> siteAdmins) {
		this.allUsers = List.copyOf(allUsers);
		this.siteAdmins = List.copyOf(siteAdmins);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AllUsersAndSiteAdmins that = (AllUsersAndSiteAdmins) o;
		return Objects.equals(allUsers, that.allUsers) && Objects.equals(siteAdmins, that.siteAdmins);
	}

	@Override
	public int hashCode() {
		return Objects.hash(allUsers, siteAdmins);
	}

	@Override
	public String toString() {
		return "UsersAndCommunityAdmins{" +
			"allUsers=" + allUsers +
			", siteUsers=" + siteAdmins +
			'}';
	}
}
