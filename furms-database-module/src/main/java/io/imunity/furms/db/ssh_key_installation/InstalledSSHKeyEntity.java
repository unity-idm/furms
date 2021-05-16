/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.ssh_key_installation;

import java.util.Objects;
import java.util.UUID;

import org.springframework.data.relational.core.mapping.Table;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;

@Table("installed_ssh_key")
public class InstalledSSHKeyEntity extends UUIDIdentifiable {

	public final UUID siteId;
	public final UUID sshkeyId;

	public final String value;

	InstalledSSHKeyEntity(UUID id, UUID siteId, UUID sshkeyId, String value) {

		this.id = id;
		this.siteId = siteId;
		this.sshkeyId = sshkeyId;

		this.value = value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteId, sshkeyId, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InstalledSSHKeyEntity other = (InstalledSSHKeyEntity) obj;
		return Objects.equals(siteId, other.siteId) && Objects.equals(sshkeyId, other.sshkeyId)
				&& Objects.equals(value, other.value);
	}

	@Override
	public String toString() {
		return "InstalledSSHKeyEntity{" + "id=" + id + ", siteId=" + siteId + ", sshkeyId=" + sshkeyId
				+ ", value=" + value + '}';
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private UUID id;
		private UUID siteId;
		private UUID sshkeyId;
		private String value;

		private Builder() {
		}

		public Builder id(UUID id) {
			this.id = id;
			return this;
		}

		public Builder siteId(UUID siteId) {
			this.siteId = siteId;
			return this;
		}

		public Builder sshkeyId(UUID sshkeyId) {
			this.sshkeyId = sshkeyId;
			return this;
		}

		public Builder value(String value) {
			this.value = value;
			return this;
		}

		public InstalledSSHKeyEntity build() {
			return new InstalledSSHKeyEntity(id, siteId, sshkeyId, value);
		}
	}
}
