/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_credits;

import java.util.Objects;

public class ResourceCreditRemovedEvent implements ResourceCreditEvent {
	public final ResourceCredit resourceCredit;

	public ResourceCreditRemovedEvent(ResourceCredit resourceCredit) {
		this.resourceCredit = resourceCredit;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceCreditRemovedEvent that = (ResourceCreditRemovedEvent) o;
		return Objects.equals(resourceCredit, that.resourceCredit);
	}

	@Override
	public int hashCode() {
		return Objects.hash(resourceCredit);
	}

	@Override
	public String toString() {
		return "RemoveResourceCreditEvent{" +
			"resourceCredit='" + resourceCredit + '\'' +
			'}';
	}
}
