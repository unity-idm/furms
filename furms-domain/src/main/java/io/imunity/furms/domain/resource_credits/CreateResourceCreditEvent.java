/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_credits;

import java.util.Objects;

public class CreateResourceCreditEvent implements ResourceCreditEvent {
	public final String id;

	public CreateResourceCreditEvent(String id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CreateResourceCreditEvent that = (CreateResourceCreditEvent) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "CreateResourceCreditEvent{" +
			"id='" + id + '\'' +
			'}';
	}
}
