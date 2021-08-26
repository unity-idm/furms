/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.policy_documents.AssignedPolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyAcceptanceStatus;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.UserPolicyAcceptancesWithServicePolicies;
import io.imunity.furms.rabbitmq.site.models.Acceptance;
import io.imunity.furms.rabbitmq.site.models.PolicyAcceptance;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

class PolicyAcceptancesMapper {

	static List<PolicyAcceptance> getPolicyAcceptances(UserPolicyAcceptancesWithServicePolicies userPolicyAcceptances) {
		Map<PolicyId, List<AssignedPolicyDocument>> servicePolicyDocumentMap = userPolicyAcceptances.servicePolicyDocuments.stream()
			.collect(Collectors.groupingBy(x -> x.id));

		Optional<AssignedPolicyDocument> sitePolicyDocument = userPolicyAcceptances.sitePolicy
			.map(policyDocument -> new AssignedPolicyDocument(policyDocument.id, null, policyDocument.name, policyDocument.revision));
		return userPolicyAcceptances.policyAcceptances.stream()
			.flatMap(policyAcceptance ->
				servicePolicyDocumentMap.getOrDefault(policyAcceptance.policyDocumentId, getAssignedPolicyDocument(sitePolicyDocument, policyAcceptance))
				.stream()
				.map(servicePolicyDocument -> PolicyAcceptance.builder()
					.policyIdentifier(policyAcceptance.policyDocumentId.id.toString())
					.currentVersion(servicePolicyDocument.revision)
					.processedVersion(policyAcceptance.policyDocumentRevision)
					.serviceIdentifier(servicePolicyDocument.serviceId)
					.acceptanceStatus(getAcceptanceStatus(policyAcceptance.acceptanceStatus))
					.build())
			).collect(Collectors.toList());
	}

	private static List<AssignedPolicyDocument> getAssignedPolicyDocument(Optional<AssignedPolicyDocument> sitePolicyDocument, io.imunity.furms.domain.policy_documents.PolicyAcceptance policyAcceptance) {
		return sitePolicyDocument.stream()
			.filter(sitePolicy -> sitePolicy.id.equals(policyAcceptance.policyDocumentId))
			.collect(Collectors.toList());
	}

	private static Acceptance getAcceptanceStatus(PolicyAcceptanceStatus status) {
		switch (status) {
			case ACCEPTED: return Acceptance.ACCEPTED;
			case NOT_ACCEPTED: return Acceptance.REJECTED;
			default: return Acceptance.UNKNOWN;
		}
	}
}