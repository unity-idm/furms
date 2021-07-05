/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.policy_documents;

import java.util.Objects;

public class PolicyDocumentUpdatedEvent implements PolicyDocumentEvent {
	public final PolicyId id;

	public PolicyDocumentUpdatedEvent(PolicyId id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PolicyDocumentUpdatedEvent siteEvent = (PolicyDocumentUpdatedEvent) o;
		return Objects.equals(id, siteEvent.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "PolicyDocumentUpdatedEvent{" +
			"id='" + id + '\'' +
			'}';
	}
}
