/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.policy_documents;

import java.util.Objects;

public class PolicyDocumentCreatedEvent implements PolicyDocumentEvent {
	public final PolicyDocument policyDocument;

	public PolicyDocumentCreatedEvent(PolicyDocument policyDocument) {
		this.policyDocument = policyDocument;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PolicyDocumentCreatedEvent siteEvent = (PolicyDocumentCreatedEvent) o;
		return Objects.equals(policyDocument, siteEvent.policyDocument);
	}

	@Override
	public int hashCode() {
		return Objects.hash(policyDocument);
	}

	@Override
	public String toString() {
		return "PolicyDocumentCreateEvent{" +
			"policyDocument='" + policyDocument + '\'' +
			'}';
	}
}
