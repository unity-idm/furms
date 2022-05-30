/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.db.ssh_keys;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.users.PersistentId;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Table("sshkey")
class SSHKeyEntity extends UUIDIdentifiable {
	private final String name;
	private final String value;
	private final LocalDateTime createTime;
	private final LocalDateTime updateTime;
	private final String ownerId;
	@MappedCollection(idColumn = "sshkey_id")
	private final Set<SSHKeySiteReference> sites;

	@PersistenceConstructor
	SSHKeyEntity(UUID id, String name, String value, String ownerId, LocalDateTime createTime,
			LocalDateTime updateTime, Set<SSHKeySiteReference> sites) {
		this.id = id;
		this.name = name;
		this.value = value;
		this.ownerId = ownerId;
		this.createTime = createTime;
		this.updateTime = updateTime;
		this.sites = sites;
	}

	SSHKeyEntity(String name, String value, String ownerId, LocalDateTime createTime, LocalDateTime updateTime,
			Set<SSHKeySiteReference> sites) {
		this.name = name;
		this.value = value;
		this.ownerId = ownerId;
		this.sites = sites;
		this.createTime = createTime;
		this.updateTime = updateTime;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public LocalDateTime getCreateTime() {
		return createTime;
	}

	public LocalDateTime getUpdateTime() {
		return updateTime;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public Set<SSHKeySiteReference> getSites() {
		return sites;
	}

	@Override
	public String toString() {
		return "SSHKeyEntity" + "id=" + id + ", name='" + name + '\'' + ", value='" + value + '\''
				+ ", createTime='" + createTime + '\'' + ", updateTime='" + updateTime + '\''
				+ ", ownerId='" + ownerId + '\'' + ", sites=" + sites + '}';
	}

	SSHKey toSSHKey() {
		return new SSHKey.SSHKeyBuilder().id(id.toString()).name(name).value(value).createTime(createTime)
				.updateTime(updateTime).ownerId(new PersistentId(ownerId))
				.sites(sites.stream().map(s -> new SiteId(s.getSiteId())).collect(Collectors.toSet()))
				.build();
	}

	public static SSHKeyEntity.SSHKeyEntityBuilder builder() {
		return new SSHKeyEntity.SSHKeyEntityBuilder();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		SSHKeyEntity entity = (SSHKeyEntity) o;
		return Objects.equals(id, entity.id) && Objects.equals(name, entity.name)
				&& Objects.equals(value, entity.value)
				&& Objects.equals(createTime, entity.createTime)
				&& Objects.equals(updateTime, entity.updateTime)
				&& Objects.equals(ownerId, entity.ownerId) 
				&& Objects.equals(sites, entity.sites);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, value, createTime, updateTime, ownerId, sites);
	}

	public static class SSHKeyEntityBuilder {

		private UUID id;
		private String name;
		private String value;
		private LocalDateTime createTime;
		private LocalDateTime updateTime;
		private String ownerId;
		private Set<SSHKeySiteReference> sites;

		public SSHKeyEntity.SSHKeyEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public SSHKeyEntity.SSHKeyEntityBuilder name(String name) {
			this.name = name;
			return this;
		}

		public SSHKeyEntity.SSHKeyEntityBuilder value(String value) {
			this.value = value;
			return this;
		}

		public SSHKeyEntity.SSHKeyEntityBuilder createTime(LocalDateTime createTime) {
			this.createTime = createTime;
			return this;
		}

		public SSHKeyEntity.SSHKeyEntityBuilder updateTime(LocalDateTime updateTime) {
			this.updateTime = updateTime;
			return this;
		}

		public SSHKeyEntity.SSHKeyEntityBuilder ownerId(String ownerId) {
			this.ownerId = ownerId;
			return this;
		}

		public SSHKeyEntity.SSHKeyEntityBuilder sites(Set<SiteId> sites) {
			this.sites = sites != null
					? sites.stream().map(s -> new SSHKeySiteReference(s.id)).collect(
							Collectors.toSet())
					: Collections.emptySet();
			return this;
		}

		public SSHKeyEntity build() {
			return new SSHKeyEntity(id, name, value, ownerId, createTime, updateTime, sites);
		}
	}

}
