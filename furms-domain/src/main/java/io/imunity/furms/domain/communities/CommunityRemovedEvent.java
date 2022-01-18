/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.communities;

import java.util.Objects;

public class CommunityRemovedEvent implements CommunityEvent {
	public final Community community;

	public CommunityRemovedEvent(Community community) {
		this.community = community;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommunityRemovedEvent that = (CommunityRemovedEvent) o;
		return Objects.equals(community, that.community);
	}

	@Override
	public int hashCode() {
		return Objects.hash(community);
	}

	@Override
	public String toString() {
		return "CommunityRemovedEvent{" +
			"community='" + community + '\'' +
			'}';
	}
}
