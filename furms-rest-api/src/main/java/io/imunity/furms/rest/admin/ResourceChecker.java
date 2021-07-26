/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.admin;

import io.imunity.furms.rest.error.exceptions.RestNotFoundException;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

class ResourceChecker {
	private final Function<String, Boolean> availabilityTester;

	public ResourceChecker(Function<String, Boolean> availabilityTester) {
		this.availabilityTester = availabilityTester;
	}

	<T> T performIfExists(String resourceId, Supplier<T> actionToPerform) {
		try {
			final T result = actionToPerform.get();
			if (result instanceof Optional && ((Optional<?>) result).isEmpty()) {
				throw new RestNotFoundException("Resource does not exist");
			}
			return result;
		} catch (Exception e) {
			if (isNotRestNotFoundException(e) && isNotAvailable(resourceId)) {
				throw new RestNotFoundException("Resource does not exist");
			}
			throw e;
		}
	}

	private boolean isNotRestNotFoundException(Exception e) {
		return !(e instanceof RestNotFoundException);
	}

	private boolean isNotAvailable(String resourceId) {
		return !availabilityTester.apply(resourceId);
	}

}
