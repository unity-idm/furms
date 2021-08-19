/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.policy_documents;

import io.imunity.furms.domain.users.FURMSUser;

import java.util.Objects;
import java.util.Set;

public class UserPolicyAcceptancesWithServicePolicies {
	public final FURMSUser user;
	public final Set<PolicyAcceptance> policyAcceptances;
	public final Set<ServicePolicyDocument> servicePolicyDocuments;

	public UserPolicyAcceptancesWithServicePolicies(FURMSUser user, Set<PolicyAcceptance> policyAcceptances, Set<ServicePolicyDocument> servicePolicyDocuments) {
		this.user = user;
		this.policyAcceptances = policyAcceptances;
		this.servicePolicyDocuments = servicePolicyDocuments;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserPolicyAcceptancesWithServicePolicies that = (UserPolicyAcceptancesWithServicePolicies) o;
		return Objects.equals(user, that.user) && Objects.equals(policyAcceptances, that.policyAcceptances) && Objects.equals(servicePolicyDocuments, that.servicePolicyDocuments);
	}

	@Override
	public int hashCode() {
		return Objects.hash(user, policyAcceptances, servicePolicyDocuments);
	}

	@Override
	public String toString() {
		return "UserPolicyAcceptancesWithServicePolicies{" +
			"user=" + user +
			", policyAcceptances=" + policyAcceptances +
			", servicePolicyDocuments=" + servicePolicyDocuments +
			'}';
	}
}
