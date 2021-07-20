/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.policy_documents;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class PolicyDocumentExtended {

	public final PolicyId id;
	public final String siteId;
	public final String siteName;
	public final String serviceName;
	public final Optional<LocalDateTime> utcAcceptedTime;
	public final String name;
	public final PolicyWorkflow workflow;
	public final int revision;
	public final PolicyContentType contentType;
	public final String htmlText;
	public final PolicyFile policyFile;

	PolicyDocumentExtended(PolicyId id, String siteId, String siteName, String serviceName,
	                       Optional<LocalDateTime> utcAcceptedTime, String name, PolicyWorkflow workflow, int revision,
	                       PolicyContentType contentType, String htmlText, PolicyFile policyFile) {
		if((htmlText != null && !htmlText.isBlank() && !policyFile.isEmpty()))
			throw new IllegalArgumentException("Html text or policy file have to be empty");
		this.id = id;
		this.siteId = siteId;
		this.siteName = siteName;
		this.serviceName = serviceName;
		this.utcAcceptedTime = utcAcceptedTime;
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
		PolicyDocumentExtended that = (PolicyDocumentExtended) o;
		return revision == that.revision &&
			Objects.equals(id, that.id) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(siteName, that.siteName) &&
			Objects.equals(serviceName, that.serviceName) &&
			Objects.equals(utcAcceptedTime, that.utcAcceptedTime) &&
			Objects.equals(name, that.name) &&
			workflow == that.workflow &&
			contentType == that.contentType &&
			Objects.equals(htmlText, that.htmlText) &&
			Objects.equals(policyFile, that.policyFile);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, siteId, siteName, serviceName, utcAcceptedTime, name, workflow, revision, contentType, htmlText, policyFile);
	}

	public static PolicyDocumentEntityBuilder builder() {
		return new PolicyDocumentEntityBuilder();
	}

	public static final class PolicyDocumentEntityBuilder {
		private PolicyId id;
		private String siteId;
		private String siteName;
		private String serviceName;
		private LocalDateTime utcAcceptedTime;
		private String name;
		private PolicyWorkflow workflow;
		private int revision;
		private PolicyContentType contentType;
		private String htmlText;
		private PolicyFile policyFile = PolicyFile.empty();

		private PolicyDocumentEntityBuilder() {
		}

		public PolicyDocumentEntityBuilder id(PolicyId id) {
			this.id = id;
			return this;
		}

		public PolicyDocumentEntityBuilder siteId(String siteId) {
			this.siteId = siteId;
			return this;
		}

		public PolicyDocumentEntityBuilder siteName(String siteName) {
			this.siteName = siteName;
			return this;
		}

		public PolicyDocumentEntityBuilder serviceName(String serviceName) {
			this.serviceName = serviceName;
			return this;
		}

		public PolicyDocumentEntityBuilder acceptedTime(LocalDateTime time) {
			this.utcAcceptedTime = time;
			return this;
		}

		public PolicyDocumentEntityBuilder name(String name) {
			this.name = name;
			return this;
		}

		public PolicyDocumentEntityBuilder workflow(PolicyWorkflow workflow) {
			this.workflow = workflow;
			return this;
		}

		public PolicyDocumentEntityBuilder revision(int revision) {
			this.revision = revision;
			return this;
		}

		public PolicyDocumentEntityBuilder contentType(PolicyContentType contentType) {
			this.contentType = contentType;
			return this;
		}

		public PolicyDocumentEntityBuilder wysiwygText(String wysiwygText) {
			this.htmlText = wysiwygText;
			return this;
		}

		public PolicyDocumentEntityBuilder file(PolicyFile policyFile) {
			this.policyFile = policyFile;
			return this;
		}

		public PolicyDocumentEntityBuilder file(byte[] file, String type) {
			if(file == null && type == null)
				this.policyFile = PolicyFile.empty();
			else
				this.policyFile = new PolicyFile(file, type);
			return this;
		}

		public PolicyDocumentExtended build() {
			return new PolicyDocumentExtended(id, siteId, siteName, serviceName, Optional.ofNullable(utcAcceptedTime),
				name, workflow, revision, contentType, htmlText, policyFile);
		}
	}
}
