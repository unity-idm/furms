/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import io.imunity.furms.domain.policy_documents.PolicyAcceptanceStatus;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.users.FenixUserId;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;

class PolicyAcceptance {
	public final String fenixUserId;
	public final String policyId;
	public final AcceptanceStatus accepted;
	public final ZonedDateTime processedOn;
	public final int revision;

	PolicyAcceptance(String policyId,
	                 int revision,
	                 String fenixUserId,
	                 AcceptanceStatus accepted,
	                 ZonedDateTime processedOn) {
		this.policyId = policyId;
		this.revision = revision;
		this.fenixUserId = fenixUserId;
		this.accepted = accepted;
		this.processedOn = processedOn;
	}

	public PolicyAcceptance(io.imunity.furms.domain.policy_documents.PolicyAcceptance policyAcceptance,
	                        FenixUserId fenixUserId) {
		this(policyAcceptance.policyDocumentId.id.toString(),
				policyAcceptance.policyDocumentRevision,
				fenixUserId.id,
				AcceptanceStatus.valeOf(policyAcceptance.acceptanceStatus),
				policyAcceptance.decisionTs.atZone(ZoneOffset.UTC));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PolicyAcceptance that = (PolicyAcceptance) o;
		return revision == that.revision &&
			Objects.equals(fenixUserId, that.fenixUserId) &&
			Objects.equals(policyId, that.policyId) &&
			accepted == that.accepted &&
			Objects.equals(processedOn, that.processedOn);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fenixUserId, policyId, accepted, processedOn, revision);
	}

	@Override
	public String toString() {
		return "PolicyAcceptance{" +
			"fenixUserId='" + fenixUserId + '\'' +
			", policyId='" + policyId + '\'' +
			", accepted=" + accepted +
			", processedOn=" + processedOn +
			", revision=" + revision +
			'}';
	}

	public static PolicyAcceptance.PolicyAcceptanceBuilder builder() {
		return new PolicyAcceptance.PolicyAcceptanceBuilder();
	}

	public static final class PolicyAcceptanceBuilder {
		private String policyId;
		private int revision;
		private String fenixUserId;
		private AcceptanceStatus accepted;
		private ZonedDateTime processedOn;

		private PolicyAcceptanceBuilder() {
		}

		public PolicyAcceptance.PolicyAcceptanceBuilder policyId(PolicyId policyId) {
			this.policyId = policyId.id.toString();
			return this;
		}

		public PolicyAcceptance.PolicyAcceptanceBuilder policyId(String policyId) {
			this.policyId = policyId;
			return this;
		}

		public PolicyAcceptance.PolicyAcceptanceBuilder revision(int revision) {
			this.revision = revision;
			return this;
		}

		public PolicyAcceptance.PolicyAcceptanceBuilder fenixUserId(FenixUserId fenixUserId) {
			this.fenixUserId = fenixUserId.id;
			return this;
		}

		public PolicyAcceptance.PolicyAcceptanceBuilder fenixUserId(String fenixUserId) {
			this.fenixUserId = fenixUserId;
			return this;
		}

		public PolicyAcceptance.PolicyAcceptanceBuilder acceptanceStatus(PolicyAcceptanceStatus acceptanceStatus) {
			this.accepted = AcceptanceStatus.valeOf(acceptanceStatus);
			return this;
		}

		public PolicyAcceptance.PolicyAcceptanceBuilder acceptanceStatus(AcceptanceStatus acceptanceStatus) {
			this.accepted = acceptanceStatus;
			return this;
		}

		public PolicyAcceptance.PolicyAcceptanceBuilder decisionTs(ZonedDateTime processedOn) {
			this.processedOn = processedOn;
			return this;
		}

		public PolicyAcceptance.PolicyAcceptanceBuilder decisionTs(Instant processedOn) {
			this.processedOn = processedOn.atZone(ZoneOffset.UTC);
			return this;
		}

		public PolicyAcceptance build() {
			return new PolicyAcceptance(policyId, revision, fenixUserId, accepted, processedOn);
		}
	}

}
