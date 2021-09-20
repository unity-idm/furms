/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.admin;

import io.imunity.furms.rest.error.exceptions.RestNotFoundException;
import org.springframework.security.access.AccessDeniedException;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

class ResourceChecker {
	private final Function<String, Boolean> availabilityTester;

	public ResourceChecker(Function<String, Boolean> availabilityTester) {
		this.availabilityTester = availabilityTester;
	}

	<T> T performIfExists(String resourceId, Supplier<T> actionToPerform) {
		assertUUID(resourceId);
		try {
			final T result = actionToPerform.get();
			assertResultExists(result);
			return result;
		} catch (Exception e) {
			handleException(e, resourceId);
			throw e;
		}
	}

	<T> T performIfExistsAndMatching(String resourceId,
	                                 Supplier<T> actionToPerform,
	                                 Function<T, Boolean> matchingFilter) {
		assertUUID(resourceId);
		try {
			final T result = actionToPerform.get();
			assertResultExists(result);
			assertMatching(matchingFilter, result);
			return result;
		} catch (Exception e) {
			handleException(e, resourceId);
			throw e;
		}
	}

	private <T> void assertMatching(Function<T, Boolean> matchingFilter, T result) {
		if (!matchingFilter.apply(result)) {
			throw new AccessDeniedException("Access Denied to resource");
		}
	}

	private void handleException(Exception e, String resourceId) {
		if (isNotAvailable(resourceId)) {
			throw new RestNotFoundException("Resource does not exist");
		}
	}

	private <T> void assertResultExists(T result) {
		if (result instanceof Optional && ((Optional<?>) result).isEmpty()) {
			throw new RestNotFoundException("Resource does not exist");
		}
		if (result instanceof Collection && ((Collection<?>) result).isEmpty()) {
			throw new RestNotFoundException("Resource does not exist");
		}
		if (result instanceof Map && ((Map<?, ?>) result).isEmpty()) {
			throw new RestNotFoundException("Resource does not exist");
		}
	}

	private void assertUUID(String resourceId) {
		try {
			UUID.fromString(resourceId);
		} catch (Exception e) {
			throw new RestNotFoundException("Resource does not exist");
		}
	}

	private boolean isNotRestNotFoundException(Exception e) {
		return !(e instanceof RestNotFoundException);
	}

	private boolean isNotAvailable(String resourceId) {
		return !availabilityTester.apply(resourceId);
	}

}
