/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.generic_groups;

import io.imunity.furms.domain.FurmsEvent;

import java.util.Objects;

public class GenericGroupCreatedEvent implements FurmsEvent {
	public final GenericGroup group;

	public GenericGroupCreatedEvent(GenericGroup group) {
		this.group = group;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GenericGroupCreatedEvent userEvent = (GenericGroupCreatedEvent) o;
		return Objects.equals(group, userEvent.group);
	}

	@Override
	public int hashCode() {
		return Objects.hash(group);
	}

	@Override
	public String toString() {
		return "GenericGroupCreatedEvent{" +
			"group='" + group + '\'' +
			'}';
	}
}
