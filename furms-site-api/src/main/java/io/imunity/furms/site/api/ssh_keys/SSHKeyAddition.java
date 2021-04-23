/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.site.api.ssh_keys;

import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Objects;


public class SSHKeyAddition {

	public final SiteExternalId siteExternalId;
	public final FenixUserId user;
	public final String userUid;
	public final String publicKey;

	SSHKeyAddition(SiteExternalId siteExternalId, FenixUserId user, String userUid, String publicKey) {
		
		this.siteExternalId = siteExternalId;
		this.user = user;
		this.userUid = userUid;
		this.publicKey = publicKey;
	}

	@Override
	public int hashCode() {
		return Objects.hash(publicKey, siteExternalId, user, userUid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SSHKeyAddition other = (SSHKeyAddition) obj;
		return Objects.equals(publicKey, other.publicKey)
				&& Objects.equals(siteExternalId, other.siteExternalId)
				&& Objects.equals(user, other.user) && Objects.equals(userUid, other.userUid);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private SiteExternalId siteExternalId;
		private FenixUserId user;
		private String userUid;
		private String publicKey;

		private Builder() {
		}

		public Builder siteExternalId(SiteExternalId siteExternalId) {
			this.siteExternalId = siteExternalId;
			return this;
		}

		public Builder user(FenixUserId user) {
			this.user = user;
			return this;
		}

		public Builder userUid(String userUid) {
			this.userUid = userUid;
			return this;
		}

		public Builder publicKey(String publicKey) {
			this.publicKey = publicKey;
			return this;
		}

		public SSHKeyAddition build() {
			return new SSHKeyAddition(siteExternalId, user, userUid, publicKey);
		}
	}

}
