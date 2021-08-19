/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.policy_documents;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyAcceptanceAtSite;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentDAO;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.POLICY_ACCEPTANCE_MAINTENANCE;
import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;
import static io.imunity.furms.domain.policy_documents.PolicyAcceptanceStatus.ACCEPTED;
import static java.util.stream.Collectors.toSet;

@Service
public class PolicyDocumentServiceHelper {
	private static final Logger LOG = LoggerFactory.getLogger(PolicyDocumentServiceImpl.class);

	private final AuthzService authzService;
	private final PolicyDocumentRepository policyDocumentRepository;
	private final PolicyDocumentDAO policyDocumentDAO;

	PolicyDocumentServiceHelper(AuthzService authzService, PolicyDocumentRepository policyDocumentRepository, PolicyDocumentDAO policyDocumentDAO) {
		this.authzService = authzService;
		this.policyDocumentRepository = policyDocumentRepository;
		this.policyDocumentDAO = policyDocumentDAO;
	}

	@FurmsAuthorize(capability = POLICY_ACCEPTANCE_MAINTENANCE, resourceType = APP_LEVEL)
	public Set<PolicyAcceptanceAtSite> findSitePolicyAcceptancesByUserId(PersistentId userId) {
		final Set<PolicyDocument> userPolicies = policyDocumentRepository.findAllSitePoliciesByUserId(userId);
		return findPolicyAcceptancesByUserIdFilterByPolicies(userId, userPolicies);
	}

	@FurmsAuthorize(capability = POLICY_ACCEPTANCE_MAINTENANCE, resourceType = APP_LEVEL)
	public Set<PolicyAcceptanceAtSite> findServicesPolicyAcceptancesByUserId(PersistentId userId) {
		final Set<PolicyDocument> userPolicies = policyDocumentRepository.findAllServicePoliciesByUserId(userId);
		return findPolicyAcceptancesByUserIdFilterByPolicies(userId, userPolicies);
	}

	private Set<PolicyAcceptanceAtSite> findPolicyAcceptancesByUserIdFilterByPolicies(PersistentId userId,
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
		return userPolicy.id.equals(policyAcceptance.policyDocumentId)
			&& userPolicy.revision == policyAcceptance.policyDocumentRevision;
	}

	private Set<PolicyAcceptance> findPolicyAcceptancesByUserId(PersistentId userId) {
		final FenixUserId fenixUserId = authzService.getCurrentAuthNUser().fenixUserId
			.orElseThrow(() -> new IllegalArgumentException("User have to be central IDP user"));

		LOG.debug("Getting all Policy Document for user id={}", userId.id);
		return policyDocumentDAO.getPolicyAcceptances(fenixUserId);
	}
}
