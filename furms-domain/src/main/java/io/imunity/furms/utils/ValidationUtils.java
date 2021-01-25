/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.utils;

import java.util.function.Supplier;

public class ValidationUtils {

	public static void check(boolean condition, Supplier<? extends RuntimeException> supplier) {
		if (!condition) {
			throw supplier.get();
		}
	}
}
