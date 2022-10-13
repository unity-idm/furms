/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.ssh_keys;

import java.util.UUID;

import io.imunity.furms.domain.UUIDBasedIdentifier;

public class InstalledSSHKeyId extends UUIDBasedIdentifier {

	public InstalledSSHKeyId(String id) {
		super(id);
	}

	public InstalledSSHKeyId(UUID id) {
		super(id);
	}

	public InstalledSSHKeyId(InstalledSSHKeyId id) {
		super(id);
	}

	@Override
	public String toString() {
		return "InstalledSSHKeyId{" + "id=" + id + '}';
	}

}
