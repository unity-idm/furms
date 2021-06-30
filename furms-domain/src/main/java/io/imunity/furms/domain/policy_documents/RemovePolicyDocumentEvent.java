/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.policy_documents;

import java.util.Objects;

public class RemovePolicyDocumentEvent implements PolicyDocumentEvent {
	public final PolicyId id;

	public RemovePolicyDocumentEvent(PolicyId id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RemovePolicyDocumentEvent siteEvent = (RemovePolicyDocumentEvent) o;
		return Objects.equals(id, siteEvent.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "RemovePolicyDocumentEvent{" +
			"id='" + id + '\'' +
			'}';
	}
}
