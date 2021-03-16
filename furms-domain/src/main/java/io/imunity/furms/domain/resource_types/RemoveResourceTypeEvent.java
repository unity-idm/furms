/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_types;

import java.util.Objects;

public class RemoveResourceTypeEvent implements ResourceTypeEvent {
	public final String id;

	public RemoveResourceTypeEvent(String id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RemoveResourceTypeEvent that = (RemoveResourceTypeEvent) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "RemoveResourceTypeEvent{" +
			"id='" + id + '\'' +
			'}';
	}
}
