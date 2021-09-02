/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_access;

import io.imunity.furms.api.users.UserService;
import io.imunity.furms.api.validation.exceptions.UserWithoutFenixIdValidationError;
import io.imunity.furms.domain.policy_documents.AssignedPolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.UserPolicyAcceptancesWithServicePolicies;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentDAO;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service("ResourceAccessPolicyDocumentServiceHelper")
class PolicyDocumentServiceHelper {
	private final PolicyDocumentRepository policyDocumentRepository;
	private final PolicyDocumentDAO policyDocumentDAO;
	private final SiteRepository siteRepository;
	private final UserService userService;

	PolicyDocumentServiceHelper(PolicyDocumentRepository policyDocumentRepository,
	                            PolicyDocumentDAO policyDocumentDAO,
	                            SiteRepository siteRepository,
	                            UserService userService) {
		this.policyDocumentRepository = policyDocumentRepository;
		this.policyDocumentDAO = policyDocumentDAO;
		this.siteRepository = siteRepository;
		this.userService = userService;
	}

	boolean hasUserSitePolicyAcceptance(FenixUserId userId, String siteId) {
		Site site = siteRepository.findById(siteId)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Site id %s doesn't exist", siteId)));

		PolicyDocument policyDocument = policyDocumentRepository.findById(site.getPolicyId())
			.orElseThrow(() -> new IllegalArgumentException(String.format("Policy id %s doesn't exist", site.getPolicyId())));

		return policyDocumentDAO.getPolicyAcceptances(userId)
			.stream()
			.anyMatch(x -> x.policyDocumentId.equals(policyDocument.id) && x.policyDocumentRevision == policyDocument.revision);
	}

	UserPolicyAcceptancesWithServicePolicies getUserPolicyAcceptancesWithServicePolicies(String siteId, FenixUserId fenixUserId) {
		FURMSUser user = userService.findByFenixUserId(fenixUserId)
			.orElseThrow(() -> new UserWithoutFenixIdValidationError("User not logged via Fenix Central IdP"));

		Site site = siteRepository.findById(siteId)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Site id %s doesn't exist", siteId)));

		Set<PolicyAcceptance> policyAcceptances = policyDocumentDAO.getPolicyAcceptances(fenixUserId);
		Set<AssignedPolicyDocument> allAssignPoliciesBySiteId = policyDocumentRepository.findAllAssignPoliciesBySiteId(site.getId());
		Optional<PolicyDocument> sitePolicyDocument = policyDocumentRepository.findById(site.getPolicyId());

		return new UserPolicyAcceptancesWithServicePolicies(user, policyAcceptances, sitePolicyDocument, allAssignPoliciesBySiteId);
	}
}
