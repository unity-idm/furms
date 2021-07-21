/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.policy_documents;

import java.time.Instant;
import java.util.Objects;

public class PolicyAgreement {
	public final PolicyId policyDocumentId;
	public final int policyDocumentRevision;
	public final PolicyAgreementStatus acceptanceStatus;
	public final Instant decisionTs;

	PolicyAgreement(PolicyId policyDocumentId, int policyDocumentRevision, PolicyAgreementStatus acceptanceStatus, Instant decisionTs) {
		this.policyDocumentId = policyDocumentId;
		this.policyDocumentRevision = policyDocumentRevision;
		this.acceptanceStatus = acceptanceStatus;
		this.decisionTs = decisionTs;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PolicyAgreement that = (PolicyAgreement) o;
		return policyDocumentRevision == that.policyDocumentRevision && Objects.equals(policyDocumentId, that.policyDocumentId) && acceptanceStatus == that.acceptanceStatus && Objects.equals(decisionTs, that.decisionTs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(policyDocumentId, policyDocumentRevision, acceptanceStatus, decisionTs);
	}

	@Override
	public String toString() {
		return "PolicyAgreement{" +
			"policyDocumentId=" + policyDocumentId +
			", policyDocumentRevision=" + policyDocumentRevision +
			", acceptanceStatus=" + acceptanceStatus +
			", decisionTs=" + decisionTs +
			'}';
	}

	public static PolicyAgreementBuilder builder() {
		return new PolicyAgreementBuilder();
	}

	public static final class PolicyAgreementBuilder {
		public PolicyId policyDocumentId;
		public int policyDocumentRevision;
		public PolicyAgreementStatus acceptanceStatus;
		public Instant decisionTs;

		private PolicyAgreementBuilder() {
		}

		public PolicyAgreementBuilder policyDocumentId(PolicyId policyDocumentId) {
			this.policyDocumentId = policyDocumentId;
			return this;
		}

		public PolicyAgreementBuilder policyDocumentRevision(int policyDocumentRevision) {
			this.policyDocumentRevision = policyDocumentRevision;
			return this;
		}

		public PolicyAgreementBuilder acceptanceStatus(PolicyAgreementStatus acceptanceStatus) {
			this.acceptanceStatus = acceptanceStatus;
			return this;
		}

		public PolicyAgreementBuilder decisionTs(Instant decisionTs) {
			this.decisionTs = decisionTs;
			return this;
		}

		public PolicyAgreement build() {
			return new PolicyAgreement(policyDocumentId, policyDocumentRevision, acceptanceStatus, decisionTs);
		}
	}
}
