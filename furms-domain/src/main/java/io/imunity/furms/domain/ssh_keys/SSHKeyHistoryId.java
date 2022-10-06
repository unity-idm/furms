/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.ssh_keys;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import io.imunity.furms.domain.Id;

public class SSHKeyHistoryId implements Id {
	public final UUID id;

	public SSHKeyHistoryId(UUID id) {
		this.id = id;
	}

	public SSHKeyHistoryId(String id) {
		this.id = Optional.ofNullable(id)
			.map(UUID::fromString)
			.orElse(null);
	}

	public SSHKeyHistoryId(SSHKeyHistoryId id) {
		this.id = Optional.ofNullable(id)
			.map(resourceCreditId -> resourceCreditId.id)
			.orElse(null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SSHKeyHistoryId sshKeyHistoryId = (SSHKeyHistoryId) o;
		return Objects.equals(id, sshKeyHistoryId.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "SSHKeyHistoryId{" +
			"id=" + id +
			'}';
	}
	
	@Override
	public String asRawString() {
		return RawIdParser.asRawString(id);
	}
}
