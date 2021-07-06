/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.policy_documents;

import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.PolicyWorkflow;

import java.util.Objects;

class PolicyDocumentGridModel {
	public final PolicyId id;
	public final String siteId;
	public final String name;
	public final PolicyWorkflow workflow;

	PolicyDocumentGridModel(PolicyId id, String siteId, String name, PolicyWorkflow workflow) {
		this.id = id;
		this.siteId = siteId;
		this.name = name;
		this.workflow = workflow;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PolicyDocumentGridModel that = (PolicyDocumentGridModel) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "PolicyDocumentFormModel{" +
			"id=" + id +
			", name='" + name + '\'' +
			", workflow=" + workflow +
			'}';
	}
}
