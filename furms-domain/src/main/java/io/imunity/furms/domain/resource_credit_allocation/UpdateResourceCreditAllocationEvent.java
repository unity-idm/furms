/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_credit_allocation;

import java.util.Objects;

public class UpdateResourceCreditAllocationEvent implements ResourceCreditAllocationEvent {
	public final String id;

	public UpdateResourceCreditAllocationEvent(String id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UpdateResourceCreditAllocationEvent that = (UpdateResourceCreditAllocationEvent) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "UpdateResourceCreditAllocationEvent{" +
			"id='" + id + '\'' +
			'}';
	}
}
