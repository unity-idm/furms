/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.policy_documents;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

class PolicyDocumentExtendedEntity extends UUIDIdentifiable {

	public final UUID siteId;
	public final String name;
	public final String siteName;
	public final String serviceName;
	public final int workflow;
	public final int revision;
	public final int contentType;
	public final String htmlText;
	public final byte[] file;
	public final String fileType;

	PolicyDocumentExtendedEntity(UUID id, UUID siteId, String siteName, String serviceName, String name, int workflow, int revision, int contentType, String htmlText, byte[] file, String fileType) {
		this.id = id;
		this.siteId = siteId;
		this.siteName = siteName;
		this.serviceName = serviceName;
		this.name = name;
		this.workflow = workflow;
		this.revision = revision;
		this.contentType = contentType;
		this.htmlText = htmlText;
		this.file = file;
		this.fileType = fileType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PolicyDocumentExtendedEntity that = (PolicyDocumentExtendedEntity) o;
		return workflow == that.workflow &&
			revision == that.revision &&
			contentType == that.contentType &&
			Objects.equals(id, that.id) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(siteName, that.siteName) &&
			Objects.equals(serviceName, that.serviceName) &&
			Objects.equals(name, that.name) &&
			Objects.equals(htmlText, that.htmlText) &&
			Arrays.equals(file, that.file) &&
			Objects.equals(fileType, that.fileType);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(id, siteId, siteName, serviceName, name, workflow, revision, contentType, htmlText, fileType);
		result = 31 * result + Arrays.hashCode(file);
		return result;
	}

	@Override
	public String toString() {
		return "PolicyDocumentEntity{" +
			"id=" + id +
			", siteId=" + siteId +
			", siteName=" + siteName +
			", serviceName=" + serviceName +
			", name='" + name + '\'' +
			", workflow=" + workflow +
			", revision=" + revision +
			", contentType=" + contentType +
			", htmlText='" + htmlText + '\'' +
			", fileType='" + fileType + '\'' +
			'}';
	}
}
