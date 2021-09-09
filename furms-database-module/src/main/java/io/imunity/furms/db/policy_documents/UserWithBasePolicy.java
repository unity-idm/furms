/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.policy_documents;

import java.util.Objects;

class UserWithBasePolicy {
	public final String userId;
	public final String policyId;
	public final int revision;

	UserWithBasePolicy(String userId, String policyId, int revision) {
		this.userId = userId;
		this.policyId = policyId;
		this.revision = revision;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserWithBasePolicy that = (UserWithBasePolicy) o;
		return revision == that.revision && Objects.equals(userId, that.userId) && Objects.equals(policyId, that.policyId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId, policyId, revision);
	}

	@Override
	public String toString() {
		return "UserIdAndPolicy{" +
			"userId='" + userId + '\'' +
			", policyId='" + policyId + '\'' +
			", revision=" + revision +
			'}';
	}
}
