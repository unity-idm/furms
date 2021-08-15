/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.policy_documents;

import com.google.common.collect.ImmutableSet;
import io.imunity.furms.domain.users.FURMSUser;

import java.util.Objects;
import java.util.Set;

public class UserPolicyAcceptances {
	public final FURMSUser user;
	public final Set<PolicyAcceptance> policyAcceptances;

	public UserPolicyAcceptances(FURMSUser user, Set<PolicyAcceptance> policyAcceptances) {
		this.user = user;
		this.policyAcceptances = ImmutableSet.copyOf(policyAcceptances);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserPolicyAcceptances that = (UserPolicyAcceptances) o;
		return Objects.equals(user, that.user) && Objects.equals(policyAcceptances, that.policyAcceptances);
	}

	@Override
	public int hashCode() {
		return Objects.hash(user, policyAcceptances);
	}

	@Override
	public String toString() {
		return "UserPolicyAcceptances{" +
			"user=" + user +
			", policyAcceptances=" + policyAcceptances +
			'}';
	}
}
