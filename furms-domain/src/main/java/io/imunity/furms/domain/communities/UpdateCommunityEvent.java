/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.communities;

import java.util.Objects;

public class UpdateCommunityEvent implements CommunityEvent{
	public final String id;

	public UpdateCommunityEvent(String id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UpdateCommunityEvent that = (UpdateCommunityEvent) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "UpdateCommunityEvent{" +
			"id='" + id + '\'' +
			'}';
	}
}
