/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.rest.cidp;

import java.util.Objects;
import java.util.Set;

import io.imunity.furms.domain.users.UserSSHKey;

class UserSSHKeyJson {

	public final String id;
	public final String value;
	public final Set<String> sites;

	UserSSHKeyJson(UserSSHKey userSSHKey) {
		this.id = userSSHKey.id;
		this.value = userSSHKey.value;
		this.sites = Set.copyOf(userSSHKey.sites);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, sites, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserSSHKeyJson other = (UserSSHKeyJson) obj;
		return Objects.equals(id, other.id) && Objects.equals(sites, other.sites)
				&& Objects.equals(value, other.value);
	}

}
