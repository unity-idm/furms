/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.policy_documents;

import io.imunity.furms.domain.users.FenixUserId;

import java.util.Objects;

public class UserAcceptedPolicyEvent implements PolicyDocumentEvent {
	public final FenixUserId userId;
	public final PolicyAcceptance policyAcceptance;

	public UserAcceptedPolicyEvent(FenixUserId userId, PolicyAcceptance policyAcceptance) {
		this.userId = userId;
		this.policyAcceptance = policyAcceptance;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserAcceptedPolicyEvent that = (UserAcceptedPolicyEvent) o;
		return Objects.equals(userId, that.userId) && Objects.equals(policyAcceptance, that.policyAcceptance);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId, policyAcceptance);
	}

	@Override
	public String toString() {
		return "UserAcceptedPolicyEvent{" +
			"userId=" + userId +
			", policyAcceptance=" + policyAcceptance +
			'}';
	}
}
