/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.users;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.imunity.furms.domain.policy_documents.PolicyAgreement;
import io.imunity.furms.domain.policy_documents.PolicyAgreementStatus;
import io.imunity.furms.domain.policy_documents.PolicyId;

import java.time.Instant;
import java.util.Objects;

class PolicyAgreementArgument {
	public final String policyDocumentId;
	public final int policyDocumentRevision;
	public final String acceptanceStatus;
	public final Instant decisionTs;

	@JsonCreator
	public PolicyAgreementArgument(String policyDocumentId, int policyDocumentRevision, String acceptanceStatus, Instant decisionTs) {
		this.policyDocumentId = policyDocumentId;
		this.policyDocumentRevision = policyDocumentRevision;
		this.acceptanceStatus = acceptanceStatus;
		this.decisionTs = decisionTs;
	}

	PolicyAgreement toPolicyAgreement(){
		return PolicyAgreement.builder()
			.policyDocumentId(new PolicyId(policyDocumentId))
			.policyDocumentRevision(policyDocumentRevision)
			.acceptanceStatus(PolicyAgreementStatus.valueOf(acceptanceStatus))
			.decisionTs(decisionTs)
			.build();
	}

	static PolicyAgreementArgument valueOf(PolicyAgreement policyAgreement){
		return new PolicyAgreementArgument(policyAgreement.policyDocumentId.id.toString(),
			policyAgreement.policyDocumentRevision, policyAgreement.acceptanceStatus.name(), policyAgreement.decisionTs);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PolicyAgreementArgument that = (PolicyAgreementArgument) o;
		return policyDocumentRevision == that.policyDocumentRevision && Objects.equals(policyDocumentId, that.policyDocumentId) && Objects.equals(acceptanceStatus, that.acceptanceStatus) && Objects.equals(decisionTs, that.decisionTs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(policyDocumentId, policyDocumentRevision, acceptanceStatus, decisionTs);
	}

	@Override
	public String toString() {
		return "PolicyAgreementArgument{" +
			"policyDocumentId='" + policyDocumentId + '\'' +
			", policyDocumentRevision=" + policyDocumentRevision +
			", acceptanceStatus='" + acceptanceStatus + '\'' +
			", decisionTs=" + decisionTs +
			'}';
	}
}
