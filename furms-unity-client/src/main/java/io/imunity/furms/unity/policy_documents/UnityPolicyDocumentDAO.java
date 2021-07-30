/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.policy_documents;

import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.UserPolicyAcceptances;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentDAO;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.unity.client.users.UserService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
class UnityPolicyDocumentDAO implements PolicyDocumentDAO {

	private final UserService userService;
	private final SiteRepository siteRepository;

	UnityPolicyDocumentDAO(UserService userService, SiteRepository siteRepository) {
		this.userService = userService;
		this.siteRepository = siteRepository;
	}

	public void addUserPolicyAcceptance(FenixUserId userId, PolicyAcceptance policyAcceptance){
		userService.addUserPolicyAcceptance(userId, policyAcceptance);
	}

	public Set<PolicyAcceptance> getyPolicyAcceptances(FenixUserId userId) {
		return userService.getPolicyAcceptances(userId);
	}

	public Set<UserPolicyAcceptances> getUserPolicyAcceptances(String siteId) {
		Map<String, Set<String>> relatedCommunityAndProjectIds = siteRepository.findRelatedProjectIds(new SiteId(siteId, ""));
		return userService.getAllUsersPolicyAcceptanceFromGroups("/", relatedCommunityAndProjectIds);
	}
}
