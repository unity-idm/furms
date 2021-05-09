/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.domain.users;

import java.util.Objects;
import java.util.Set;

public class SiteSSHKeys {
	
	public final String siteId;
	public final String siteName;
	public final Set<String> sshKeys;
	
	public SiteSSHKeys(String siteId, String siteName, Set<String> sshKeys) {
		
		this.siteId = siteId;
		this.siteName = siteName;
		this.sshKeys =  Set.copyOf(sshKeys);
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
		SiteSSHKeys other = (SiteSSHKeys) obj;
		return Objects.equals(siteId, other.siteId) && Objects.equals(siteName, other.siteName)
				&& Objects.equals(sshKeys, other.sshKeys);
	}
}
