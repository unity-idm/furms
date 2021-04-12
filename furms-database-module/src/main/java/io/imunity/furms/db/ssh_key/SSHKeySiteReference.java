/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.db.ssh_key;

import java.util.Objects;
import java.util.UUID;

import org.springframework.data.relational.core.mapping.Table;

@Table("sshkey_site")
public class SSHKeySiteReference {
	private final UUID siteId;

	public SSHKeySiteReference(UUID siteId) {
		this.siteId = siteId;
	}

	public UUID getSiteId() {
		return siteId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		SSHKeySiteReference siteRef = (SSHKeySiteReference) o;
		return Objects.equals(siteId, siteRef.getSiteId());
	}

	@Override
	public String toString() {
		return "SSHKeySiteReference" + "siteId=" + siteId + '}';
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteId);
	}
}
