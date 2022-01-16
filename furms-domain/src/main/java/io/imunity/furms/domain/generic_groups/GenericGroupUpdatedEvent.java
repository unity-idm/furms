/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.generic_groups;

import io.imunity.furms.domain.FurmsEvent;

import java.util.Objects;

public class GenericGroupUpdatedEvent implements FurmsEvent {
	public final GenericGroup oldGroup;
	public final GenericGroup newGroup;

	public GenericGroupUpdatedEvent(GenericGroup oldGroup, GenericGroup newGroup) {
		this.oldGroup = oldGroup;
		this.newGroup = newGroup;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GenericGroupUpdatedEvent userEvent = (GenericGroupUpdatedEvent) o;
		return Objects.equals(oldGroup, userEvent.oldGroup) &&
			Objects.equals(newGroup, userEvent.newGroup);
	}

	@Override
	public int hashCode() {
		return Objects.hash(oldGroup, newGroup);
	}

	@Override
	public String toString() {
		return "GenericGroupUpdatedEvent{" +
			"oldGenericGroup='" + oldGroup + '\'' +
			",newGenericGroup='" + newGroup + '\'' +
			'}';
	}
}
