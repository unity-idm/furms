/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.ssh_keys;

import java.util.UUID;

import io.imunity.furms.domain.UUIDBasedIdentifier;

public class SSHKeyHistoryId extends UUIDBasedIdentifier {

	public SSHKeyHistoryId(String id) {
		super(id);
	}

	public SSHKeyHistoryId(UUID id) {
		super(id);
	}

	public SSHKeyHistoryId(SSHKeyHistoryId id) {
		super(id);
	}

	@Override
	public String toString() {
		return "SSHKeyHistoryId{" + "id=" + id + '}';
	}
}
