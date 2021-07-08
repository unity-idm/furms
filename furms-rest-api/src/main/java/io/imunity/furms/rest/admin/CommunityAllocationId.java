/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.util.Objects;

class CommunityAllocationId {
	public final String communityId;
	public final String allocationId;

	CommunityAllocationId(String communityId, String allocationId) {
		this.communityId = communityId;
		this.allocationId = allocationId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommunityAllocationId that = (CommunityAllocationId) o;
		return Objects.equals(communityId, that.communityId)
				&& Objects.equals(allocationId, that.allocationId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(communityId, allocationId);
	}

	@Override
	public String toString() {
		return "CommunityAllocationId{" +
				"communityId='" + communityId + '\'' +
				", allocationId='" + allocationId + '\'' +
				'}';
	}
}
