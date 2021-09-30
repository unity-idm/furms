/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.generic_groups;

import io.imunity.furms.domain.FurmsEvent;

import java.util.Objects;

public class GenericGroupRemoveEvent implements FurmsEvent {
	public final GenericGroupId id;

	public GenericGroupRemoveEvent(GenericGroupId id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GenericGroupRemoveEvent userEvent = (GenericGroupRemoveEvent) o;
		return Objects.equals(id, userEvent.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "GenericGroupRemoveEvent{" +
			"id='" + id + '\'' +
			'}';
	}
}
