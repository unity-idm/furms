/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.utils;

import static java.util.Optional.ofNullable;

import java.math.BigDecimal;

import io.imunity.furms.domain.resource_types.AmountWithUnit;

public class BigDecimalUtils {
	public static BigDecimal toBigDecimal(String value) {
		return ofNullable(value)
			.filter(v -> !v.isBlank())
			.map(String::trim)
			.map(BigDecimal::new)
			.orElse(null);
	}

	public static String toString(AmountWithUnit amount) {
		return ofNullable(amount)
			.map(amountWithUnit -> amountWithUnit.amount)
			.map(BigDecimal::toPlainString)
			.orElse(null);
	}

	public static boolean isBigDecimal(String value) {
		try {
			new BigDecimal(value);
			return true;
		}catch (NumberFormatException e){
			return false;
		}
	}
}
