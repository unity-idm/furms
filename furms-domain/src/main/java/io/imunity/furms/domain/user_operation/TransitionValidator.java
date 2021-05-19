/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.user_operation;

import java.util.Map;
import java.util.Set;

import static io.imunity.furms.domain.user_operation.UserStatus.*;

class TransitionValidator {
	private final Map<UserStatus, Set<UserStatus>> userStatusAllowedTransitions = Map.of(
		ADDING_PENDING, Set.of(ADDING_ACKNOWLEDGED, ADDING_FAILED, ADDED),
		REMOVAL_PENDING, Set.of(REMOVAL_ACKNOWLEDGED, REMOVAL_FAILED, REMOVED),
		ADDING_ACKNOWLEDGED, Set.of(ADDING_FAILED, ADDED),
		REMOVAL_ACKNOWLEDGED, Set.of(REMOVAL_FAILED, REMOVED),
		ADDING_FAILED, Set.of(),
		REMOVAL_FAILED, Set.of(REMOVAL_PENDING),
		ADDED, Set.of(REMOVAL_PENDING),
		REMOVED, Set.of()
	);

	boolean isTransitional(UserStatus currentState, UserStatus nextStatus) {
		return userStatusAllowedTransitions.get(currentState).contains(nextStatus);
	}
}
