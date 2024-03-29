/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.users.FenixUserId;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;

class PolicyAcceptance {
	public final String fenixUserId;
	public final String policyId;
	public final AcceptanceStatus acceptanceStatus;
	public final ZonedDateTime processedOn;
	public final int currentPolicyRevision;
	public final Integer acceptedRevision;

	PolicyAcceptance(String policyId,
	                 int currentPolicyRevision,
	                 Integer acceptedRevision,
	                 String fenixUserId,
	                 AcceptanceStatus acceptanceStatus,
	                 ZonedDateTime processedOn) {
		this.policyId = policyId;
		this.currentPolicyRevision = currentPolicyRevision;
		this.acceptedRevision = acceptedRevision;
		this.fenixUserId = fenixUserId;
		this.acceptanceStatus = acceptanceStatus;
		this.processedOn = processedOn;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PolicyAcceptance that = (PolicyAcceptance) o;
		return acceptedRevision.equals(that.acceptedRevision) &&
			Objects.equals(fenixUserId, that.fenixUserId) &&
			Objects.equals(policyId, that.policyId) &&
				acceptanceStatus == that.acceptanceStatus &&
			Objects.equals(processedOn, that.processedOn);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fenixUserId, policyId, acceptanceStatus, processedOn, acceptedRevision);
	}

	@Override
	public String toString() {
		return "PolicyAcceptance{" +
			"fenixUserId='" + fenixUserId + '\'' +
			", policyId='" + policyId + '\'' +
			", acceptanceStatus=" + acceptanceStatus +
			", processedOn=" + processedOn +
			", revision=" + acceptedRevision +
			'}';
	}

	public static PolicyAcceptance.PolicyAcceptanceBuilder builder() {
		return new PolicyAcceptance.PolicyAcceptanceBuilder();
	}

	public static final class PolicyAcceptanceBuilder {
		private String policyId;
		private int currentPolicyRevision;
		private Integer acceptedRevision;
		private String fenixUserId;
		private AcceptanceStatus acceptanceStatus;
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

		public PolicyAcceptance.PolicyAcceptanceBuilder revision(int currentPolicyRevision) {
			this.currentPolicyRevision = currentPolicyRevision;
			return this;
		}

		public PolicyAcceptance.PolicyAcceptanceBuilder acceptedRevision(Integer acceptedRevision) {
			this.acceptedRevision = acceptedRevision;
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

		public PolicyAcceptance.PolicyAcceptanceBuilder acceptanceStatus(AcceptanceStatus acceptanceStatus) {
			this.acceptanceStatus = acceptanceStatus;
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
			return new PolicyAcceptance(policyId, currentPolicyRevision, acceptedRevision, fenixUserId, acceptanceStatus,
					processedOn);
		}
	}

}
