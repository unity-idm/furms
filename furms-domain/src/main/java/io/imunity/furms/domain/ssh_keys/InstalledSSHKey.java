/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.domain.ssh_keys;

public class InstalledSSHKey {
	public final String id;
	public final String siteId;
	public final String sshkeyId;
	public final String value;

	InstalledSSHKey(String id, String siteId, String sshkeyId, String value) {

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
		private String id;
		private String siteId;
		private String sshkeyId;
		private String value;

		private Builder() {
		}

		public Builder id(String id) {
			this.id = id;
			return this;
		}

		public Builder siteId(String siteId) {
			this.siteId = siteId;
			return this;
		}

		public Builder sshkeyId(String sshkeyId) {
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
