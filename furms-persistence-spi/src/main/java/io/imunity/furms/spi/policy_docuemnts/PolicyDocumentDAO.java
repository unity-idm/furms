/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.policy_docuemnts;

import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.UserPolicyAcceptances;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Set;

public interface PolicyDocumentDAO {
	void addUserPolicyAgreement(FenixUserId userId, PolicyAcceptance policyAcceptance);
	Set<PolicyAcceptance> getPolicyAgreements(FenixUserId userId);
	Set<UserPolicyAcceptances> getUserPolicyAcceptances(String siteId);
}
