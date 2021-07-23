/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.policy_documents;

import io.imunity.furms.domain.users.FURMSUser;

import java.util.Objects;
import java.util.Set;

public class UserPolicyAgreements {
	public final FURMSUser user;
	public final Set<PolicyAgreement> policyAgreements;

	public UserPolicyAgreements(FURMSUser user, Set<PolicyAgreement> policyAgreements) {
		this.user = user;
		this.policyAgreements = policyAgreements;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserPolicyAgreements that = (UserPolicyAgreements) o;
		return Objects.equals(user, that.user) && Objects.equals(policyAgreements, that.policyAgreements);
	}

	@Override
	public int hashCode() {
		return Objects.hash(user, policyAgreements);
	}

	@Override
	public String toString() {
		return "UserPolicyAgreements{" +
			"user=" + user +
			", policyAgreements=" + policyAgreements +
			'}';
	}
}
