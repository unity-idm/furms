/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.domain.ssh_keys;

import io.imunity.furms.domain.sites.SiteId;

public class InstalledSSHKey {
	public final InstalledSSHKeyId id;
	public final SiteId siteId;
	public final SSHKeyId sshkeyId;
	public final String value;

	InstalledSSHKey(InstalledSSHKeyId id, SiteId siteId, SSHKeyId sshkeyId, String value) {

		this.id = id;
		this.siteId = siteId;
		this.sshkeyId = sshkeyId;
		this.value = value;
	}

	@Override
	public String toString() {
		return "InstalledSSHKey{" + "id=" + id + ", siteId=" + siteId + ", sshkeyId=" + sshkeyId + ", value="
				+ value + '}';
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private InstalledSSHKeyId id;
		private SiteId siteId;
		private SSHKeyId sshkeyId;
		private String value;

		private Builder() {
		}

		public Builder id(String id) {
			this.id = new InstalledSSHKeyId(id);
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

		public Builder sshkeyId(String sshkeyId) {
			this.sshkeyId = new SSHKeyId(sshkeyId);
			return this;
		}

		public Builder sshkeyId(SSHKeyId sshkeyId) {
			this.sshkeyId = sshkeyId;
			return this;
		}

		public Builder value(String value) {
			this.value = value;
			return this;
		}

		public InstalledSSHKey build() {
			return new InstalledSSHKey(id, siteId, sshkeyId, value);
		}
	}
}
