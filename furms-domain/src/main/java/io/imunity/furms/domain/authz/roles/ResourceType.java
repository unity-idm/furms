/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.authz.roles;

import java.util.Arrays;

public enum  ResourceType {
	APP_LEVEL(0), SITE(1), COMMUNITY(2), PROJECT(3);

	private final int persistentId;

	ResourceType(int persistentId) {
		this.persistentId = persistentId;
	}

	public int getPersistentId() {
		return persistentId;
	}

	public static ResourceType valueOf(int status){
		return Arrays.stream(values())
			.filter(resourceType -> resourceType.getPersistentId() == status)
			.findAny()
			.orElseThrow(() -> new IllegalArgumentException(String.format("Bad status code - %s, it shouldn't happen", status)));
	}
}