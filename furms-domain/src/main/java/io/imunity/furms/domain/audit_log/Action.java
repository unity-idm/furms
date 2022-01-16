/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.audit_log;

import java.util.Arrays;

public enum Action {
	LOGIN(0), LOGOUT(1), CREATE(2), UPDATE(3), DELETE(4), GRANT(5), REVOKE(6), ACCEPT(7);

	private final int persistentId;

	Action(int persistentId) {
		this.persistentId = persistentId;
	}

	public int getPersistentId() {
		return persistentId;
	}

	public static Action valueOf(int status){
		return Arrays.stream(values())
			.filter(policyContentType -> policyContentType.getPersistentId() == status)
			.findAny()
			.orElseThrow(() -> new IllegalArgumentException(String.format("Bad status code - %s, it shouldn't happen", status)));
	}

}
