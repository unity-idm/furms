/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.policy_documents;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.policy_documents.PolicyContentType;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.PolicyWorkflow;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Table("policy_document")
class PolicyDocumentEntity extends UUIDIdentifiable {

	public final UUID siteId;
	public final String name;
	public final int workflow;
	public final int revision;
	public final int contentType;
	public final String wysiwygText;
	public final byte[] file;
	public final String fileType;

	PolicyDocumentEntity(UUID id, UUID siteId, String name, int workflow, int revision, int contentType, String wysiwygText, byte[] file, String fileType) {
		this.id = id;
		this.siteId = siteId;
		this.name = name;
		this.workflow = workflow;
		this.revision = revision;
		this.contentType = contentType;
		this.wysiwygText = wysiwygText;
		this.file = file;
		this.fileType = fileType;
	}

	PolicyDocument toPolicyDocument(){
		return PolicyDocument.builder()
			.id(new PolicyId(id))
			.siteId(siteId)
			.name(name)
			.workflow(PolicyWorkflow.valueOf(workflow))
			.revision(revision)
			.contentType(PolicyContentType.valueOf(contentType))
			.wysiwygText(wysiwygText)
			.file(file, fileType)
			.build();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PolicyDocumentEntity that = (PolicyDocumentEntity) o;
		return workflow == that.workflow &&
			revision == that.revision &&
			contentType == that.contentType &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(name, that.name) &&
			Objects.equals(wysiwygText, that.wysiwygText) &&
			Arrays.equals(file, that.file) &&
			Objects.equals(fileType, that.fileType);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(siteId, name, workflow, revision, contentType, wysiwygText, fileType);
		result = 31 * result + Arrays.hashCode(file);
		return result;
	}

	@Override
	public String toString() {
		return "PolicyDocumentEntity{" +
			"id=" + id +
			", siteId=" + siteId +
			", name='" + name + '\'' +
			", workflow=" + workflow +
			", revision=" + revision +
			", contentType=" + contentType +
			", wysiwygText='" + wysiwygText + '\'' +
			", fileType='" + fileType + '\'' +
			'}';
	}

	public static PolicyDocumentEntityBuilder builder() {
		return new PolicyDocumentEntityBuilder();
	}

	public static final class PolicyDocumentEntityBuilder {
		public UUID siteId;
		public String name;
		public int workflow;
		public int revision;
		public int contentType;
		public String wysiwygText;
		public byte[] file;
		public String fileType;
		protected UUID id;

		private PolicyDocumentEntityBuilder() {
		}

		public PolicyDocumentEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public PolicyDocumentEntityBuilder siteId(UUID siteId) {
			this.siteId = siteId;
			return this;
		}

		public PolicyDocumentEntityBuilder name(String name) {
			this.name = name;
			return this;
		}

		public PolicyDocumentEntityBuilder workflow(int workflow) {
			this.workflow = workflow;
			return this;
		}

		public PolicyDocumentEntityBuilder revision(int revision) {
			this.revision = revision;
			return this;
		}

		public PolicyDocumentEntityBuilder contentType(int contentType) {
			this.contentType = contentType;
			return this;
		}

		public PolicyDocumentEntityBuilder wysiwygText(String wysiwygText) {
			this.wysiwygText = wysiwygText;
			return this;
		}

		public PolicyDocumentEntityBuilder file(byte[] file) {
			this.file = file;
			return this;
		}

		public PolicyDocumentEntityBuilder fileType(String fileType) {
			this.fileType = fileType;
			return this;
		}

		public PolicyDocumentEntity build() {
			return new PolicyDocumentEntity(id, siteId, name, workflow, revision, contentType, wysiwygText, file, fileType);
		}
	}
}
