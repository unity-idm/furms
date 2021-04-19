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

import static io.imunity.furms.rabbitmq.site.models.AgentProjectResourceAllocationResult.*;

@JsonDeserialize(builder = AgentProjectResourceAllocationResultBuilder.class)
@FurmsMessage(type = "ProjectResourceAllocationRequest")
public class AgentProjectResourceAllocationResult {
	public final String projectIdentifier;
	public final String allocationIdentifier;
	public final String allocationChunkIdentifier;
	public final String resourceType;
	public final double amount;
	public final LocalDateTime validFrom;
	public final LocalDateTime validTo;

	AgentProjectResourceAllocationResult(String projectIdentifier, String allocationIdentifier, String allocationChunkIdentifier, String resourceType, double amount, LocalDateTime validFrom, LocalDateTime validTo) {
		this.projectIdentifier = projectIdentifier;
		this.allocationIdentifier = allocationIdentifier;
		this.allocationChunkIdentifier = allocationChunkIdentifier;
		this.resourceType = resourceType;
		this.amount = amount;
		this.validFrom = validFrom;
		this.validTo = validTo;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AgentProjectResourceAllocationResult that = (AgentProjectResourceAllocationResult) o;
		return Double.compare(that.amount, amount) == 0 &&
			Objects.equals(projectIdentifier, that.projectIdentifier) &&
			Objects.equals(allocationIdentifier, that.allocationIdentifier) &&
			Objects.equals(allocationChunkIdentifier, that.allocationChunkIdentifier) &&
			Objects.equals(resourceType, that.resourceType) &&
			Objects.equals(validFrom, that.validFrom) &&
			Objects.equals(validTo, that.validTo);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectIdentifier, allocationIdentifier, allocationChunkIdentifier, resourceType, amount, validFrom, validTo);
	}

	@Override
	public String toString() {
		return "AgentProjectResourceAllocationResult{" +
			"projectIdentifier='" + projectIdentifier + '\'' +
			", allocationIdentifier='" + allocationIdentifier + '\'' +
			", allocationChunkIdentifier='" + allocationChunkIdentifier + '\'' +
			", resourceType='" + resourceType + '\'' +
			", amount=" + amount +
			", validFrom=" + validFrom +
			", validTo=" + validTo +
			'}';
	}

	public static AgentProjectResourceAllocationResultBuilder builder() {
		return new AgentProjectResourceAllocationResultBuilder();
	}

	@JsonPOJOBuilder(withPrefix = "")
	public static final class AgentProjectResourceAllocationResultBuilder {
		public String projectIdentifier;
		public String allocationIdentifier;
		public String allocationChunkIdentifier;
		public String resourceType;
		public double amount;
		public LocalDateTime validFrom;
		public LocalDateTime validTo;

		private AgentProjectResourceAllocationResultBuilder() {
		}

		public AgentProjectResourceAllocationResultBuilder projectIdentifier(String projectIdentifier) {
			this.projectIdentifier = projectIdentifier;
			return this;
		}

		public AgentProjectResourceAllocationResultBuilder allocationIdentifier(String allocationIdentifier) {
			this.allocationIdentifier = allocationIdentifier;
			return this;
		}

		public AgentProjectResourceAllocationResultBuilder allocationChunkIdentifier(String allocationChunkIdentifier) {
			this.allocationChunkIdentifier = allocationChunkIdentifier;
			return this;
		}

		public AgentProjectResourceAllocationResultBuilder resourceType(String resourceType) {
			this.resourceType = resourceType;
			return this;
		}

		public AgentProjectResourceAllocationResultBuilder amount(double amount) {
			this.amount = amount;
			return this;
		}

		public AgentProjectResourceAllocationResultBuilder validFrom(LocalDateTime validFrom) {
			this.validFrom = validFrom;
			return this;
		}

		public AgentProjectResourceAllocationResultBuilder validTo(LocalDateTime validTo) {
			this.validTo = validTo;
			return this;
		}

		public AgentProjectResourceAllocationResult build() {
			return new AgentProjectResourceAllocationResult(projectIdentifier, allocationIdentifier, allocationChunkIdentifier, resourceType, amount, validFrom, validTo);
		}
	}
}
