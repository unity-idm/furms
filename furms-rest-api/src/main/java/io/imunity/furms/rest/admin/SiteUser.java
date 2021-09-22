/*
 * Copyright (c) 2020 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.imunity.furms.rest.user.User;

class SiteUser {
	public final User user;
	public final String uid;
	public final List<String> sshKeys;
	public final Set<String> projectIds;
	
	SiteUser(User user, String uid, List<String> sshKeys, Set<String> projectIds) {
		this.user = user;
		this.uid = uid;
		this.projectIds = ImmutableSet.copyOf(projectIds);
		this.sshKeys = ImmutableList.copyOf(sshKeys);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SiteUser siteUser = (SiteUser) o;
		return Objects.equals(user, siteUser.user)
				&& Objects.equals(uid, siteUser.uid)
				&& Objects.equals(sshKeys, siteUser.sshKeys)
				&& Objects.equals(projectIds, siteUser.projectIds);
	}

	@Override
	public int hashCode() {
		return Objects.hash(user, uid, sshKeys, projectIds);
	}

	@Override
	public String toString() {
		return "SiteUser{" +
				"user=" + user +
				", uid='" + uid + '\'' +
				", sshKeys=" + sshKeys +
				", projectIds=" + projectIds +
				'}';
	}
}
