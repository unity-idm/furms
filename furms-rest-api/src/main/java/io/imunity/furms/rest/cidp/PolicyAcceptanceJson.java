/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.cidp;

import io.imunity.furms.domain.policy_documents.PolicyAcceptanceAtSite;
import io.imunity.furms.domain.policy_documents.PolicyAcceptanceStatus;
import io.imunity.furms.utils.UTCTimeUtils;

import java.time.ZonedDateTime;
import java.util.Objects;

class PolicyAcceptanceJson {
	public final String policyId;
	public final PolicyAcceptanceStatus acceptanceStatus;
	public final ZonedDateTime processedOn;
	public final int currentVersion;
	public final int processedVersion;

	public PolicyAcceptanceJson(PolicyAcceptanceAtSite policyAcceptance) {
		this.policyId = policyAcceptance.policyDocumentId.id.toString();
		this.acceptanceStatus = policyAcceptance.acceptanceStatus;
		this.processedOn = UTCTimeUtils.convertToZoneTime(policyAcceptance.decisionTs);
		this.currentVersion = policyAcceptance.policyDocumentRevision;
		this.processedVersion = policyAcceptance.acceptedPolicyDocumentRevision;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PolicyAcceptanceJson that = (PolicyAcceptanceJson) o;
		return currentVersion == that.currentVersion
				&& processedVersion == that.processedVersion
				&& Objects.equals(policyId, that.policyId)
				&& acceptanceStatus == that.acceptanceStatus
				&& Objects.equals(processedOn, that.processedOn);
	}

	@Override
	public int hashCode() {
		return Objects.hash(policyId, acceptanceStatus, processedOn, currentVersion, processedVersion);
	}

	@Override
	public String toString() {
		return "PolicyAcceptanceJson{" +
				"policyId='" + policyId + '\'' +
				", acceptanceStatus=" + acceptanceStatus +
				", processedOn=" + processedOn +
				", currentVersion=" + currentVersion +
				", processedVersion=" + processedVersion +
				'}';
	}
}
