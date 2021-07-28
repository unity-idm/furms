/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.cidp;

import io.imunity.furms.domain.policy_documents.PolicyAgreementExtended;
import io.imunity.furms.domain.policy_documents.PolicyAgreementStatus;
import io.imunity.furms.utils.UTCTimeUtils;

import java.time.ZonedDateTime;
import java.util.Objects;

class PolicyAcceptanceJson {
	public final String policyId;
	public final PolicyAgreementStatus accepted;
	public final ZonedDateTime processedOn;
	public final int revision;

	public PolicyAcceptanceJson(PolicyAgreementExtended policyAcceptance) {
		this.policyId = policyAcceptance.policyDocumentId.id.toString();
		this.accepted = policyAcceptance.acceptanceStatus;
		this.processedOn = UTCTimeUtils.convertToZoneTime(policyAcceptance.decisionTs);
		this.revision = policyAcceptance.policyDocumentRevision;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PolicyAcceptanceJson that = (PolicyAcceptanceJson) o;
		return revision == that.revision
				&& Objects.equals(policyId, that.policyId)
				&& accepted == that.accepted
				&& Objects.equals(processedOn, that.processedOn);
	}

	@Override
	public int hashCode() {
		return Objects.hash(policyId, accepted, processedOn, revision);
	}

	@Override
	public String toString() {
		return "PolicyAcceptanceJson{" +
				", policyId='" + policyId + '\'' +
				", accepted=" + accepted +
				", processedOn=" + processedOn +
				", revision=" + revision +
				'}';
	}
}
