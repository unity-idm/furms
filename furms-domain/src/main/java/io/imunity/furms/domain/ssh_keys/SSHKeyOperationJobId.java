/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.ssh_keys;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class SSHKeyOperationJobId {
	public final UUID id;

	public SSHKeyOperationJobId(UUID id) {
		this.id = id;
	}

	public SSHKeyOperationJobId(String id) {
		this.id = Optional.ofNullable(id)
			.map(UUID::fromString)
			.orElse(null);
	}

	public SSHKeyOperationJobId(SSHKeyOperationJobId id) {
		this.id = Optional.ofNullable(id)
			.map(resourceCreditId -> resourceCreditId.id)
			.orElse(null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SSHKeyOperationJobId sshKeyOperationJobId = (SSHKeyOperationJobId) o;
		return Objects.equals(id, sshKeyOperationJobId.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "SSHKeyOperationJobId{" +
			"id=" + id +
			'}';
	}
}
