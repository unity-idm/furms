/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.domain.ssh_keys;

import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.PersistentId;

import java.time.LocalDateTime;
import java.util.Objects;

public class SSHKeyHistory {
	public final SSHKeyHistoryId id;
	public final SiteId siteId;
	public final PersistentId  sshkeyOwnerId;
	public final String sshkeyFingerprint;
	public final LocalDateTime originationTime;

	SSHKeyHistory(SSHKeyHistoryId id, SiteId siteId, PersistentId  sshkeyOwnerId, String sshkeyFingerprint, LocalDateTime originationTime) {
		this.id = id;
		this.siteId = siteId;
		this.sshkeyOwnerId = sshkeyOwnerId;
		this.sshkeyFingerprint = sshkeyFingerprint;
		this.originationTime = originationTime;
	}

	@Override
	public String toString() {
		return "SSHKeyHistory{" + "id=" + id + ", sshkeyFingerprint=" + sshkeyFingerprint
				+ ", siteId=" + siteId
				+ ", sshkeyOwnerId=" + sshkeyOwnerId
				+ ", originationTime=" + originationTime + '}';
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id, originationTime, siteId, sshkeyFingerprint, sshkeyOwnerId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SSHKeyHistory other = (SSHKeyHistory) obj;
		return Objects.equals(id, other.id) && Objects.equals(originationTime, other.originationTime)
				&& Objects.equals(siteId, other.siteId)
				&& Objects.equals(sshkeyOwnerId, other.sshkeyOwnerId)
				&& Objects.equals(sshkeyFingerprint, other.sshkeyFingerprint);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private SSHKeyHistoryId id;
		private SiteId siteId;
		private PersistentId  sshkeyOwnerId;
		private String sshkeyFingerprint;
		private LocalDateTime originationTime;

		private Builder() {
		}

		public Builder id(String id) {
			this.id = new SSHKeyHistoryId(id);
			return this;
		}

		public Builder siteId(String siteId) {
			this.siteId = new SiteId(siteId);
			return this;
		}

		public Builder siteId(SiteId siteId) {
			this.siteId = siteId;
			return this;
		}

		public Builder sshkeyOwnerId(PersistentId sshkeyOwnerId) {
			this.sshkeyOwnerId = sshkeyOwnerId;
			return this;
		}

		public Builder sshkeyFingerprint(String sshkeyFingerprint) {
			this.sshkeyFingerprint = sshkeyFingerprint;
			return this;
		}

		public Builder originationTime(LocalDateTime originationTime) {
			this.originationTime = originationTime;
			return this;
		}

		public SSHKeyHistory build() {
			return new SSHKeyHistory(id, siteId, sshkeyOwnerId, sshkeyFingerprint, originationTime);
		}
	}

}
