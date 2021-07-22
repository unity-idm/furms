/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.admin;

import io.imunity.furms.rest.error.exceptions.RestNotFoundException;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

class ResourceExistsWrapper {

	static <T> T performIfExists(BooleanSupplier availabilityTester, Supplier<T> actionToPerform) {
		try {
			final T result = actionToPerform.get();
			if (result instanceof Optional && ((Optional<?>) result).isEmpty()) {
				throw new RestNotFoundException("Resource does not exist");
			}
			return result;
		} catch (Exception e) {
			if (!(e instanceof RestNotFoundException) && !availabilityTester.getAsBoolean()) {
				throw new RestNotFoundException("Resource does not exist");
			}
			throw e;
		}
	}

}
