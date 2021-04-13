/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.ssh_keys;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Sets;

import io.imunity.furms.domain.users.PersistentId;

class SSHKeyViewModel {

	public final String id;
	public final PersistentId ownerId;
	public final String rowSiteId;
	public final ZonedDateTime createTime;

	private Set<String> sites;
	private String name;
	private String value;
	private ZonedDateTime updateTime;

	SSHKeyViewModel(PersistentId ownerId) {
		this.id = null;
		this.rowSiteId = null;
		this.ownerId = ownerId;
		this.createTime = ZonedDateTime.now();
		this.sites = Collections.emptySet();
	}

	SSHKeyViewModel(String id, PersistentId ownerId, String name, String siteId, Set<String> sites, String value,
			ZonedDateTime createTime, ZonedDateTime updateTime) {

		this.id = id;
		this.ownerId = ownerId;
		this.name = name;
		this.rowSiteId = siteId;
		this.sites = sites;
		this.value = value;
		this.createTime = createTime;
		this.updateTime = updateTime;
	}

	public Set<String> getSites() {
		return sites;
	}

	public void setSites(Set<String> sites) {
		this.sites = sites;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public ZonedDateTime getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(ZonedDateTime updateTime) {
		this.updateTime = updateTime;
	}

	@Override
	public int hashCode() {
		return Objects.hash(createTime, id, name, ownerId, rowSiteId, sites, updateTime, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SSHKeyViewModel other = (SSHKeyViewModel) obj;
		return Objects.equals(createTime, other.createTime) && Objects.equals(id, other.id)
				&& Objects.equals(name, other.name) && Objects.equals(ownerId, other.ownerId)
				&& Objects.equals(rowSiteId, other.rowSiteId) && Objects.equals(sites, other.sites)
				&& Objects.equals(updateTime, other.updateTime) && Objects.equals(value, other.value);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private String id;
		private PersistentId ownerId;
		private String rowSiteId;
		private ZonedDateTime createTime;
		private Set<String> sites = Collections.emptySet();
		private String name;
		private String value;
		private ZonedDateTime updateTime;

		private Builder() {
		}

		public Builder id(String id) {
			this.id = id;
			return this;
		}

		public Builder ownerId(PersistentId ownerId) {
			this.ownerId = ownerId;
			return this;
		}

		public Builder rowSiteId(String rowSiteId) {
			this.rowSiteId = rowSiteId;
			return this;
		}

		public Builder createTime(ZonedDateTime createTime) {
			this.createTime = createTime;
			return this;
		}

		public Builder sites(Set<String> sites) {
			this.sites = sites == null ? Sets.newHashSet() : sites;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder value(String value) {
			this.value = value;
			return this;
		}

		public Builder updateTime(ZonedDateTime updateTime) {
			this.updateTime = updateTime;
			return this;
		}

		public SSHKeyViewModel build() {
			return new SSHKeyViewModel(id, ownerId, name, rowSiteId, sites, value, createTime, updateTime);
		}
	}

}
