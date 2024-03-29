/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.export;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

class Consumption {
	public final LocalDateTime consumptionUntil;
	public final BigDecimal amount;

	@JsonCreator
	Consumption(@JsonProperty("consumptionUntil") LocalDateTime consumptionUntil, @JsonProperty("amount") BigDecimal amount) {
		this.consumptionUntil = consumptionUntil;
		this.amount = amount;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Consumption that = (Consumption) o;
		return Objects.equals(consumptionUntil, that.consumptionUntil) && Objects.equals(amount, that.amount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(consumptionUntil, amount);
	}

	@Override
	public String toString() {
		return "Consumption{" +
			"consumptionUntil=" + consumptionUntil +
			", amount=" + amount +
			'}';
	}
}
