/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.generic_groups;

import io.imunity.furms.domain.FurmsEvent;

import java.util.Objects;

public class GenericGroupCreateEvent implements FurmsEvent {
	public final GenericGroupId id;

	public GenericGroupCreateEvent(GenericGroupId id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GenericGroupCreateEvent userEvent = (GenericGroupCreateEvent) o;
		return Objects.equals(id, userEvent.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "GenericGroupCreateEvent{" +
			"id='" + id + '\'' +
			'}';
	}
}
