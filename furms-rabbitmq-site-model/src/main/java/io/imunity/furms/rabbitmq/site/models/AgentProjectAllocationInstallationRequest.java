/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.time.ZonedDateTime;
import java.util.Objects;

@JsonTypeName("ProjectResourceAllocationRequest")
public class AgentProjectAllocationInstallationRequest implements Body {
	public final String projectIdentifier;
	public final String allocationIdentifier;
	public final String resourceCreditIdentifier;
	public final String resourceType;
	public final double amount;
	public final ZonedDateTime validFrom;
	public final ZonedDateTime validTo;

	@JsonCreator
	AgentProjectAllocationInstallationRequest(String projectIdentifier, String allocationIdentifier, String resourceCreditIdentifier, String resourceType, double amount, ZonedDateTime validFrom, ZonedDateTime validTo) {
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
		AgentProjectAllocationInstallationRequest that = (AgentProjectAllocationInstallationRequest) o;
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

	public static final class AgentProjectResourceAllocationRequestBuilder {
		public String projectIdentifier;
		public String allocationIdentifier;
		public String resourceCreditIdentifier;
		public String resourceType;
		public double amount;
		public ZonedDateTime validFrom;
		public ZonedDateTime validTo;

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

		public AgentProjectResourceAllocationRequestBuilder validFrom(ZonedDateTime validFrom) {
			this.validFrom = validFrom;
			return this;
		}

		public AgentProjectResourceAllocationRequestBuilder validTo(ZonedDateTime validTo) {
			this.validTo = validTo;
			return this;
		}

		public AgentProjectAllocationInstallationRequest build() {
			return new AgentProjectAllocationInstallationRequest(projectIdentifier, allocationIdentifier, resourceCreditIdentifier, resourceType, amount, validFrom, validTo);
		}
	}
}
