/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Objects;

@JsonTypeName("SetUserStatusRequest")
public class SetUserStatusRequest implements Body {
	public final String fenixUserId;
	public final UserAccountStatus status;
	public final SetUserStatusReason reason;

	public SetUserStatusRequest(String fenixUserId, UserAccountStatus status, SetUserStatusReason reason) {
		this.fenixUserId = fenixUserId;
		this.status = status;
		this.reason = reason;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SetUserStatusRequest that = (SetUserStatusRequest) o;
		return Objects.equals(fenixUserId, that.fenixUserId)
				&& Objects.equals(status, that.status)
				&& Objects.equals(reason, that.reason);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fenixUserId, status, reason);
	}

	@Override
	public String toString() {
		return "SetUserStatusRequest{" +
				"fenixUserId='" + fenixUserId + '\'' +
				", status='" + status + '\'' +
				", reason='" + reason + '\'' +
				'}';
	}
}
