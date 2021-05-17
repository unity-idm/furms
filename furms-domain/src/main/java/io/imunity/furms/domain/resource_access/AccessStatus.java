/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_access;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public enum AccessStatus {
	GRANT_PENDING(0),
	REVOKE_PENDING(1),
	GRANT_ACKNOWLEDGED(2),
	REVOKE_ACKNOWLEDGED(3),
	GRANT_FAILED(4),
	REVOKE_FAILED(5),
	GRANTED(6),
	REVOKED(7);

	public static Set<AccessStatus> PENDING_STATUES = Set.of(GRANT_PENDING, REVOKE_PENDING);
	public static Set<AccessStatus> ACKNOWLEDGED_STATUES = Set.of(GRANT_ACKNOWLEDGED, REVOKE_ACKNOWLEDGED);
	public static Set<AccessStatus> PENDING_AND_ACKNOWLEDGED_STATUES = Set.of(GRANT_PENDING, REVOKE_PENDING, GRANT_ACKNOWLEDGED, REVOKE_ACKNOWLEDGED);
	public static Set<AccessStatus> FAILED_STATUES = Set.of(GRANT_FAILED, REVOKE_FAILED);
	public static Set<AccessStatus> ENABLED_STATUES = Set.of(GRANT_PENDING, GRANT_ACKNOWLEDGED, GRANTED, REVOKE_FAILED);

	static {
		GRANT_PENDING.predicate = status -> List.of(GRANT_ACKNOWLEDGED, GRANT_FAILED, GRANTED).contains(status);
		REVOKE_PENDING.predicate = status -> List.of(REVOKE_ACKNOWLEDGED, REVOKE_FAILED, REVOKED).contains(status);
		GRANT_ACKNOWLEDGED.predicate = status -> List.of(GRANT_FAILED, GRANTED).contains(status);
		REVOKE_ACKNOWLEDGED.predicate = status -> List.of(REVOKE_FAILED, REVOKED).contains(status);
		GRANT_FAILED.predicate = status -> false;
		REVOKE_FAILED.predicate = status -> status.equals(REVOKE_PENDING);
		GRANTED.predicate = status -> status.equals(REVOKE_PENDING);
		REVOKED.predicate = status -> false;
	}


	AccessStatus(int persistentId) {
		this.persistentId = persistentId;
	}

	private final int persistentId;
	private Predicate<AccessStatus> predicate;

	public boolean isTransitionalTo(AccessStatus accessStatus) {
		return predicate.test(accessStatus);
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
