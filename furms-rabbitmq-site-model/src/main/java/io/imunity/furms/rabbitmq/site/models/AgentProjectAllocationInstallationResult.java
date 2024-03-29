/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;

@JsonTypeName("ProjectResourceAllocationResult")
public class AgentProjectAllocationInstallationResult implements Body {
	@NotEmpty
	public final String allocationIdentifier;
	@NotEmpty
	public final String allocationChunkIdentifier;
	@NotNull
	public final BigDecimal amount;
	@NotNull
	public final OffsetDateTime validFrom;
	@NotNull
	public final OffsetDateTime validTo;
	@JsonIgnore
	public final ZonedDateTime receivedTime = ZonedDateTime.now();

	@JsonCreator
	AgentProjectAllocationInstallationResult(String allocationIdentifier, String allocationChunkIdentifier,
	                                         BigDecimal amount, OffsetDateTime validFrom, OffsetDateTime validTo) {
		this.allocationIdentifier = allocationIdentifier;
		this.allocationChunkIdentifier = allocationChunkIdentifier;
		this.amount = amount;
		this.validFrom = validFrom;
		this.validTo = validTo;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AgentProjectAllocationInstallationResult that = (AgentProjectAllocationInstallationResult) o;
		return Objects.equals(that.amount, amount)  &&
			Objects.equals(allocationIdentifier, that.allocationIdentifier) &&
			Objects.equals(allocationChunkIdentifier, that.allocationChunkIdentifier) &&
			Objects.equals(validFrom, that.validFrom) &&
			Objects.equals(validTo, that.validTo);
	}

	@Override
	public int hashCode() {
		return Objects.hash(allocationIdentifier, allocationChunkIdentifier, amount, validFrom, validTo);
	}

	@Override
	public String toString() {
		return "AgentProjectResourceAllocationResult{" +
			"allocationIdentifier='" + allocationIdentifier + '\'' +
			", allocationChunkIdentifier='" + allocationChunkIdentifier + '\'' +
			", amount=" + amount +
			", validFrom=" + validFrom +
			", validTo=" + validTo +
			'}';
	}

	public static AgentProjectResourceAllocationResultBuilder builder() {
		return new AgentProjectResourceAllocationResultBuilder();
	}

	public static final class AgentProjectResourceAllocationResultBuilder {
		public String allocationIdentifier;
		public String allocationChunkIdentifier;
		public String resourceType;
		public BigDecimal amount;
		public OffsetDateTime validFrom;
		public OffsetDateTime validTo;

		private AgentProjectResourceAllocationResultBuilder() {
		}

		public AgentProjectResourceAllocationResultBuilder allocationIdentifier(String allocationIdentifier) {
			this.allocationIdentifier = allocationIdentifier;
			return this;
		}

		public AgentProjectResourceAllocationResultBuilder allocationChunkIdentifier(String allocationChunkIdentifier) {
			this.allocationChunkIdentifier = allocationChunkIdentifier;
			return this;
		}

		public AgentProjectResourceAllocationResultBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public AgentProjectResourceAllocationResultBuilder validFrom(OffsetDateTime validFrom) {
			this.validFrom = validFrom;
			return this;
		}

		public AgentProjectResourceAllocationResultBuilder validTo(OffsetDateTime validTo) {
			this.validTo = validTo;
			return this;
		}

		public AgentProjectAllocationInstallationResult build() {
			return new AgentProjectAllocationInstallationResult(allocationIdentifier, allocationChunkIdentifier, amount, validFrom, validTo);
		}
	}
}
