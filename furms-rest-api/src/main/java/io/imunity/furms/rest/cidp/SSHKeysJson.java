/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.rest.cidp;

import java.util.Objects;
import java.util.Set;

import io.imunity.furms.domain.users.SiteSSHKeys;

class SSHKeysJson {

	public final String siteId;
	public final Set<String> sshKeys;
	
	SSHKeysJson(SiteSSHKeys keys) {
		this.siteId = keys.siteId;
		this.sshKeys = Set.copyOf(keys.sshKeys);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SSHKeysJson that = (SSHKeysJson) o;
		return Objects.equals(siteId, that.siteId) && Objects.equals(sshKeys, that.sshKeys);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteId, sshKeys);
	}

	@Override
	public String toString() {
		return "SSHKeysJson{" +
				"siteId='" + siteId + '\'' +
				", sshKeys=" + sshKeys +
				'}';
	}
}
