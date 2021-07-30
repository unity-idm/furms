/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.policy_documents;

import java.time.Instant;
import java.util.Objects;

public class PolicyAcceptanceExtended {
	public final PolicyId policyDocumentId;
	public final String siteId;
	public final int policyDocumentRevision;
	public final PolicyAcceptanceStatus acceptanceStatus;
	public final Instant decisionTs;

	public PolicyAcceptanceExtended(PolicyId policyDocumentId, String siteId, int policyDocumentRevision,
	                                PolicyAcceptanceStatus acceptanceStatus, Instant decisionTs) {
		this.policyDocumentId = policyDocumentId;
		this.siteId = siteId;
		this.policyDocumentRevision = policyDocumentRevision;
		this.acceptanceStatus = acceptanceStatus;
		this.decisionTs = decisionTs;
	}

	public PolicyAcceptanceExtended(PolicyAcceptance policyAcceptance, PolicyDocument policyDocument) {
		this(policyAcceptance.policyDocumentId, policyDocument.siteId, policyAcceptance.policyDocumentRevision,
				policyAcceptance.acceptanceStatus, policyAcceptance.decisionTs);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PolicyAcceptanceExtended that = (PolicyAcceptanceExtended) o;
		return policyDocumentRevision == that.policyDocumentRevision && Objects.equals(policyDocumentId, that.policyDocumentId) && Objects.equals(siteId, that.siteId) && acceptanceStatus == that.acceptanceStatus && Objects.equals(decisionTs, that.decisionTs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(policyDocumentId, siteId, policyDocumentRevision, acceptanceStatus, decisionTs);
	}

	@Override
	public String toString() {
		return "PolicyAcceptanceExtended{" +
				"policyDocumentId=" + policyDocumentId +
				", siteId='" + siteId + '\'' +
				", policyDocumentRevision=" + policyDocumentRevision +
				", acceptanceStatus=" + acceptanceStatus +
				", decisionTs=" + decisionTs +
				'}';
	}

	public static PolicyAcceptanceExtendedBuilder builder() {
		return new PolicyAcceptanceExtendedBuilder();
	}


	public static final class PolicyAcceptanceExtendedBuilder {
		public PolicyId policyDocumentId;
		public String siteId;
		public int policyDocumentRevision;
		public PolicyAcceptanceStatus acceptanceStatus;
		public Instant decisionTs;

		private PolicyAcceptanceExtendedBuilder() {
		}

		public PolicyAcceptanceExtendedBuilder policyDocumentId(PolicyId policyDocumentId) {
			this.policyDocumentId = policyDocumentId;
			return this;
		}

		public PolicyAcceptanceExtendedBuilder siteId(String siteId) {
			this.siteId = siteId;
			return this;
		}

		public PolicyAcceptanceExtendedBuilder policyDocumentRevision(int policyDocumentRevision) {
			this.policyDocumentRevision = policyDocumentRevision;
			return this;
		}

		public PolicyAcceptanceExtendedBuilder acceptanceStatus(PolicyAcceptanceStatus acceptanceStatus) {
			this.acceptanceStatus = acceptanceStatus;
			return this;
		}

		public PolicyAcceptanceExtendedBuilder decisionTs(Instant decisionTs) {
			this.decisionTs = decisionTs;
			return this;
		}

		public PolicyAcceptanceExtended build() {
			return new PolicyAcceptanceExtended(policyDocumentId, siteId, policyDocumentRevision, acceptanceStatus, decisionTs);
		}
	}
}
