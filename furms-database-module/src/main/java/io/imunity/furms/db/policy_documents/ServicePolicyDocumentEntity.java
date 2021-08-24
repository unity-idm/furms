/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.policy_documents;

import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.AssignedPolicyDocument;

import java.util.Objects;
import java.util.UUID;

public class ServicePolicyDocumentEntity {

	public final UUID id;
	public final String serviceId;
	public final String name;
	public final int revision;

	ServicePolicyDocumentEntity(UUID id, String serviceId, String name, int revision) {
		this.id = id;
		this.serviceId = serviceId;
		this.name = name;
		this.revision = revision;
	}

	AssignedPolicyDocument toServicePolicyDocument(){
		return AssignedPolicyDocument.builder()
			.id(new PolicyId(id))
			.serviceId(serviceId)
			.revision(revision)
			.name(name)
			.build();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ServicePolicyDocumentEntity that = (ServicePolicyDocumentEntity) o;
		return revision == that.revision &&
			Objects.equals(id, that.id) &&
			Objects.equals(serviceId, that.serviceId) &&
			Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, serviceId, revision);
	}

	@Override
	public String toString() {
		return "ServicePolicyDocumentEntity{" +
			"id='" + id + '\'' +
			", serviceId='" + serviceId + '\'' +
			", name='" + name + '\'' +
			", revision=" + revision +
			'}';
	}
}
