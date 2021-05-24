/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.utils;

import java.util.function.Supplier;

public class ValidationUtils {

	public static void assertTrue(boolean condition, Supplier<? extends RuntimeException> errorSupplier) {
		assertFalse(!condition, errorSupplier);
	}

	public static void assertFalse(boolean condition, Supplier<? extends RuntimeException> errorSupplier) {
		if (condition)
			throw errorSupplier.get();
	}
}
