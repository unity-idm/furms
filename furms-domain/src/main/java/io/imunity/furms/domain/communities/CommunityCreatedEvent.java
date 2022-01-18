/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.communities;

import java.util.Objects;

public class CommunityCreatedEvent implements CommunityEvent {
	public final Community community;

	public CommunityCreatedEvent(Community community) {
		this.community = community;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommunityCreatedEvent that = (CommunityCreatedEvent) o;
		return Objects.equals(community, that.community);
	}

	@Override
	public int hashCode() {
		return Objects.hash(community);
	}

	@Override
	public String toString() {
		return "CommunityCreatedEvent{" +
			"community='" + community + '\'' +
			'}';
	}
}
