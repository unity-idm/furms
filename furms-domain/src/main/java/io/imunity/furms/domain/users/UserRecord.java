/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.domain.users;

import io.imunity.furms.domain.generic_groups.GroupAccess;
import io.imunity.furms.domain.sites.SiteUser;

import java.util.Objects;
import java.util.Set;

public class UserRecord {
	public final FURMSUser user;
	public final Set<SiteUser> siteInstallations;
	public final Set<GroupAccess> groupAccesses;

	public UserRecord(FURMSUser user, Set<SiteUser> siteInstallations, Set<GroupAccess> groupAccesses) {
		this.user = user;
		this.siteInstallations = siteInstallations;
		this.groupAccesses = groupAccesses;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserRecord that = (UserRecord) o;
		return Objects.equals(user, that.user)
				&& Objects.equals(siteInstallations, that.siteInstallations)
				&& Objects.equals(groupAccesses, that.groupAccesses);
	}

	@Override
	public int hashCode() {
		return Objects.hash(user, siteInstallations, groupAccesses);
	}

	@Override
	public String toString() {
		return "UserRecord{" +
				"user=" + user +
				", siteInstallations=" + siteInstallations +
				", groupAccesses=" + groupAccesses +
				'}';
	}
}
