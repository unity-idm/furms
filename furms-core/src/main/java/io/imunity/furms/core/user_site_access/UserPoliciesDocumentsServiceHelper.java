/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.user_site_access;

import io.imunity.furms.api.validation.exceptions.UserWithoutFenixIdValidationError;
import io.imunity.furms.domain.policy_documents.AssignedPolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.UserPolicyAcceptancesWithServicePolicies;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentDAO;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
class UserPoliciesDocumentsServiceHelper {
	private final PolicyDocumentRepository policyDocumentRepository;
	private final PolicyDocumentDAO policyDocumentDAO;
	private final SiteRepository siteRepository;
	private final UsersDAO usersDAO;

	UserPoliciesDocumentsServiceHelper(PolicyDocumentRepository policyDocumentRepository,
	                                   PolicyDocumentDAO policyDocumentDAO,
	                                   SiteRepository siteRepository,
	                                   UsersDAO usersDAO) {
		this.policyDocumentRepository = policyDocumentRepository;
		this.policyDocumentDAO = policyDocumentDAO;
		this.siteRepository = siteRepository;
		this.usersDAO = usersDAO;
	}

	PolicyDocument findById(PolicyId id) {
		return policyDocumentRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Policy id %s doesn't exist", id)));

	}

	Site getPolicySite(PolicyDocument policyDocument) {
		return siteRepository.findById(policyDocument.siteId)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Site id %s doesn't exist", policyDocument.siteId)));
	}

	boolean hasUserSitePolicyAcceptance(FenixUserId userId, String siteId) {
		Site site = siteRepository.findById(siteId)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Site id %s doesn't exist", siteId)));

		if(site.getPolicyId().id == null)
			return false;

		Optional<PolicyDocument> policyDocument = policyDocumentRepository.findById(site.getPolicyId());
		return policyDocumentDAO.getPolicyAcceptances(userId)
			.stream()
			.anyMatch(
				policyAcceptance -> policyAcceptance.policyDocumentId.equals(policyDocument.get().id) &&
				policyAcceptance.policyDocumentRevision == policyDocument.get().revision
			);
	}

	boolean hasSitePolicy(String siteId) {
		return siteRepository.findById(siteId).stream()
			.anyMatch(site -> site.getPolicyId().id != null);
	}

	UserPolicyAcceptancesWithServicePolicies getUserPolicyAcceptancesWithServicePolicies(String siteId, FenixUserId fenixUserId) {
		FURMSUser user = usersDAO.findById(fenixUserId)
			.orElseThrow(() -> new UserWithoutFenixIdValidationError("User not logged via Fenix Central IdP"));

		Site site = siteRepository.findById(siteId)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Site id %s doesn't exist", siteId)));

		Set<PolicyAcceptance> policyAcceptances = policyDocumentDAO.getPolicyAcceptances(fenixUserId);
		Set<AssignedPolicyDocument> allAssignedPoliciesBySiteId = policyDocumentRepository.findAllAssignPoliciesBySiteId(site.getId());
		Optional<PolicyDocument> sitePolicyDocument = site.getPolicyId().id != null ? policyDocumentRepository.findById(site.getPolicyId()) : Optional.empty();

		return new UserPolicyAcceptancesWithServicePolicies(user, policyAcceptances, sitePolicyDocument, allAssignedPoliciesBySiteId);
	}
}
