/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.policy_documents;

import java.time.Instant;
import java.util.Objects;

public class PolicyAcceptanceAtSite {
	public final PolicyId policyDocumentId;
	public final String siteId;
	public final int policyDocumentRevision;
	public final int acceptedPolicyDocumentRevision;
	public final PolicyAcceptanceStatus acceptanceStatus;
	public final Instant decisionTs;

	public PolicyAcceptanceAtSite(PolicyId policyDocumentId,
	                              String siteId,
	                              int policyDocumentRevision,
	                              int acceptedPolicyDocumentRevision,
	                              PolicyAcceptanceStatus acceptanceStatus,
	                              Instant decisionTs) {
		this.policyDocumentId = policyDocumentId;
		this.siteId = siteId;
		this.policyDocumentRevision = policyDocumentRevision;
		this.acceptedPolicyDocumentRevision = acceptedPolicyDocumentRevision;
		this.acceptanceStatus = acceptanceStatus;
		this.decisionTs = decisionTs;
	}

	public PolicyAcceptanceAtSite(PolicyAcceptance policyAcceptance, PolicyDocument policyDocument) {
		this(policyAcceptance.policyDocumentId, policyDocument.siteId, policyDocument.revision,
				policyAcceptance.policyDocumentRevision, policyAcceptance.acceptanceStatus, policyAcceptance.decisionTs);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PolicyAcceptanceAtSite that = (PolicyAcceptanceAtSite) o;
		return policyDocumentRevision == that.policyDocumentRevision
				&& acceptedPolicyDocumentRevision == that.acceptedPolicyDocumentRevision
				&& Objects.equals(policyDocumentId, that.policyDocumentId)
				&& Objects.equals(siteId, that.siteId)
				&& acceptanceStatus == that.acceptanceStatus
				&& Objects.equals(decisionTs, that.decisionTs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(policyDocumentId, siteId, policyDocumentRevision, acceptedPolicyDocumentRevision, acceptanceStatus, decisionTs);
	}

	@Override
	public String toString() {
		return "PolicyAcceptanceAtSite{" +
				"policyDocumentId=" + policyDocumentId +
				", siteId='" + siteId + '\'' +
				", policyDocumentRevision=" + policyDocumentRevision +
				", acceptedPolicyDocumentRevision=" + acceptedPolicyDocumentRevision +
				", acceptanceStatus=" + acceptanceStatus +
				", decisionTs=" + decisionTs +
				'}';
	}

	public static PolicyAcceptanceAtSiteBuilder builder() {
		return new PolicyAcceptanceAtSiteBuilder();
	}


	public static final class PolicyAcceptanceAtSiteBuilder {
		public PolicyId policyDocumentId;
		public String siteId;
		public int policyDocumentRevision;
		public int acceptedPolicyDocumentRevision;
		public PolicyAcceptanceStatus acceptanceStatus;
		public Instant decisionTs;

		private PolicyAcceptanceAtSiteBuilder() {
		}

		public PolicyAcceptanceAtSiteBuilder policyDocumentId(PolicyId policyDocumentId) {
			this.policyDocumentId = policyDocumentId;
			return this;
		}

		public PolicyAcceptanceAtSiteBuilder siteId(String siteId) {
			this.siteId = siteId;
			return this;
		}

		public PolicyAcceptanceAtSiteBuilder policyDocumentRevision(int policyDocumentRevision) {
			this.policyDocumentRevision = policyDocumentRevision;
			return this;
		}

		public PolicyAcceptanceAtSiteBuilder acceptedPolicyDocumentRevision(int acceptedPolicyDocumentRevision) {
			this.acceptedPolicyDocumentRevision = acceptedPolicyDocumentRevision;
			return this;
		}

		public PolicyAcceptanceAtSiteBuilder acceptanceStatus(PolicyAcceptanceStatus acceptanceStatus) {
			this.acceptanceStatus = acceptanceStatus;
			return this;
		}

		public PolicyAcceptanceAtSiteBuilder decisionTs(Instant decisionTs) {
			this.decisionTs = decisionTs;
			return this;
		}

		public PolicyAcceptanceAtSite build() {
			return new PolicyAcceptanceAtSite(policyDocumentId, siteId, policyDocumentRevision, acceptedPolicyDocumentRevision, acceptanceStatus, decisionTs);
		}
	}
}
