/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.rest.cidp;

import java.util.Objects;
import java.util.Set;

import io.imunity.furms.domain.users.SiteSSHKeys;

class SiteSSHKeysJson {

	public final String siteId;
	public final String siteName;
	public final Set<String> sshKeys;
	
	SiteSSHKeysJson(String siteId, String siteName, Set<String> sshKeys) {
		this.siteId = siteId;
		this.siteName = siteName;
		this.sshKeys = Set.copyOf(sshKeys);
	}
	
	SiteSSHKeysJson(SiteSSHKeys keys) {
		this.siteId = keys.siteId;
		this.siteName = keys.siteName;
		this.sshKeys = Set.copyOf(keys.sshKeys);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteId, siteName, sshKeys);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SiteSSHKeysJson other = (SiteSSHKeysJson) obj;
		return Objects.equals(siteId, other.siteId) && Objects.equals(siteName, other.siteName)
				&& Objects.equals(sshKeys, other.sshKeys);
	}

	
	

}
