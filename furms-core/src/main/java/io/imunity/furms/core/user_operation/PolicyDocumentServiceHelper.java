/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.user_operation;

import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyAcceptanceAtSite;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentDAO;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.POLICY_ACCEPTANCE_MAINTENANCE;
import static io.imunity.furms.domain.policy_documents.PolicyAcceptanceStatus.ACCEPTED;
import static java.util.stream.Collectors.toSet;

@Service
class PolicyDocumentServiceHelper {
	private static final Logger LOG = LoggerFactory.getLogger(PolicyDocumentServiceHelper.class);

	private final PolicyDocumentRepository policyDocumentRepository;
	private final PolicyDocumentDAO policyDocumentDAO;

	PolicyDocumentServiceHelper(PolicyDocumentRepository policyDocumentRepository,
	                            PolicyDocumentDAO policyDocumentDAO) {
		this.policyDocumentRepository = policyDocumentRepository;
		this.policyDocumentDAO = policyDocumentDAO;
	}

	@FurmsAuthorize(capability = POLICY_ACCEPTANCE_MAINTENANCE)
	public Set<PolicyAcceptanceAtSite> findSitePolicyAcceptancesByUserId(FenixUserId userId) {
		final Set<PolicyDocument> userPolicies = policyDocumentRepository.findAllSitePoliciesByUserId(userId);
		return findPolicyAcceptancesByUserIdFilterByPolicies(userId, userPolicies);
	}

	@FurmsAuthorize(capability = POLICY_ACCEPTANCE_MAINTENANCE)
	public Set<PolicyAcceptanceAtSite> findServicesPolicyAcceptancesByUserId(FenixUserId userId) {
		final Set<PolicyDocument> userPolicies = policyDocumentRepository.findAllServicePoliciesByUserId(userId);
		return findPolicyAcceptancesByUserIdFilterByPolicies(userId, userPolicies);
	}

	private Set<PolicyAcceptanceAtSite> findPolicyAcceptancesByUserIdFilterByPolicies(FenixUserId userId,
	                                                                                  Set<PolicyDocument> userPolicies) {
		return findPolicyAcceptancesByUserId(userId).stream()
			.filter(policyAcceptance -> policyAcceptance.acceptanceStatus == ACCEPTED)
			.map(policyAcceptance -> userPolicies.stream()
				.filter(userPolicy -> isPolicyRelatedToAcceptance(userPolicy, policyAcceptance))
				.findFirst()
				.map(policyDocument -> new PolicyAcceptanceAtSite(policyAcceptance, policyDocument))
				.orElse(null))
			.filter(Objects::nonNull)
			.collect(toSet());
	}

	private boolean isPolicyRelatedToAcceptance(PolicyDocument userPolicy, PolicyAcceptance policyAcceptance) {
		return userPolicy.id.equals(policyAcceptance.policyDocumentId);
	}

	private Set<PolicyAcceptance> findPolicyAcceptancesByUserId(FenixUserId fenixUserId) {
		LOG.debug("Getting all Policy Document for fenix user id={}", fenixUserId.id);
		return policyDocumentDAO.getPolicyAcceptances(fenixUserId);
	}
}
