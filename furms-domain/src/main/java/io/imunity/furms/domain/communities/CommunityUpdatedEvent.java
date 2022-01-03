/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.communities;

import java.util.Objects;

public class CommunityUpdatedEvent implements CommunityEvent {
	public final Community oldCommunity;
	public final Community newCommunity;

	public CommunityUpdatedEvent(Community oldCommunity, Community newCommunity) {
		this.oldCommunity = oldCommunity;
		this.newCommunity = newCommunity;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommunityUpdatedEvent that = (CommunityUpdatedEvent) o;
		return Objects.equals(oldCommunity, that.oldCommunity) &&
			Objects.equals(newCommunity, that.newCommunity);
	}

	@Override
	public int hashCode() {
		return Objects.hash(oldCommunity, newCommunity);
	}

	@Override
	public String toString() {
		return "CommunityUpdatedEvent{" +
			"oldCommunity='" + oldCommunity + '\'' +
			",newCommunity='" + newCommunity + '\'' +
			'}';
	}
}
