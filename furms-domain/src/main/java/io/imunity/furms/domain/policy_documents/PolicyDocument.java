/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.policy_documents;

import io.imunity.furms.domain.sites.SiteId;

import java.util.Objects;

public class PolicyDocument {

	public final PolicyId id;
	public final SiteId siteId;
	public final String name;
	public final PolicyWorkflow workflow;
	public final int revision;
	public final PolicyContentType contentType;
	public final String htmlText;
	public final PolicyFile policyFile;

	PolicyDocument(PolicyId id, SiteId siteId, String name, PolicyWorkflow workflow, int revision, PolicyContentType contentType, String htmlText, PolicyFile policyFile) {
		if((htmlText != null && !htmlText.isBlank() && !policyFile.isEmpty()))
			throw new IllegalArgumentException("Html text or policy file have to be empty");
		this.id = id;
		this.siteId = siteId;
		this.name = name;
		this.workflow = workflow;
		this.revision = revision;
		this.contentType = contentType;
		this.htmlText = htmlText;
		this.policyFile = policyFile;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PolicyDocument that = (PolicyDocument) o;
		return revision == that.revision &&
			Objects.equals(id, that.id) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(name, that.name) &&
			workflow == that.workflow &&
			contentType == that.contentType &&
			Objects.equals(htmlText, that.htmlText) &&
			Objects.equals(policyFile, that.policyFile);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, siteId, name, workflow, revision, contentType, htmlText, policyFile);
	}

	public boolean isRevisionDefined(){
		return revision > 0;
	}

	public static PolicyDocumentBuilder builder() {
		return new PolicyDocumentBuilder();
	}

	public static final class PolicyDocumentBuilder {
		private PolicyId id;
		private SiteId siteId;
		private String name;
		private PolicyWorkflow workflow;
		private int revision = 1;
		private PolicyContentType contentType;
		private String htmlText;
		private PolicyFile policyFile = PolicyFile.empty();

		private PolicyDocumentBuilder() {
		}

		public PolicyDocumentBuilder id(PolicyId id) {
			this.id = id;
			return this;
		}

		public PolicyDocumentBuilder siteId(String siteId) {
			this.siteId = new SiteId(siteId);
			return this;
		}

		public PolicyDocumentBuilder siteId(SiteId siteId) {
			this.siteId = siteId;
			return this;
		}

		public PolicyDocumentBuilder name(String name) {
			this.name = name;
			return this;
		}

		public PolicyDocumentBuilder workflow(PolicyWorkflow workflow) {
			this.workflow = workflow;
			return this;
		}

		public PolicyDocumentBuilder revision(int revision) {
			this.revision = revision;
			return this;
		}

		public PolicyDocumentBuilder revisionUndefined() {
			return revision(-1);
		}

		public PolicyDocumentBuilder contentType(PolicyContentType contentType) {
			this.contentType = contentType;
			return this;
		}

		public PolicyDocumentBuilder wysiwygText(String wysiwygText) {
			this.htmlText = wysiwygText;
			return this;
		}

		public PolicyDocumentBuilder file(PolicyFile policyFile) {
			this.policyFile = policyFile;
			return this;
		}

		public PolicyDocumentBuilder file(byte[] file, String type, String name) {
			if(file == null && type == null)
				this.policyFile = PolicyFile.empty();
			else
				this.policyFile = new PolicyFile(file, type, name);
			return this;
		}

		public PolicyDocument build() {
			return new PolicyDocument(id, siteId, name, workflow, revision, contentType, htmlText, policyFile);
		}
	}
}
