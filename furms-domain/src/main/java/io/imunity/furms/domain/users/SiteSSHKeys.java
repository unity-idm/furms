/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.domain.users;

import java.util.Objects;
import java.util.Set;

public class SiteSSHKeys {
	
	public final String siteId;
	public final Set<String> sshKeys;
	
	public SiteSSHKeys(String siteId, Set<String> sshKeys) {
		this.siteId = siteId;
		this.sshKeys =  Set.copyOf(sshKeys);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SiteSSHKeys that = (SiteSSHKeys) o;
		return Objects.equals(siteId, that.siteId) && Objects.equals(sshKeys, that.sshKeys);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteId, sshKeys);
	}
}
