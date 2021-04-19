/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.imunity.furms.rabbitmq.site.models.converter.FurmsMessage;

import java.time.LocalDateTime;
import java.util.Objects;

import static io.imunity.furms.rabbitmq.site.models.AgentProjectResourceAllocationRequest.*;

@JsonDeserialize(builder = AgentProjectResourceAllocationRequestBuilder.class)
@FurmsMessage(type = "ProjectResourceAllocationRequest")
public class AgentProjectResourceAllocationRequest {
	public final String projectIdentifier;
	public final String allocationIdentifier;
	public final String resourceCreditIdentifier;
	public final String resourceType;
	public final double amount;
	public final LocalDateTime validFrom;
	public final LocalDateTime validTo;

	AgentProjectResourceAllocationRequest(String projectIdentifier, String allocationIdentifier, String resourceCreditIdentifier, String resourceType, double amount, LocalDateTime validFrom, LocalDateTime validTo) {
		this.projectIdentifier = projectIdentifier;
		this.allocationIdentifier = allocationIdentifier;
		this.resourceCreditIdentifier = resourceCreditIdentifier;
		this.resourceType = resourceType;
		this.amount = amount;
		this.validFrom = validFrom;
		this.validTo = validTo;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AgentProjectResourceAllocationRequest that = (AgentProjectResourceAllocationRequest) o;
		return Double.compare(that.amount, amount) == 0 &&
			Objects.equals(projectIdentifier, that.projectIdentifier) &&
			Objects.equals(allocationIdentifier, that.allocationIdentifier) &&
			Objects.equals(resourceCreditIdentifier, that.resourceCreditIdentifier) &&
			Objects.equals(resourceType, that.resourceType) &&
			Objects.equals(validFrom, that.validFrom) &&
			Objects.equals(validTo, that.validTo);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectIdentifier, allocationIdentifier, resourceCreditIdentifier, resourceType, amount, validFrom, validTo);
	}

	@Override
	public String toString() {
		return "AgentProjectResourceAllocationRequest{" +
			"projectIdentifier='" + projectIdentifier + '\'' +
			", allocationIdentifier='" + allocationIdentifier + '\'' +
			", resourceCreditIdentifier='" + resourceCreditIdentifier + '\'' +
			", resourceType='" + resourceType + '\'' +
			", amount=" + amount +
			", validFrom=" + validFrom +
			", validTo=" + validTo +
			'}';
	}

	public static AgentProjectResourceAllocationRequestBuilder builder() {
		return new AgentProjectResourceAllocationRequestBuilder();
	}

	@JsonPOJOBuilder(withPrefix = "")
	public static final class AgentProjectResourceAllocationRequestBuilder {
		public String projectIdentifier;
		public String allocationIdentifier;
		public String resourceCreditIdentifier;
		public String resourceType;
		public double amount;
		public LocalDateTime validFrom;
		public LocalDateTime validTo;

		private AgentProjectResourceAllocationRequestBuilder() {
		}

		public AgentProjectResourceAllocationRequestBuilder projectIdentifier(String projectIdentifier) {
			this.projectIdentifier = projectIdentifier;
			return this;
		}

		public AgentProjectResourceAllocationRequestBuilder allocationIdentifier(String allocationIdentifier) {
			this.allocationIdentifier = allocationIdentifier;
			return this;
		}

		public AgentProjectResourceAllocationRequestBuilder resourceType(String resourceType) {
			this.resourceType = resourceType;
			return this;
		}

		public AgentProjectResourceAllocationRequestBuilder resourceCreditIdentifier(String resourceCreditIdentifier) {
			this.resourceCreditIdentifier = resourceCreditIdentifier;
			return this;
		}

		public AgentProjectResourceAllocationRequestBuilder amount(double amount) {
			this.amount = amount;
			return this;
		}

		public AgentProjectResourceAllocationRequestBuilder validFrom(LocalDateTime validFrom) {
			this.validFrom = validFrom;
			return this;
		}

		public AgentProjectResourceAllocationRequestBuilder validTo(LocalDateTime validTo) {
			this.validTo = validTo;
			return this;
		}

		public AgentProjectResourceAllocationRequest build() {
			return new AgentProjectResourceAllocationRequest(projectIdentifier, allocationIdentifier, resourceCreditIdentifier, resourceType, amount, validFrom, validTo);
		}
	}
}
