/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.policy_docuemnts;

import io.imunity.furms.domain.policy_documents.PolicyAgreement;
import io.imunity.furms.domain.policy_documents.UserPolicyAgreements;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Set;

public interface PolicyDocumentDAO {
	void addUserPolicyAgreement(FenixUserId userId, PolicyAgreement policyAgreement);
	Set<PolicyAgreement> getPolicyAgreements(FenixUserId userId);
	Set<UserPolicyAgreements> getUserPolicyAgreements(String siteId);
}
