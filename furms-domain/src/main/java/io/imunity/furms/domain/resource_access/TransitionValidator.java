/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_access;

import java.util.Map;
import java.util.Set;

import static io.imunity.furms.domain.resource_access.AccessStatus.*;

class TransitionValidator {
	private final Map<AccessStatus, Set<AccessStatus>> accessStatusAllowedTransitions = Map.of(
		GRANT_PENDING, Set.of(GRANT_ACKNOWLEDGED, GRANT_FAILED, GRANTED),
		REVOKE_PENDING, Set.of(REVOKE_ACKNOWLEDGED, REVOKE_FAILED, REVOKED),
		GRANT_ACKNOWLEDGED, Set.of(GRANT_FAILED, GRANTED),
		REVOKE_ACKNOWLEDGED, Set.of(REVOKE_FAILED, REVOKED),
		GRANT_FAILED, Set.of(),
		REVOKE_FAILED, Set.of(REVOKE_PENDING),
		GRANTED, Set.of(REVOKE_PENDING),
		REVOKED, Set.of()
	);

	boolean isTransitional(AccessStatus currentState, AccessStatus nextStatus) {
		return accessStatusAllowedTransitions.get(currentState).contains(nextStatus);
	}
}
