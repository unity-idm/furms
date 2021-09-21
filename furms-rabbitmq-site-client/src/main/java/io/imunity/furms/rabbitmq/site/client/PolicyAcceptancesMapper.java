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

import static java.util.Comparator.comparing;
import static java.util.function.BinaryOperator.maxBy;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

class PolicyAcceptancesMapper {

	static List<PolicyAcceptance> getPolicyAcceptances(UserPolicyAcceptancesWithServicePolicies userPolicyAcceptances) {
		Map<PolicyId, List<AssignedPolicyDocument>> servicePolicyDocumentMap = userPolicyAcceptances.servicePolicyDocuments.stream()
			.collect(groupingBy(x -> x.id));

		Optional<AssignedPolicyDocument> sitePolicyDocument = userPolicyAcceptances.sitePolicy
			.map(policyDocument -> new AssignedPolicyDocument(policyDocument.id, null, policyDocument.name, policyDocument.revision));
		return userPolicyAcceptances.policyAcceptances.stream()
			.collect(
				toMap(
					policyAcceptance -> policyAcceptance.policyDocumentId,
					identity(),
					maxBy(comparing(policyAcceptance -> policyAcceptance.policyDocumentRevision)))
			)
			.values().stream()
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
			)
			.distinct()
			.collect(toList());
	}

	private static List<AssignedPolicyDocument> getAssignedPolicyDocument(Optional<AssignedPolicyDocument> sitePolicyDocument, io.imunity.furms.domain.policy_documents.PolicyAcceptance policyAcceptance) {
		return sitePolicyDocument.stream()
			.filter(sitePolicy -> sitePolicy.id.equals(policyAcceptance.policyDocumentId))
			.collect(toList());
	}

	private static Acceptance getAcceptanceStatus(PolicyAcceptanceStatus status) {
		switch (status) {
			case ACCEPTED: return Acceptance.ACCEPTED;
			case NOT_ACCEPTED: return Acceptance.REJECTED;
			default: return Acceptance.UNKNOWN;
		}
	}
}
