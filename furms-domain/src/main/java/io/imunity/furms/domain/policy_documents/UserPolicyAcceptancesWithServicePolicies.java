/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.policy_documents;

import io.imunity.furms.domain.users.FURMSUser;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class UserPolicyAcceptancesWithServicePolicies {
	public final FURMSUser user;
	public final Set<PolicyAcceptance> policyAcceptances;
	public final Optional<PolicyDocument> sitePolicy;
	public final Set<AssignedPolicyDocument> servicePolicyDocuments;

	public UserPolicyAcceptancesWithServicePolicies(FURMSUser user, Set<PolicyAcceptance> policyAcceptances,
	                                         Optional<PolicyDocument> sitePolicy, Set<AssignedPolicyDocument> servicePolicyDocuments) {
		this.user = user;
		this.policyAcceptances = policyAcceptances;
		this.sitePolicy = sitePolicy;
		this.servicePolicyDocuments = servicePolicyDocuments;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserPolicyAcceptancesWithServicePolicies that = (UserPolicyAcceptancesWithServicePolicies) o;
		return Objects.equals(user, that.user) && Objects.equals(policyAcceptances, that.policyAcceptances) && Objects.equals(sitePolicy, that.sitePolicy) && Objects.equals(servicePolicyDocuments, that.servicePolicyDocuments);
	}

	@Override
	public int hashCode() {
		return Objects.hash(user, policyAcceptances, sitePolicy, servicePolicyDocuments);
	}

	@Override
	public String toString() {
		return "UserPolicyAcceptancesWithServicePolicies{" +
			"user=" + user +
			", policyAcceptances=" + policyAcceptances +
			", sitePolicy=" + sitePolicy +
			", servicePolicyDocuments=" + servicePolicyDocuments +
			'}';
	}
}
