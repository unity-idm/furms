/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.users;

import java.util.List;
import java.util.Objects;

public class AllUsersAndFenixAdmins {
	public final List<FURMSUser> allUsers;
	public final List<FURMSUser> fenixAdmins;

	public AllUsersAndFenixAdmins(List<FURMSUser> allUsers, List<FURMSUser> fenixAdmins) {
		this.allUsers = List.copyOf(allUsers);
		this.fenixAdmins = List.copyOf(fenixAdmins);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AllUsersAndFenixAdmins that = (AllUsersAndFenixAdmins) o;
		return Objects.equals(allUsers, that.allUsers) && Objects.equals(fenixAdmins, that.fenixAdmins);
	}

	@Override
	public int hashCode() {
		return Objects.hash(allUsers, fenixAdmins);
	}

	@Override
	public String toString() {
		return "AllUsersAndFenixAdmins{" +
			"allUsers=" + allUsers +
			", fenixAdmins=" + fenixAdmins +
			'}';
	}
}
