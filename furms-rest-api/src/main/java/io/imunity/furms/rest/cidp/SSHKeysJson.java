/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.rest.cidp;

import java.util.Objects;
import java.util.Set;

import io.imunity.furms.domain.users.SiteSSHKeys;

class SSHKeysJson {

	public final Set<String> sshKeys;
	
	SSHKeysJson(SiteSSHKeys keys) {
		this.sshKeys = Set.copyOf(keys.sshKeys);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sshKeys);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SSHKeysJson other = (SSHKeysJson) obj;
		return Objects.equals(sshKeys, other.sshKeys);
	}

}
