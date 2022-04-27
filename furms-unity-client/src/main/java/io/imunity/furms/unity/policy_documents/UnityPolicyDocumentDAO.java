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
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.unity.client.users.UserService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
class UnityPolicyDocumentDAO implements PolicyDocumentDAO {

	private final UserService userService;
	private final SiteRepository siteRepository;
	private final ResourceAccessRepository resourceAccessRepository;

	UnityPolicyDocumentDAO(UserService userService, SiteRepository siteRepository, ResourceAccessRepository resourceAccessRepository) {
		this.userService = userService;
		this.siteRepository = siteRepository;
		this.resourceAccessRepository = resourceAccessRepository;
	}

	public void addUserPolicyAcceptance(FenixUserId userId, PolicyAcceptance policyAcceptance){
		userService.addUserPolicyAcceptance(userId, policyAcceptance);
	}

	public Set<PolicyAcceptance> getPolicyAcceptances(FenixUserId userId) {
		return userService.getPolicyAcceptances(userId);
	}

	public Set<UserPolicyAcceptances> getUserPolicyAcceptances(SiteId siteId) {
		Map<String, Set<String>> relatedCommunityAndProjectIds = siteRepository.findRelatedProjectIds(siteId);
		if(relatedCommunityAndProjectIds.isEmpty())
			return Set.of();

		Set<FenixUserId> userIds = resourceAccessRepository.findUsersBySiteId(siteId);
		return userService.getAllUsersPolicyAcceptanceFromGroups(relatedCommunityAndProjectIds).stream()
			.filter(userPolicyAcceptances -> userPolicyAcceptances.user.fenixUserId.isPresent())
			.filter(userPolicyAcceptances -> userIds.contains(userPolicyAcceptances.user.fenixUserId.get()))
			.collect(Collectors.toSet());
	}
}
