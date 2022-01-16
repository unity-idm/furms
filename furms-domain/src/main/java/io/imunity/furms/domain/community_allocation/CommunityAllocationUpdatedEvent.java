/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.community_allocation;

import java.util.Objects;

public class CommunityAllocationUpdatedEvent implements CommunityAllocationEvent {
	public final CommunityAllocation oldCommunityAllocation;
	public final CommunityAllocation newCommunityAllocation;

	public CommunityAllocationUpdatedEvent(CommunityAllocation oldCommunityAllocation, CommunityAllocation newCommunityAllocation) {
		this.oldCommunityAllocation = oldCommunityAllocation;
		this.newCommunityAllocation = newCommunityAllocation;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommunityAllocationUpdatedEvent that = (CommunityAllocationUpdatedEvent) o;
		return Objects.equals(oldCommunityAllocation, that.oldCommunityAllocation) &&
			Objects.equals(newCommunityAllocation, that.newCommunityAllocation);
	}

	@Override
	public int hashCode() {
		return Objects.hash(oldCommunityAllocation, newCommunityAllocation);
	}

	@Override
	public String toString() {
		return "CommunityAllocationUpdatedEvent{" +
			"oldCommunityAllocation='" + oldCommunityAllocation + '\'' +
			",newCommunityAllocation='" + newCommunityAllocation + '\'' +
			'}';
	}
}
