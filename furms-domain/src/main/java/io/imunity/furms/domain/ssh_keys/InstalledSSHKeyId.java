/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.ssh_keys;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class InstalledSSHKeyId {
	public final UUID id;

	public InstalledSSHKeyId(UUID id) {
		this.id = id;
	}

	public InstalledSSHKeyId(String id) {
		this.id = Optional.ofNullable(id)
			.map(UUID::fromString)
			.orElse(null);
	}

	public InstalledSSHKeyId(InstalledSSHKeyId id) {
		this.id = Optional.ofNullable(id)
			.map(resourceCreditId -> resourceCreditId.id)
			.orElse(null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		InstalledSSHKeyId installedSSHKeyId = (InstalledSSHKeyId) o;
		return Objects.equals(id, installedSSHKeyId.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "InstalledSSHKeyId{" +
			"id=" + id +
			'}';
	}
}
