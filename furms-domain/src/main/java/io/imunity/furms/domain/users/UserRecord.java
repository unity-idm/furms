/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.domain.users;

import io.imunity.furms.domain.sites.SiteUser;

import java.util.Objects;
import java.util.Set;

public class UserRecord {
	public final FURMSUser user;
	public final Set<SiteUser> siteInstallations;

	public UserRecord(FURMSUser user, Set<SiteUser> siteInstallations) {
		this.user = user;
		this.siteInstallations = siteInstallations;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserRecord that = (UserRecord) o;
		return Objects.equals(user, that.user)
				&& Objects.equals(siteInstallations, that.siteInstallations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(user, siteInstallations);
	}

	@Override
	public String toString() {
		return "UserRecord{" +
				"user=" + user +
				", siteInstallations=" + siteInstallations +
				'}';
	}
}
