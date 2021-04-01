/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.community_allocation;

import java.util.Objects;

public class RemoveCommunityAllocationEvent implements CommunityAllocationEvent {
	public final String id;

	public RemoveCommunityAllocationEvent(String id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RemoveCommunityAllocationEvent that = (RemoveCommunityAllocationEvent) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "RemoveCommunityAllocationEvent{" +
			"id='" + id + '\'' +
			'}';
	}
}
