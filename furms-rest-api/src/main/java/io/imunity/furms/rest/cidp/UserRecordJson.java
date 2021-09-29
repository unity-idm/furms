/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.cidp;

import io.imunity.furms.domain.users.UserRecord;
import io.imunity.furms.domain.users.UserStatus;
import io.imunity.furms.rest.user.User;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class UserRecordJson {
	public final User user;
	public final UserStatus userStatus;
	public final List<SiteUserJson> siteAccess;
	public final List<GroupAccessJson> groupAccess;

	UserRecordJson(User user, UserStatus userStatus, List<SiteUserJson> siteAccess, List<GroupAccessJson> groupAccess) {
		this.user = user;
		this.userStatus = userStatus;
		this.siteAccess = siteAccess;
		this.groupAccess = groupAccess;
	}

	UserRecordJson(UserRecord record) {
		this(new User(record.user),
				record.user.status,
				record.siteInstallations.stream().map(SiteUserJson::new).collect(toList()),
				record.groupAccesses.stream().map(x -> new GroupAccessJson(x.communityId, x.groups)).collect(Collectors.toList()));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserRecordJson that = (UserRecordJson) o;
		return Objects.equals(user, that.user) && userStatus == that.userStatus && Objects.equals(siteAccess, that.siteAccess);
	}

	@Override
	public int hashCode() {
		return Objects.hash(user, userStatus, siteAccess);
	}

	@Override
	public String toString() {
		return "UserRecordJson{" +
				"user=" + user +
				", userStatus=" + userStatus +
				", siteAccess=" + siteAccess +
				'}';
	}
}
