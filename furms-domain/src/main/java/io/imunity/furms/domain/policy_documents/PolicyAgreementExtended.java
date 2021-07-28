/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.policy_documents;

import java.time.Instant;

public class PolicyAgreementExtended {
	public final PolicyId policyDocumentId;
	public final String siteId;
	public final int policyDocumentRevision;
	public final PolicyAgreementStatus acceptanceStatus;
	public final Instant decisionTs;

	public PolicyAgreementExtended(PolicyId policyDocumentId, String siteId, int policyDocumentRevision,
	                               PolicyAgreementStatus acceptanceStatus, Instant decisionTs) {
		this.policyDocumentId = policyDocumentId;
		this.siteId = siteId;
		this.policyDocumentRevision = policyDocumentRevision;
		this.acceptanceStatus = acceptanceStatus;
		this.decisionTs = decisionTs;
	}

	public PolicyAgreementExtended(PolicyAgreement policyAcceptance, PolicyDocument policyDocument) {
		this(policyAcceptance.policyDocumentId, policyDocument.siteId, policyAcceptance.policyDocumentRevision,
				policyAcceptance.acceptanceStatus, policyAcceptance.decisionTs);
	}

	public static PolicyAgreementExtendedBuilder builder() {
		return new PolicyAgreementExtendedBuilder();
	}


	public static final class PolicyAgreementExtendedBuilder {
		public PolicyId policyDocumentId;
		public String siteId;
		public int policyDocumentRevision;
		public PolicyAgreementStatus acceptanceStatus;
		public Instant decisionTs;

		private PolicyAgreementExtendedBuilder() {
		}

		public PolicyAgreementExtendedBuilder policyDocumentId(PolicyId policyDocumentId) {
			this.policyDocumentId = policyDocumentId;
			return this;
		}

		public PolicyAgreementExtendedBuilder siteId(String siteId) {
			this.siteId = siteId;
			return this;
		}

		public PolicyAgreementExtendedBuilder policyDocumentRevision(int policyDocumentRevision) {
			this.policyDocumentRevision = policyDocumentRevision;
			return this;
		}

		public PolicyAgreementExtendedBuilder acceptanceStatus(PolicyAgreementStatus acceptanceStatus) {
			this.acceptanceStatus = acceptanceStatus;
			return this;
		}

		public PolicyAgreementExtendedBuilder decisionTs(Instant decisionTs) {
			this.decisionTs = decisionTs;
			return this;
		}

		public PolicyAgreementExtended build() {
			return new PolicyAgreementExtended(policyDocumentId, siteId, policyDocumentRevision, acceptanceStatus, decisionTs);
		}
	}
}
