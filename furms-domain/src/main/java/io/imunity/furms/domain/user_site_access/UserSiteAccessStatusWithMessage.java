/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.user_site_access;

import java.util.Objects;

public class UserSiteAccessStatusWithMessage {
	public final String message;
	public final UserSiteAccessStatus status;

	public UserSiteAccessStatusWithMessage(String message, UserSiteAccessStatus status) {
		this.message = message;
		this.status = status;
	}

	public UserSiteAccessStatusWithMessage(UserSiteAccessStatus status) {
		this.status = status;
		this.message = null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserSiteAccessStatusWithMessage that = (UserSiteAccessStatusWithMessage) o;
		return Objects.equals(message, that.message) && status == that.status;
	}

	@Override
	public int hashCode() {
		return Objects.hash(message, status);
	}

	@Override
	public String toString() {
		return "UserSiteAccessStatusWithMessage{" +
			"message='" + message + '\'' +
			", status=" + status +
			'}';
	}
}
