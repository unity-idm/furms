/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;

@JsonTypeName("ProjectResourceAllocationUpdate")
public class AgentProjectAllocationUpdate implements Body {
	public final String allocationIdentifier;
	public final String allocationChunkIdentifier;
	public final BigDecimal amount;
	public final OffsetDateTime validFrom;
	public final OffsetDateTime validTo;
	@JsonIgnore
	public final ZonedDateTime receivedTime = ZonedDateTime.now();

	@JsonCreator
	AgentProjectAllocationUpdate(String allocationIdentifier, String allocationChunkIdentifier,
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
		AgentProjectAllocationUpdate that = (AgentProjectAllocationUpdate) o;
		return Objects.equals(that.amount, amount) &&
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
			", allocationIdentifier='" + allocationIdentifier + '\'' +
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

		public AgentProjectAllocationUpdate build() {
			return new AgentProjectAllocationUpdate(allocationIdentifier, allocationChunkIdentifier, amount, validFrom, validTo);
		}
	}
}
