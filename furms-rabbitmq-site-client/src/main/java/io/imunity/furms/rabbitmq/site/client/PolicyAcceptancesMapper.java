/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.policy_documents.PolicyAcceptanceStatus;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.ServicePolicyDocument;
import io.imunity.furms.domain.policy_documents.UserPolicyAcceptancesWithServicePolicies;
import io.imunity.furms.rabbitmq.site.models.Acceptance;
import io.imunity.furms.rabbitmq.site.models.PolicyAcceptance;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

class PolicyAcceptancesMapper {

	public static List<PolicyAcceptance> getPolicyAcceptances(UserPolicyAcceptancesWithServicePolicies userPolicyAcceptances) {
		Map<PolicyId, ServicePolicyDocument> servicePolicyDocumentMap = userPolicyAcceptances.servicePolicyDocuments.stream().collect(Collectors.toMap(x -> x.id, Function.identity()));

		return userPolicyAcceptances.policyAcceptances.stream()
			.map(x -> PolicyAcceptance.builder()
				.policyIdentifier(x.policyDocumentId.id.toString())
				.currentVersion(servicePolicyDocumentMap.get(x.policyDocumentId).revision)
				.processedVersion(x.policyDocumentRevision)
				.serviceIdentifier(Optional.ofNullable(servicePolicyDocumentMap.get(x.policyDocumentId)).map(y -> y.serviceId).orElse(null))
				.acceptanceStatus(getAcceptanceStatus(x.acceptanceStatus))
				.build()
			).collect(Collectors.toList());
	}

	private static Acceptance getAcceptanceStatus(PolicyAcceptanceStatus status) {
		switch (status) {
			case ACCEPTED: return Acceptance.ACCEPTED;
			case NOT_ACCEPTED: return Acceptance.REJECTED;
			default: return Acceptance.UNKNOWN;
		}
	}
}
