/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.policy_documents;

import java.time.Instant;
import java.util.Objects;

public class PolicyAcceptance {
	public final PolicyId policyDocumentId;
	public final int policyDocumentRevision;
	public final PolicyAcceptanceStatus acceptanceStatus;
	public final Instant decisionTs;

	PolicyAcceptance(PolicyId policyDocumentId,
	                 int policyDocumentRevision,
	                 PolicyAcceptanceStatus acceptanceStatus,
	                 Instant decisionTs) {
		this.policyDocumentId = policyDocumentId;
		this.policyDocumentRevision = policyDocumentRevision;
		this.acceptanceStatus = acceptanceStatus;
		this.decisionTs = decisionTs;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PolicyAcceptance that = (PolicyAcceptance) o;
		return policyDocumentRevision == that.policyDocumentRevision
				&& Objects.equals(policyDocumentId, that.policyDocumentId)
				&& acceptanceStatus == that.acceptanceStatus
				&& Objects.equals(decisionTs, that.decisionTs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(policyDocumentId, policyDocumentRevision, acceptanceStatus, decisionTs);
	}

	@Override
	public String toString() {
		return "PolicyAcceptance{" +
			"policyDocumentId=" + policyDocumentId +
			", policyDocumentRevision=" + policyDocumentRevision +
			", acceptanceStatus=" + acceptanceStatus +
			", decisionTs=" + decisionTs +
			'}';
	}

	public static PolicyAcceptanceBuilder builder() {
		return new PolicyAcceptanceBuilder();
	}

	public static final class PolicyAcceptanceBuilder {
		public PolicyId policyDocumentId;
		public int policyDocumentRevision;
		public PolicyAcceptanceStatus acceptanceStatus;
		public Instant decisionTs;

		private PolicyAcceptanceBuilder() {
		}

		public PolicyAcceptanceBuilder policyDocumentId(PolicyId policyDocumentId) {
			this.policyDocumentId = policyDocumentId;
			return this;
		}

		public PolicyAcceptanceBuilder policyDocumentRevision(int policyDocumentRevision) {
			this.policyDocumentRevision = policyDocumentRevision;
			return this;
		}

		public PolicyAcceptanceBuilder acceptanceStatus(PolicyAcceptanceStatus acceptanceStatus) {
			this.acceptanceStatus = acceptanceStatus;
			return this;
		}

		public PolicyAcceptanceBuilder decisionTs(Instant decisionTs) {
			this.decisionTs = decisionTs;
			return this;
		}

		public PolicyAcceptance build() {
			return new PolicyAcceptance(policyDocumentId, policyDocumentRevision, acceptanceStatus, decisionTs);
		}
	}
}
