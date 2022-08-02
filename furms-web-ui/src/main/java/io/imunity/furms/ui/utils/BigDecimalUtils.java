/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.utils;

import io.imunity.furms.domain.resource_types.AmountWithUnit;

import java.math.BigDecimal;

import static java.util.Optional.ofNullable;

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

	public static boolean isBigDecimalGreaterThen0(String value) {
		try {
			BigDecimal bigDecimal = new BigDecimal(value);
			return bigDecimal.compareTo(BigDecimal.ZERO) > 0;
		}catch (NumberFormatException e){
			return false;
		}
	}
}
