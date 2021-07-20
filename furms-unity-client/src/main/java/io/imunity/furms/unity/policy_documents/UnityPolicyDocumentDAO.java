/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.policy_documents;

import io.imunity.furms.domain.policy_documents.PolicyAgreement;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentDAO;
import io.imunity.furms.unity.client.users.UserService;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
class UnityPolicyDocumentDAO implements PolicyDocumentDAO {

	private final UserService userService;

	UnityPolicyDocumentDAO(UserService userService) {
		this.userService = userService;
	}

	public void addUserPolicyAgreement(FenixUserId userId, PolicyAgreement policyAgreement){
		userService.addUserPolicyAgreement(userId, policyAgreement);
	}

	public Set<PolicyAgreement> getPolicyAgreements(FenixUserId userId) {
		return userService.getPolicyAgreements(userId);
	}
}
