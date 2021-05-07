/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Objects;

@JsonTypeName("ProjectResourceDeallocationRequest")
public class AgentProjectDeallocationRequest implements Body {
	public final String projectIdentifier;
	public final String allocationIdentifier;
	public final String resourceCreditIdentifier;
	public final String resourceType;

	@JsonCreator
	AgentProjectDeallocationRequest(String projectIdentifier, String allocationIdentifier, String resourceCreditIdentifier, String resourceType) {
		this.projectIdentifier = projectIdentifier;
		this.allocationIdentifier = allocationIdentifier;
		this.resourceCreditIdentifier = resourceCreditIdentifier;
		this.resourceType = resourceType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AgentProjectDeallocationRequest that = (AgentProjectDeallocationRequest) o;
		return Objects.equals(projectIdentifier, that.projectIdentifier) &&
			Objects.equals(allocationIdentifier, that.allocationIdentifier) &&
			Objects.equals(resourceCreditIdentifier, that.resourceCreditIdentifier) &&
			Objects.equals(resourceType, that.resourceType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectIdentifier, allocationIdentifier, resourceCreditIdentifier, resourceType);
	}

	@Override
	public String toString() {
		return "AgentProjectDeallocationRequest{" +
			"projectIdentifier='" + projectIdentifier + '\'' +
			", allocationIdentifier='" + allocationIdentifier + '\'' +
			", resourceCreditIdentifier='" + resourceCreditIdentifier + '\'' +
			", resourceType='" + resourceType +
			'}';
	}

	public static AgentProjectResourceDeallocationRequestBuilder builder() {
		return new AgentProjectResourceDeallocationRequestBuilder();
	}

	public static final class AgentProjectResourceDeallocationRequestBuilder {
		public String projectIdentifier;
		public String allocationIdentifier;
		public String resourceCreditIdentifier;
		public String resourceType;

		private AgentProjectResourceDeallocationRequestBuilder() {
		}

		public AgentProjectResourceDeallocationRequestBuilder projectIdentifier(String projectIdentifier) {
			this.projectIdentifier = projectIdentifier;
			return this;
		}

		public AgentProjectResourceDeallocationRequestBuilder allocationIdentifier(String allocationIdentifier) {
			this.allocationIdentifier = allocationIdentifier;
			return this;
		}

		public AgentProjectResourceDeallocationRequestBuilder resourceType(String resourceType) {
			this.resourceType = resourceType;
			return this;
		}

		public AgentProjectResourceDeallocationRequestBuilder resourceCreditIdentifier(String resourceCreditIdentifier) {
			this.resourceCreditIdentifier = resourceCreditIdentifier;
			return this;
		}

		public AgentProjectDeallocationRequest build() {
			return new AgentProjectDeallocationRequest(projectIdentifier, allocationIdentifier, resourceCreditIdentifier, resourceType);
		}
	}
}
