/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.community_allocation;

import java.util.Objects;

public class CommunityAllocationCreatedEvent implements CommunityAllocationEvent {
	public final CommunityAllocation communityAllocation;

	public CommunityAllocationCreatedEvent(CommunityAllocation communityAllocation) {
		this.communityAllocation = communityAllocation;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommunityAllocationCreatedEvent that = (CommunityAllocationCreatedEvent) o;
		return Objects.equals(communityAllocation, that.communityAllocation);
	}

	@Override
	public int hashCode() {
		return Objects.hash(communityAllocation);
	}

	@Override
	public String toString() {
		return "CommunityAllocationCreatedEvent{" +
			"communityAllocation='" + communityAllocation + '\'' +
			'}';
	}
}
