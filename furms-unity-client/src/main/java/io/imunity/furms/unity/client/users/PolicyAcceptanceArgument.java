/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.users;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyAcceptanceStatus;
import io.imunity.furms.domain.policy_documents.PolicyId;

import java.time.Instant;
import java.util.Objects;

class PolicyAcceptanceArgument {
	public final String policyDocumentId;
	public final int policyDocumentRevision;
	public final String acceptanceStatus;
	public final Instant decisionTs;

	@JsonCreator
	public PolicyAcceptanceArgument(String policyDocumentId, int policyDocumentRevision, String acceptanceStatus, Instant decisionTs) {
		this.policyDocumentId = policyDocumentId;
		this.policyDocumentRevision = policyDocumentRevision;
		this.acceptanceStatus = acceptanceStatus;
		this.decisionTs = decisionTs;
	}

	PolicyAcceptance toPolicyAcceptance(){
		return PolicyAcceptance.builder()
			.policyDocumentId(new PolicyId(policyDocumentId))
			.policyDocumentRevision(policyDocumentRevision)
			.acceptanceStatus(PolicyAcceptanceStatus.valueOf(acceptanceStatus))
			.decisionTs(decisionTs)
			.build();
	}

	static PolicyAcceptanceArgument valueOf(PolicyAcceptance policyAcceptance){
		return new PolicyAcceptanceArgument(policyAcceptance.policyDocumentId.id.toString(),
			policyAcceptance.policyDocumentRevision, policyAcceptance.acceptanceStatus.name(), policyAcceptance.decisionTs);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PolicyAcceptanceArgument that = (PolicyAcceptanceArgument) o;
		return policyDocumentRevision == that.policyDocumentRevision
				&& Objects.equals(policyDocumentId, that.policyDocumentId)
				&& Objects.equals(acceptanceStatus, that.acceptanceStatus)
				&& Objects.equals(decisionTs, that.decisionTs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(policyDocumentId, policyDocumentRevision, acceptanceStatus, decisionTs);
	}

	@Override
	public String toString() {
		return "PolicyAcceptanceArgument{" +
			"policyDocumentId='" + policyDocumentId + '\'' +
			", policyDocumentRevision=" + policyDocumentRevision +
			", acceptanceStatus='" + acceptanceStatus + '\'' +
			", decisionTs=" + decisionTs +
			'}';
	}
}
