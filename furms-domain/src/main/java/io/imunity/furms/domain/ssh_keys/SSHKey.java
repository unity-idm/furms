/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.domain.ssh_keys;

import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.PersistentId;
import org.apache.sshd.common.config.keys.AuthorizedKeyEntry;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.digest.BuiltinDigests;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class SSHKey {
	public final SSHKeyId id;
	public final String name;
	public final String value;
	public final LocalDateTime createTime;
	public final LocalDateTime updateTime;
	public final PersistentId ownerId;
	public final Set<SiteId> sites;

	private SSHKey(SSHKeyId id, String name, String value, PersistentId ownerId, LocalDateTime createTime,
			LocalDateTime updateTime, Set<SiteId> sites) {
		this.id = id;
		this.name = name;
		this.value = value;
		this.ownerId = ownerId;
		this.createTime = createTime;
		this.updateTime = updateTime;
		this.sites = sites != null ? Set.copyOf(sites) : null;
	}

	@Override
	public String toString() {
		return "SSHKey" + "id=" + id + ", name='" + name + ", createTime='" + createTime + '\''
				+ ", updateTime='" + updateTime + '\'' + ", ownerId='" + ownerId + '\'' + ", sites="
				+ sites + '}';
	}

	public static SSHKey.SSHKeyBuilder builder() {
		return new SSHKey.SSHKeyBuilder();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		SSHKey entity = (SSHKey) o;
		return Objects.equals(id, entity.id) && Objects.equals(name, entity.name)
				&& Objects.equals(value, entity.value)
				&& Objects.equals(sites, entity.sites)
				&& Objects.equals(createTime, entity.createTime)
				&& Objects.equals(updateTime, entity.updateTime)
				&& Objects.equals(ownerId, entity.ownerId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, value, createTime, updateTime, ownerId, sites);
	}

	public Map<String, String> getKeyOptions() {
		return getKeyOptions(value);
	}

	public void validate() {
		validate(value);
	}

	public void validateFromOption() {
		SSHKeyFromOptionValidator.validateFromOption(getKeyOptions().get("from"));
	}
	
	public static Map<String, String> getKeyOptions(String publicSSHKey) {

		validate(publicSSHKey);
		return AuthorizedKeyEntry.parseAuthorizedKeyEntry(publicSSHKey).getLoginOptions();
	}

	public String getFingerprint() {
		return getKeyFingerprint(value);
	}

	public static String getKeyFingerprint(String publicSSHKey) {

		try {
			return KeyUtils.getFingerPrint(BuiltinDigests.md5, AuthorizedKeyEntry
					.parseAuthorizedKeyEntry(publicSSHKey).resolvePublicKey(null, null));
		} catch (Exception e) {
			throw new InvalidSSHKeyValueException("Invalid SSH key value", e);
		}
	}

	public static void validate(String publicSSHKey) {
		if (publicSSHKey == null || publicSSHKey.isEmpty())
			throw new InvalidSSHKeyValueException("Invalid SSH Key value: SSH Key value is empty.");
		AuthorizedKeyEntry.parseAuthorizedKeyEntry(publicSSHKey);
		getKeyFingerprint(publicSSHKey);
	}

	public static class SSHKeyBuilder {

		private SSHKeyId id;
		private String name;
		private String value;
		private LocalDateTime createTime;
		private LocalDateTime updateTime;
		private PersistentId ownerId;
		private Set<SiteId> sites;

		public SSHKey.SSHKeyBuilder id(String id) {
			this.id = new SSHKeyId(id);
			return this;
		}

		public SSHKey.SSHKeyBuilder id(SSHKeyId id) {
			this.id = id;
			return this;
		}

		public SSHKey.SSHKeyBuilder name(String name) {
			this.name = name;
			return this;
		}

		public SSHKey.SSHKeyBuilder value(String value) {
			this.value = value;
			return this;
		}

		public SSHKey.SSHKeyBuilder createTime(LocalDateTime createTime) {
			this.createTime = createTime;
			return this;
		}

		public SSHKey.SSHKeyBuilder updateTime(LocalDateTime updateTime) {
			this.updateTime = updateTime;
			return this;
		}

		public SSHKey.SSHKeyBuilder ownerId(PersistentId ownerId) {
			this.ownerId = ownerId;
			return this;
		}

		public SSHKey.SSHKeyBuilder sites(Set<SiteId> sites) {
			this.sites = sites;
			return this;
		}

		public SSHKey build() {
			return new SSHKey(id, name, value, ownerId, createTime, updateTime, sites);
		}
	}
}
