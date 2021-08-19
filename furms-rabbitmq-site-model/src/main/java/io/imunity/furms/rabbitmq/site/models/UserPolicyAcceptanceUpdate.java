/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;
import java.util.Objects;

@JsonTypeName("UserPolicyAcceptanceUpdate")
public class UserPolicyAcceptanceUpdate implements Body {
	public final String fenixUserId;
	public final List<PolicyAcceptance> policiesAcceptance;

	@JsonCreator
	public UserPolicyAcceptanceUpdate(String fenixUserId, List<PolicyAcceptance> policiesAcceptance) {
		this.fenixUserId = fenixUserId;
		this.policiesAcceptance = policiesAcceptance;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserPolicyAcceptanceUpdate that = (UserPolicyAcceptanceUpdate) o;
		return Objects.equals(fenixUserId, that.fenixUserId) &&
			Objects.equals(policiesAcceptance, that.policiesAcceptance);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fenixUserId, policiesAcceptance);
	}

	@Override
	public String toString() {
		return "UserPolicyAcceptanceUpdate{" +
			"fenixUserId='" + fenixUserId + '\'' +
			", policiesAcceptance=" + policiesAcceptance +
			'}';
	}
}
