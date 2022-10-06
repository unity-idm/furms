/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.ssh_keys;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import io.imunity.furms.domain.Id;

public class SSHKeyId implements Id {
	public final UUID id;

	public SSHKeyId(UUID id) {
		this.id = id;
	}

	public SSHKeyId(String id) {
		this.id = Optional.ofNullable(id)
			.map(UUID::fromString)
			.orElse(null);
	}

	public SSHKeyId(SSHKeyId id) {
		this.id = Optional.ofNullable(id)
			.map(resourceCreditId -> resourceCreditId.id)
			.orElse(null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SSHKeyId sshKeyId = (SSHKeyId) o;
		return Objects.equals(id, sshKeyId.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "SSHKeyId{" +
			"id=" + id +
			'}';
	}
	
	@Override
	public String asRawString() {
		return RawIdParser.asRawString(id);
	}
}
