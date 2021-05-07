/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.ssh_key_history;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.relational.core.mapping.Table;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.ssh_keys.SSHKeyHistory;
import io.imunity.furms.domain.users.PersistentId;

@Table("ssh_key_history")
class SSHKeyHistoryEntity extends UUIDIdentifiable {

	public final UUID siteId;
	public final String sshkeyOwnerId;
	public final String sshkeyFingerprint;
	public final LocalDateTime originationTime;

	SSHKeyHistoryEntity(UUID id, UUID siteId, String sshkeyOwnerId, String sshkeyFingerprint, LocalDateTime originationTime) {
		this.id = id;
		this.siteId = siteId;
		this.sshkeyOwnerId = sshkeyOwnerId;
		this.sshkeyFingerprint = sshkeyFingerprint;
		this.originationTime = originationTime;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		SSHKeyHistoryEntity that = (SSHKeyHistoryEntity) o;
		return Objects.equals(id, that.id) && Objects.equals(siteId, that.siteId)
				&& Objects.equals(sshkeyOwnerId, that.sshkeyOwnerId)
				&& Objects.equals(sshkeyFingerprint, that.sshkeyFingerprint)
				&& Objects.equals(originationTime, that.originationTime);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, siteId, sshkeyOwnerId, sshkeyFingerprint, originationTime);
	}

	@Override
	public String toString() {
		return "SSHKeyHistoryEntity{" + "id=" + id + ", sshkeyFingerprint=" + sshkeyFingerprint
				+ ", siteId=" + siteId
				+ ", sshkeyOwnerId=" + sshkeyOwnerId
				+ ", originationTime=" + originationTime + '}';
	}

	SSHKeyHistory toSSHKeyHistory() {
		return SSHKeyHistory.builder().id(id.toString()).siteId(siteId.toString())
				.sshkeyFingerprint(sshkeyFingerprint).originationTime(originationTime)
				.sshkeyOwnerId(new PersistentId(sshkeyOwnerId)).build();
	}

	public static SSHKeyHistoryEntityBuilder builder() {
		return new SSHKeyHistoryEntityBuilder();
	}

	public static final class SSHKeyHistoryEntityBuilder {
		public UUID id;
		public UUID siteId;
		public String sshkeyOwnerId;
		public String sshkeyFingerprint;
		private LocalDateTime originationTime;

		private SSHKeyHistoryEntityBuilder() {
		}

		public SSHKeyHistoryEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public SSHKeyHistoryEntityBuilder siteId(UUID siteId) {
			this.siteId = siteId;
			return this;
		}
		
		public SSHKeyHistoryEntityBuilder sshkeyOwnerId(String sshkeyOwnerId) {
			this.sshkeyOwnerId = sshkeyOwnerId;
			return this;
		}


		public SSHKeyHistoryEntityBuilder sshkeyFingerprint(String sshkeyFingerprint) {
			this.sshkeyFingerprint = sshkeyFingerprint;
			return this;
		}

		public SSHKeyHistoryEntityBuilder originationTime(LocalDateTime originationTime) {
			this.originationTime = originationTime;
			return this;
		}

		public SSHKeyHistoryEntity build() {
			return new SSHKeyHistoryEntity(id, siteId, sshkeyOwnerId, sshkeyFingerprint, originationTime);
		}
	}
}
