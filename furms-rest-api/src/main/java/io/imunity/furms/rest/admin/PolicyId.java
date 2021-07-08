/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.util.Objects;

class PolicyId {
	public final String siteId;
	public final String policyId;

	PolicyId(String siteId, String policyId) {
		this.siteId = siteId;
		this.policyId = policyId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PolicyId policyId1 = (PolicyId) o;
		return Objects.equals(siteId, policyId1.siteId) && Objects.equals(policyId, policyId1.policyId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteId, policyId);
	}

	@Override
	public String toString() {
		return "PolicyId{" +
				"siteId='" + siteId + '\'' +
				", policyId='" + policyId + '\'' +
				'}';
	}
}
