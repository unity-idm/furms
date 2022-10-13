/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.alarms;

import java.util.UUID;

import io.imunity.furms.domain.UUIDBasedIdentifier;

public class AlarmId extends UUIDBasedIdentifier {

	public AlarmId(String id) {
		super(id);
	}

	public AlarmId(UUID id) {
		super(id);
	}

	public AlarmId(AlarmId id) {
		super(id);
	}

	@Override
	public String toString() {
		return "AlarmId{" + "id=" + id + '}';
	}
}
