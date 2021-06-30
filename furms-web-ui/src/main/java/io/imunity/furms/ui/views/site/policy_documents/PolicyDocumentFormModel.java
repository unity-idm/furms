/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.policy_documents;

import io.imunity.furms.domain.policy_documents.PolicyContentType;
import io.imunity.furms.domain.policy_documents.PolicyFile;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.PolicyWorkflow;

import java.util.Objects;

class PolicyDocumentFormModel {
	public PolicyId id;
	public String siteId;
	public String name;
	public PolicyWorkflow workflow;
	public int revision;
	public PolicyContentType contentType;
	public String wysiwygText;
	public PolicyFile policyFile = PolicyFile.empty();

	PolicyDocumentFormModel(PolicyId id,
	                               String siteId,
	                               String name,
	                               PolicyWorkflow workflow,
	                               int revision,
	                               PolicyContentType contentType,
	                               String wysiwygText,
	                               PolicyFile policyFile) {
		this.id = id;
		this.siteId = siteId;
		this.name = name;
		this.workflow = workflow;
		this.revision = revision;
		this.contentType = contentType;
		this.wysiwygText = wysiwygText;
		this.policyFile = policyFile;
	}

	PolicyDocumentFormModel(String siteId){
		this.siteId = siteId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PolicyDocumentFormModel that = (PolicyDocumentFormModel) o;
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
			", siteId='" + siteId + '\'' +
			", name='" + name + '\'' +
			", workflow=" + workflow +
			", revision=" + revision +
			", contentType=" + contentType +
			", wysiwygText='" + wysiwygText + '\'' +
			", policyFile=" + policyFile +
			'}';
	}

	public static PolicyDocumentFormModelBuilder builder() {
		return new PolicyDocumentFormModelBuilder();
	}

	public static final class PolicyDocumentFormModelBuilder {
		private PolicyId id;
		private String siteId;
		private String name;
		private PolicyWorkflow workflow;
		private int revision;
		private PolicyContentType contentType;
		private String wysiwygText;
		private PolicyFile policyFile = PolicyFile.empty();

		private PolicyDocumentFormModelBuilder() {
		}

		public PolicyDocumentFormModelBuilder id(PolicyId id) {
			this.id = id;
			return this;
		}

		public PolicyDocumentFormModelBuilder siteId(String siteId) {
			this.siteId = siteId;
			return this;
		}

		public PolicyDocumentFormModelBuilder name(String name) {
			this.name = name;
			return this;
		}

		public PolicyDocumentFormModelBuilder workflow(PolicyWorkflow workflow) {
			this.workflow = workflow;
			return this;
		}

		public PolicyDocumentFormModelBuilder revision(int revision) {
			this.revision = revision;
			return this;
		}

		public PolicyDocumentFormModelBuilder contentType(PolicyContentType contentType) {
			this.contentType = contentType;
			return this;
		}

		public PolicyDocumentFormModelBuilder wysiwygText(String wysiwygText) {
			this.wysiwygText = wysiwygText;
			return this;
		}

		public PolicyDocumentFormModelBuilder policyFile(PolicyFile policyFile) {
			this.policyFile = policyFile;
			return this;
		}

		public PolicyDocumentFormModel build() {
			return new PolicyDocumentFormModel(id, siteId, name, workflow, revision, contentType, wysiwygText, policyFile);
		}
	}
}
