/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_credits;

import io.imunity.furms.domain.users.PersistentId;

import java.util.Objects;

public class ResourceCreditCreatedEvent implements ResourceCreditEvent {
	public final PersistentId originator;
	public final ResourceCredit resourceCredit;

	public ResourceCreditCreatedEvent(PersistentId originator, ResourceCredit resourceCredit) {
		this.originator = originator;
		this.resourceCredit = resourceCredit;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceCreditCreatedEvent that = (ResourceCreditCreatedEvent) o;
		return Objects.equals(resourceCredit, that.resourceCredit);
	}

	@Override
	public int hashCode() {
		return Objects.hash(resourceCredit);
	}

	@Override
	public String toString() {
		return "ResourceCreditCreatedEvent{" +
			", originator=" + originator +
			", resourceCredit=" + resourceCredit +
			'}';
	}
}
