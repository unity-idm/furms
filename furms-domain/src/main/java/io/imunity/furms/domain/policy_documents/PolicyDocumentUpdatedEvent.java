/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.policy_documents;

import java.util.Objects;

public class PolicyDocumentUpdatedEvent implements PolicyDocumentEvent {
	public final PolicyDocument oldPolicyDocument;
	public final PolicyDocument newPolicyDocument;

	public PolicyDocumentUpdatedEvent(PolicyDocument oldPolicyDocument, PolicyDocument newPolicyDocument) {
		this.oldPolicyDocument = oldPolicyDocument;
		this.newPolicyDocument = newPolicyDocument;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PolicyDocumentUpdatedEvent siteEvent = (PolicyDocumentUpdatedEvent) o;
		return Objects.equals(oldPolicyDocument, siteEvent.oldPolicyDocument) &&
			Objects.equals(newPolicyDocument, siteEvent.newPolicyDocument);
	}

	@Override
	public int hashCode() {
		return Objects.hash(oldPolicyDocument, newPolicyDocument);
	}

	@Override
	public String toString() {
		return "PolicyDocumentUpdatedEvent{" +
			"oldPolicyDocument='" + oldPolicyDocument + '\'' +
			",newPolicyDocument='" + newPolicyDocument + '\'' +
			'}';
	}
}
