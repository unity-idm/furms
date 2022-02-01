/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.audit_log;

import java.util.Arrays;

public enum Operation {
	AUTHENTICATION(0), RESOURCE_CREDIT(1), COMMUNITY_ALLOCATION(2), PROJECT_ALLOCATION(3), SITES_MANAGEMENT(4), COMMUNITIES_MANAGEMENT(5),
	RESOURCE_TYPES_MANAGEMENT(6), POLICY_DOCUMENTS_MANAGEMENT(7), SERVICES_MANAGEMENT(8), PROJECTS_MANAGEMENT(9), GENERIC_GROUPS_MANAGEMENT(10),
	SSH_KEYS_MANAGEMENT(11), ROLE_ASSIGNMENT(12), PROJECT_RESOURCE_ASSIGNMENT(13), GENERIC_GROUPS_ASSIGNMENT(14), POLICY_ACCEPTANCE(15),
	ALARM_MANAGEMENT(16);

	private final int persistentId;

	Operation(int persistentId) {
		this.persistentId = persistentId;
	}

	public int getPersistentId() {
		return persistentId;
	}

	public static Operation valueOf(int status){
		return Arrays.stream(values())
			.filter(policyContentType -> policyContentType.getPersistentId() == status)
			.findAny()
			.orElseThrow(() -> new IllegalArgumentException(String.format("Bad status code - %s, it shouldn't happen", status)));
	}
}
