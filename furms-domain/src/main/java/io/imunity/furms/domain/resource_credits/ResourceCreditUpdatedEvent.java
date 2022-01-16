/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_credits;

import java.util.Objects;

public class ResourceCreditUpdatedEvent implements ResourceCreditEvent {
	public final ResourceCredit oldResourceCredit;
	public final ResourceCredit newResourceCredit;

	public ResourceCreditUpdatedEvent(ResourceCredit oldResourceCredit, ResourceCredit newResourceCredit) {
		this.oldResourceCredit = oldResourceCredit;
		this.newResourceCredit = newResourceCredit;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceCreditUpdatedEvent that = (ResourceCreditUpdatedEvent) o;
		return Objects.equals(oldResourceCredit, that.oldResourceCredit) &&
			Objects.equals(newResourceCredit, that.newResourceCredit);
	}

	@Override
	public int hashCode() {
		return Objects.hash(oldResourceCredit, newResourceCredit);
	}

	@Override
	public String toString() {
		return "UpdateResourceCreditEvent{" +
			"oldResourceCredit='" + oldResourceCredit + '\'' +
			"newResourceCredit='" + newResourceCredit + '\'' +
			'}';
	}
}
