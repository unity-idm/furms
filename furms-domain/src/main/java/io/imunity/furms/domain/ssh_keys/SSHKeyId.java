/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.ssh_keys;

import java.util.UUID;

import io.imunity.furms.domain.UUIDBasedIdentifier;

public class SSHKeyId extends UUIDBasedIdentifier {

	public SSHKeyId(String id) {
		super(id);
	}

	public SSHKeyId(UUID id) {
		super(id);
	}

	public SSHKeyId(SSHKeyId id) {
		super(id);
	}

	@Override
	public String toString() {
		return "SSHKeyId{" + "id=" + id + '}';
	}
}
