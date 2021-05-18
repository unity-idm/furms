/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_access;

import java.util.Arrays;
import java.util.Set;

public enum AccessStatus {
	GRANT_PENDING(0),
	REVOKE_PENDING(1),
	GRANT_ACKNOWLEDGED(2),
	REVOKE_ACKNOWLEDGED(3),
	GRANT_FAILED(4),
	REVOKE_FAILED(5),
	GRANTED(6),
	REVOKED(7);

	public static final Set<AccessStatus> PENDING_STATUES = Set.of(GRANT_PENDING, REVOKE_PENDING);
	public static final Set<AccessStatus> ACKNOWLEDGED_STATUES = Set.of(GRANT_ACKNOWLEDGED, REVOKE_ACKNOWLEDGED);
	public static final Set<AccessStatus> PENDING_AND_ACKNOWLEDGED_STATUES = Set.of(GRANT_PENDING, REVOKE_PENDING, GRANT_ACKNOWLEDGED, REVOKE_ACKNOWLEDGED);
	public static final Set<AccessStatus> FAILED_STATUES = Set.of(GRANT_FAILED, REVOKE_FAILED);
	public static final Set<AccessStatus> ENABLED_STATUES = Set.of(GRANT_PENDING, GRANT_ACKNOWLEDGED, GRANTED, REVOKE_FAILED);
	public static final Set<AccessStatus> TERMINAL_GRANTED = Set.of(GRANTED, GRANT_FAILED);

	AccessStatus(int persistentId) {
		this.persistentId = persistentId;
		this.transitionManager = new TransitionManager();
	}

	private final int persistentId;
	private final TransitionManager transitionManager;

	public boolean isTransitionalTo(AccessStatus accessStatus) {
		return transitionManager.isTransitional(this, accessStatus);
	}

	public int getPersistentId() {
		return persistentId;
	}

	public static AccessStatus valueOf(int status){
		return Arrays.stream(values())
			.filter(revokeAccessStatus -> revokeAccessStatus.getPersistentId() == status)
			.findAny()
			.orElseThrow(() -> new IllegalArgumentException("Bad status code, it shouldn't happen"));
	}
}
