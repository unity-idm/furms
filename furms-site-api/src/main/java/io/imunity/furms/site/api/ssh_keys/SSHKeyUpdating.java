/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.site.api.ssh_keys;

import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Objects;

public class SSHKeyUpdating {

	public final SiteExternalId siteExternalId;
	public final FenixUserId user;
	public final String userUid;
	public final String oldPublicKey;
	public final String newPublicKey;

	SSHKeyUpdating(SiteExternalId siteExternalId, FenixUserId user, String userUid, String oldPublicKey,
			String newPublicKey) {

		this.siteExternalId = siteExternalId;
		this.user = user;
		this.userUid = userUid;
		this.oldPublicKey = oldPublicKey;
		this.newPublicKey = newPublicKey;
	}

	@Override
	public int hashCode() {
		return Objects.hash(newPublicKey, oldPublicKey, siteExternalId, user, userUid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SSHKeyUpdating other = (SSHKeyUpdating) obj;
		return Objects.equals(newPublicKey, other.newPublicKey)
				&& Objects.equals(oldPublicKey, other.oldPublicKey)
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
		private String oldPublicKey;
		private String newPublicKey;

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

		public Builder oldPublicKey(String oldPublicKey) {
			this.oldPublicKey = oldPublicKey;
			return this;
		}

		public Builder newPublicKey(String newPublicKey) {
			this.newPublicKey = newPublicKey;
			return this;
		}

		public SSHKeyUpdating build() {
			return new SSHKeyUpdating(siteExternalId, user, userUid, oldPublicKey, newPublicKey);
		}
	}

}
