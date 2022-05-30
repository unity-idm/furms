/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.ssh_keys;

import com.google.common.collect.Sets;
import io.imunity.furms.domain.ssh_keys.SSHKeyId;
import io.imunity.furms.domain.users.PersistentId;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

class SSHKeyViewModel {

	public final SSHKeyId id;
	public final PersistentId ownerId;
	public final ZonedDateTime createTime;
	public final SiteWithKeyStatus sourceSite;

	public final Set<SiteWithKeyStatus> sites;
	public final String name;
	public final String value;

	SSHKeyViewModel(PersistentId ownerId) {
		this.id = null;
		this.ownerId = ownerId;
		this.createTime = ZonedDateTime.now();
		this.sites = Collections.emptySet();
		this.sourceSite = null;
		this.name = null;
		this.value = null;
	}

	SSHKeyViewModel(SSHKeyId id, PersistentId ownerId, String name, SiteWithKeyStatus sourceSite,
			Set<SiteWithKeyStatus> sites, String value, ZonedDateTime createTime) {

		this.id = id;
		this.ownerId = ownerId;
		this.name = name;
		this.sites = sites;
		this.value = value;
		this.createTime = createTime;
		this.sourceSite = sourceSite;
	}

	@Override
	public int hashCode() {
		return Objects.hash(createTime, id, name, ownerId, sourceSite, sites, value);
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
				&& Objects.equals(sites, other.sites) 
				&& Objects.equals(sourceSite, other.sourceSite) 
				&& Objects.equals(value, other.value);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private SSHKeyId id;
		private PersistentId ownerId;
		private ZonedDateTime createTime;
		private Set<SiteWithKeyStatus> sites = Collections.emptySet();
		private String name;
		private String value;
		private SiteWithKeyStatus sourceSite;

		private Builder() {
		}

		public Builder id(SSHKeyId id) {
			this.id = id;
			return this;
		}

		public Builder ownerId(PersistentId ownerId) {
			this.ownerId = ownerId;
			return this;
		}

		public Builder createTime(ZonedDateTime createTime) {
			this.createTime = createTime;
			return this;
		}

		public Builder sites(Set<SiteWithKeyStatus> sites) {
			this.sites = sites == null ? Sets.newHashSet() : sites;
			return this;
		}

		public Builder sourceSite(SiteWithKeyStatus sourceSite) {
			this.sourceSite = sourceSite;
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

		public SSHKeyViewModel build() {
			return new SSHKeyViewModel(id, ownerId, name, sourceSite, sites, value, createTime);
		}
	}

}
