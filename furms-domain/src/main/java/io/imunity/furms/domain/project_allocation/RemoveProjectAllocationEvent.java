/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_allocation;

import java.util.Objects;

public class RemoveProjectAllocationEvent implements ProjectAllocationEvent {
	public final String id;

	public RemoveProjectAllocationEvent(String id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RemoveProjectAllocationEvent that = (RemoveProjectAllocationEvent) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "RemoveProjectAllocationEvent{" +
			"id='" + id + '\'' +
			'}';
	}
}
