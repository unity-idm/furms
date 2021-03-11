/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.utils;

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

	public static String toString(BigDecimal amount) {
		return ofNullable(amount)
			.map(BigDecimal::toPlainString)
			.orElse(null);
	}
}
